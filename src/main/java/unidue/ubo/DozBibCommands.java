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
import java.util.Optional;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
        addCommand(new MCRCommand("ubo rebuild search index", "unidue.ubo.DozBibIndexer.rebuildIndex",
            "rebuilds the search index of all entries"));
        addCommand(new MCRCommand("ubo fix origin", "unidue.ubo.DozBibCommands.fixOrigin",
            "Fixes all origin fields in entries, by removing non-existing category references and changing moved categories"));
        addCommand(new MCRCommand("ubo collect statistics {0}", "unidue.ubo.DozBibStatistics.collectStatistics String",
            "Counts number of publications by status, type etc. from all entries, web application directory is parameter {0}"));
        addCommand(new MCRCommand("ubo find gnds", "unidue.ubo.DozBibGNDCommands.findGNDs", "Find GNDs"));
        addCommand(new MCRCommand("ubo migrate to mcrobject", "unidue.ubo.DozBibCommands.migrate2mcrobject",
            "migrates all bibentries to mycoreobject persistence"));
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

        LOGGER.info("Collecting all entries...");

        Element bibentries = new Element("bibentries");
        Document doc = new Document(bibentries);

        Iterator<Integer> IDs = DozBibManager.instance().iterateStoredIDs();
        while (IDs.hasNext()) {
            int id = IDs.next();
            try {
                Document entry = DozBibManager.instance().getEntry(id);
                String status = entry.getRootElement().getAttributeValue("status", "");
                if (!status.equals("confirmed"))
                    continue;
                bibentries.addContent(entry.detachRootElement());
            } catch (Exception ex) {
                String msg = "Skipping corrupted entry " + id;
                LOGGER.warn(msg, ex);
            }
        }

        LOGGER.info("Transforming entries to MODS...");

        MCRXSLTransformer transformer = new MCRXSLTransformer("xsl/bibentries-mods.xsl");
        MCRJDOMContent source = new MCRJDOMContent(doc);
        transformer.transform(source, zip);

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
            Document xml = store.retrieve(id).getMetadata().asXML();
            Element root = xml.getRootElement();

            LOGGER.info("Migrating <bibentry> " + id + " to <mycoreobject>...");

            MCRObjectID oid = DozBibManager.buildMCRObjectID(id);
            if (MCRMetadataManager.exists(oid)) {
                LOGGER.info("object " + oid.toString() + " already exists, skipping...");
                continue;
            }

            MCRMODSWrapper wrapper = new MCRMODSWrapper();
            wrapper.setServiceFlag("status", root.getAttributeValue("status"));
            Element mods = root.getChild("mods", MCRConstants.MODS_NAMESPACE).clone();
            wrapper.setMODS(mods);
            MCRObject obj = wrapper.getMCRObject();

            obj.setId(oid);
            MCRMetadataManager.create(obj);
        }
    }
}
