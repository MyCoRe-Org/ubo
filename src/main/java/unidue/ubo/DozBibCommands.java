/**
 * Copyright (c) 2016 Duisburg-Essen University Library
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package unidue.ubo;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.ElementFilter;
import org.mycore.common.MCRConstants;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.content.transformer.MCRXSLTransformer;
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryDAO;
import org.mycore.datamodel.classifications2.MCRCategoryDAOFactory;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.classifications2.MCRLabel;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.ifs2.MCRMetadataStore;
import org.mycore.datamodel.ifs2.MCRStore;
import org.mycore.datamodel.ifs2.MCRStoreManager;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.cli.MCRAbstractCommands;
import org.mycore.frontend.cli.MCRCommand;
import org.mycore.mods.MCRMODSWrapper;

public class DozBibCommands extends MCRAbstractCommands {

    private static final Logger LOGGER = LogManager.getLogger(DozBibCommands.class);

    /** Commands for the MyCoRe Command Line Interface */
    public DozBibCommands() {
        addCommand(new MCRCommand("ubo mods export all entries to directory {0}",
            "unidue.ubo.DozBibCommands.exportMODS String",
            "exports all entries as MODS dump to a zipped xml file in local directory {0}"));
        addCommand(new MCRCommand("ubo transform entry {0} using xsl {1}",
            "unidue.ubo.DozBibCommands.transformEntry int String",
            "Transforms persistent xml of bibentry using XSL stylesheet"));
        addCommand(
            new MCRCommand("ubo transform entries using xsl {0}", "unidue.ubo.DozBibCommands.transformEntries String",
                "Transforms persistent xml of all bibentries using XSL stylesheet"));
        addCommand(new MCRCommand("ubo fix origin", "unidue.ubo.DozBibCommands.fixOrigin",
            "Fixes all origin fields in entries, by removing non-existing category references and changing moved categories"));
        addCommand(new MCRCommand("ubo collect statistics {0}", "unidue.ubo.DozBibStatistics.collectStatistics String",
            "Counts number of publications by status, type etc. from all entries, web application directory is parameter {0}"));
        addCommand(new MCRCommand("ubo find gnds", "unidue.ubo.DozBibGNDCommands.findGNDs", "Find GNDs"));
        addCommand(new MCRCommand("ubo migrate to mcrobject", "unidue.ubo.DozBibCommands.migrate2mcrobject",
            "migrates all bibentries to mycoreobject persistence"));
        addCommand(new MCRCommand("ubo build duplicates report to directory {0}",
            "unidue.ubo.dedup.DeDupCommands String",
            "builds report on possibly duplicate entries and writes it as xml to file duplicates.xml in directory {0}"));
        addCommand(new MCRCommand("ubo import scopus publications from RSS feed",
            "unidue.ubo.importer.scopus.ScopusFeedImporter.importPublications",
            "Reads an RSS feed from Scopus referencing new publications and imports those publications that are not stored yet."));
    }

    /** Exports all entries as MODS dump to a zipped xml file in the given directory */
    public static void exportMODS(String dir) throws Exception {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Date now = new Date();
        String date = df.format(now);
        String fileName = "export-" + date + ".zip";

        LOGGER.info("Exporting all entries to file " + fileName + "...");

        File file = new File(dir, fileName);
        FileOutputStream fos = new FileOutputStream(file);
        ZipOutputStream zip = new ZipOutputStream(new BufferedOutputStream(fos));
        zip.setLevel(Deflater.BEST_COMPRESSION);

        ZipEntry ze = new ZipEntry("export-" + date + ".xml");
        ze.setTime(now.getTime());
        zip.putNextEntry(ze);

        LOGGER.info("Collecting object IDs...");
        List<String> oids = MCRXMLMetadataManager.instance().listIDsOfType("mods");

        LOGGER.info("Collecting all entries...");
        Element collection = new Element("modsCollection", MCRConstants.MODS_NAMESPACE);

        for (String oid : oids) {
            MCRObject obj = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(oid));
            MCRMODSWrapper wrapper = new MCRMODSWrapper(obj);
            if ("confirmed".equals(wrapper.getServiceFlag("status"))) {
                MCRContent src = new MCRJDOMContent(obj.createXML());
                MCRXSLTransformer transformer = new MCRXSLTransformer("xsl/mycoreobject-mods.xsl");
                MCRContent mods = transformer.transform(src);
                collection.addContent(mods.asXML().detachRootElement());
            }
        }
        System.out.println();

        LOGGER.info("Writing output to zip...");
        Document doc = new Document(collection);
        new MCRJDOMContent(doc).sendTo(zip);
        zip.close();

        LOGGER.info("Exported all entries to file " + file.getAbsolutePath());
    }

    /** Transforms existing entries using XSL stylesheet **/
    public static void transformEntries(String xslFile) throws Exception {
        Iterator<Integer> IDs = DozBibManager.instance().iterateStoredIDs();
        while (IDs.hasNext()) {
            transformEntry(IDs.next(), xslFile);
        }
    }

    /** Transforms existing entry using XSL stylesheet **/
    public static void transformEntry(int entryID, String xslFile) throws Exception {
        MCRXSLTransformer transformer = MCRXSLTransformer.getInstance("xsl/" + xslFile);
        Document entry = DozBibManager.instance().getEntry(entryID);
        transformEntry(entryID, transformer, entry);
    }

    private static void transformEntry(int entryID, MCRXSLTransformer transformer, Document entry) {
        try {
            MCRJDOMContent source = new MCRJDOMContent(entry);
            MCRContent result = transformer.transform(source);
            DozBibManager.instance().updateEntry(result.asXML());
            LOGGER.info("bibentry " + entryID + " transformed");
        } catch (Exception ex) {
            LOGGER
                .error("bibentry " + entryID + " NOT transformed: " + ex.getClass().getName() + ": " + ex.getMessage());
        }
    }

    public static void fixOrigin() throws Exception {
        MCRCategoryDAO DAO = MCRCategoryDAOFactory.getInstance();

        Iterator<Integer> IDs = DozBibManager.instance().iterateStoredIDs();
        LOGGER.info("Fixing origin of UBO entries...");

        while (IDs.hasNext()) {
            int ID = IDs.next();
            try {
                Document xml = DozBibManager.instance().getEntry(ID);

                for (Element classification : xml.getRootElement()
                    .getDescendants(new ElementFilter("classification", MCRConstants.MODS_NAMESPACE))) {
                    String authorityURI = classification.getAttributeValue("authorityURI");
                    if (!authorityURI.contains("ORIGIN"))
                        continue;

                    String origin = classification.getAttributeValue("valueURI").split("#")[1];

                    MCRCategoryID originID = new MCRCategoryID("ORIGIN", origin);
                    if (DAO.exist(originID)) {
                        MCRCategory category = DAO.getCategory(originID, 0);
                        Optional<MCRLabel> label = category.getLabel("x-move");
                        if (!label.isPresent())
                            continue;

                        String newCategory = label.get().getText();
                        LOGGER.info("Moving UBO entry " + ID + " from " + origin + " to " + newCategory);
                        classification.setAttribute("valueURI", authorityURI + "#" + newCategory);
                        DozBibManager.instance().updateEntry(xml);
                    } else {
                        LOGGER.warn("UBO entry " + ID + " contains illegal origin entry, removing: " + origin);
                        classification.detach();
                        DozBibManager.instance().updateEntry(xml);
                    }
                }

            } catch (Exception tolerated) {
                LOGGER.warn("Unable to fix UBO entry " + ID + ": " + tolerated.getMessage());
            }
        }
        LOGGER.info("Finished fixing origin of UBO entries");
    }

    public static void migrate2mcrobject() throws Exception {
        MCRMetadataStore store = MCRStoreManager.createStore("ubo", MCRMetadataStore.class);
        for (Iterator<Integer> IDs = store.listIDs(MCRStore.ASCENDING); IDs.hasNext();) {
            int id = IDs.next();
            LOGGER.info("Migrating <bibentry> " + id + " to <mycoreobject>...");

            MCRObjectID oid = DozBibManager.buildMCRObjectID(id);
            if (MCRMetadataManager.exists(oid)) {
                LOGGER.info("object " + oid.toString() + " already exists, skipping...");
                continue;
            }

            Document xml = store.retrieve(id).getMetadata().asXML();

            // migrate extension source elements
            Element root = xml.getRootElement();
            Element mods = root.getChild("mods", MCRConstants.MODS_NAMESPACE);

            for (Element extension : mods.getChildren("extension", MCRConstants.MODS_NAMESPACE)) {
                String type = extension.getAttributeValue("type");
                extension.removeAttribute("type");
                if ("source".equals(type)) {
                    Attribute format = extension.getAttribute("format").detach();
                    Element source = new Element("source");
                    source.setAttribute(format);
                    List<Content> content = extension.removeContent();
                    source.addContent(content);
                    extension.addContent(source);
                }
            }

            Element extension = mods.getChild("extension", MCRConstants.MODS_NAMESPACE);
            if (extension == null) {
                extension = new Element("extension", MCRConstants.MODS_NAMESPACE);
                mods.addContent(extension);
            }

            // migrate tag elements
            for (Element tag : root.getChildren("tag"))
                extension.addContent(tag.clone());

            MCRMODSWrapper wrapper = new MCRMODSWrapper();
            wrapper.setServiceFlag("status", root.getAttributeValue("status"));
            wrapper.setMODS(mods.detach());
            MCRObject obj = wrapper.getMCRObject();

            obj.setId(oid);
            MCRMetadataManager.create(obj);
        }
    }
}
