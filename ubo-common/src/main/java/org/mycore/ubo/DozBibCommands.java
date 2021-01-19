/**
 * Copyright (c) 2016 Duisburg-Essen University Library
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package org.mycore.ubo;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.mycore.ubo.importer.scopus.ScopusInitialImporter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaders;
import org.mycore.common.MCRConstants;
import org.mycore.common.MCRException;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.content.transformer.MCRXSLTransformer;
import org.mycore.common.xml.MCRXMLHelper;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.ifs2.MCRMetadataStore;
import org.mycore.datamodel.ifs2.MCRStore;
import org.mycore.datamodel.ifs2.MCRStoreManager;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.cli.MCRAbstractCommands;
import org.mycore.frontend.cli.MCRCommand;
import org.mycore.mods.MCRMODSCommands;
import org.mycore.mods.MCRMODSWrapper;

public class DozBibCommands extends MCRAbstractCommands {

    private static final Logger LOGGER = LogManager.getLogger(DozBibCommands.class);

    /** Commands for the MyCoRe Command Line Interface */
    public DozBibCommands() {
        addCommand(new MCRCommand("ubo mods export all entries to directory {0}",
            "org.mycore.ubo.DozBibCommands.exportMODS String",
            "exports all entries as MODS dump to a zipped xml file in local directory {0}"));
        addCommand(new MCRCommand("ubo transform entries using xsl {0}",
            "org.mycore.ubo.DozBibCommands.transformEntries String",
            "Transforms persistent xml of all bibentries using XSL stylesheet"));
        addCommand(new MCRCommand("ubo find gnds", "org.mycore.ubo.DozBibGNDCommands.findGNDs", "Find GNDs"));
        addCommand(new MCRCommand("ubo migrate to mcrobject", "org.mycore.ubo.DozBibCommands.migrate2mcrobject",
            "migrates all bibentries to mycoreobject persistence"));
        addCommand(new MCRCommand("ubo build duplicates report to directory {0}",
            "org.mycore.ubo.dedup.DeDupCommands.buildDuplicatesReport String",
            "builds report on possibly duplicate entries and writes it as xml to file duplicates.xml in directory {0}"));
        addCommand(new MCRCommand("ubo import mods collection from file {0}",
            "org.mycore.ubo.DozBibCommands.importMODSCollection String",
            "import mods:modsCollection from xml file {0}"));
        addCommand(new MCRCommand("ubo update from scopus for affiliation IDs {0} last {1} days max {2}",
            "org.mycore.ubo.importer.scopus.ScopusUpdater.update String int int",
            "Queries Scopus for new publications of comma-separated affiliation IDs {0} added within last {1} days," +
                "retrieves max {2} publications and imports them if not already present"));
        addCommand(new MCRCommand(ScopusInitialImporter.IMPORT_BATCH_COMMAND,
                "org.mycore.ubo.importer.scopus.ScopusInitialImporter.initialImport String int",
                "Queries all affiliation IDs and imports all documents {0} = afid {1} = start point should be 0"));
        addCommand(new MCRCommand(ScopusInitialImporter.IMPORT_SINGLE_COMMAND,
                "org.mycore.ubo.importer.scopus.ScopusInitialImporter.doImport String",
                "{0] = ID of object"));
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
    public static List<String> transformEntries(String xslFile) throws Exception {
        List<String> commands = new ArrayList<String>();
        for (String oid : MCRXMLMetadataManager.instance().listIDsOfType("mods")) {
            commands.add("xslt " + oid + " with file " + xslFile);
        }
        return commands;
    }

    public static void migrate2mcrobject() throws Exception {
        MCRMetadataStore store = MCRStoreManager.createStore("ubo", MCRMetadataStore.class);
        for (Iterator<Integer> IDs = store.listIDs(MCRStore.ASCENDING); IDs.hasNext();) {
            int id = IDs.next();
            LOGGER.info("Migrating <bibentry> " + id + " to <mycoreobject>...");

            MCRObjectID oid = MCRObjectID.getInstance(MCRObjectID.formatID("ubo_mods", id));
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
            for (Element tag : root.getChildren("tag")) {
                extension.addContent(tag.clone());
            }

            MCRMODSWrapper wrapper = new MCRMODSWrapper();
            wrapper.setServiceFlag("status", root.getAttributeValue("status"));
            wrapper.setMODS(mods.detach());
            MCRObject obj = wrapper.getMCRObject();

            obj.setId(oid);
            MCRMetadataManager.create(obj);
        }
    }

    public static void importMODSCollection(String fileName) throws Exception {
        File file = new File(fileName);
        if (!file.isFile()) {
            throw new MCRException(MessageFormat.format("File {0} is not a file.", file.getAbsolutePath()));
        }
        SAXBuilder s = new SAXBuilder(XMLReaders.NONVALIDATING, null, null);
        Document doc = s.build(file);
        MCRXMLHelper.validate(doc, MCRMODSCommands.MODS_V3_XSD_URI);
        Element root = doc.getRootElement();
        if (!root.getNamespace().equals(MCRConstants.MODS_NAMESPACE)) {
            throw new MCRException(MessageFormat.format("File {0} is not a MODS document.", file.getAbsolutePath()));
        }
        if (!root.getName().equals("modsCollection")) {
            throw new MCRException(
                MessageFormat.format("File {0} does not contain a mods:modsCollection.", file.getAbsolutePath()));
        }
        for (Element mods : root.getChildren("mods", MCRConstants.MODS_NAMESPACE)) {
            MCRMODSWrapper wrapper = new MCRMODSWrapper();
            wrapper.setServiceFlag("status", "imported");
            wrapper.setMODS(mods.clone());
            MCRObject obj = wrapper.getMCRObject();

            obj.setId(MCRObjectID.getNextFreeId("ubo_mods"));
            MCRMetadataManager.create(obj);
        }
    }
}
