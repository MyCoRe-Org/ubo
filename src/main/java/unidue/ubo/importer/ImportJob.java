/**
 * Copyright (c) 2016 Duisburg-Essen University Library
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package unidue.ubo.importer;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.mycore.access.MCRAccessException;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.transformer.MCRContentTransformer;
import org.mycore.common.content.transformer.MCRContentTransformerFactory;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.xml.sax.SAXException;

public abstract class ImportJob {

    private final static Logger LOGGER = LogManager.getLogger(ImportJob.class);

    private final static SimpleDateFormat idBuilder = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    protected MCRContent source;

    protected String id;

    protected String type;

    protected String label;

    protected Element parameters;

    protected ImportJob(String type) {
        this.type = type;
        this.id = idBuilder.format(new Date());
    }

    public String getID() {
        return id;
    }

    public void transformAndImport() throws IOException, JDOMException, SAXException, MCRAccessException {
        List<Document> publications = this.transform();
        new CategoryAdder(parameters).addCategories(publications);
        savePublications(publications);
    }

    protected List<Document> transform() throws IOException, JDOMException, SAXException {
        LOGGER.info("Importing " + type + " from " + label);

        String transformerID = "import." + type;
        MCRContentTransformer transformer = MCRContentTransformerFactory.getTransformer(transformerID);
        MCRContent transformed = transformer.transform(source);
        Element collection = transformed.asXML().getRootElement();
        List<Document> publications = extractPublications(collection);

        int num = publications.size();
        LOGGER.info("Transformed " + num + " " + type + " entries to MODS bibliography entries");
        return publications;
    }

    private List<Document> extractPublications(Element collection) {
        List<Document> entries = new ArrayList<Document>();
        for (Element publication : collection.getChildren())
            entries.add(new Document(publication.clone()));
        return entries;
    }

    private void savePublications(List<Document> publications) throws MCRAccessException {
        for (Document publication : publications) {
            MCRObject obj = new MCRObject(publication);
            MCRObjectID oid = MCRObjectID.getNextFreeId("ubo_mods");
            obj.setId(oid);
            obj.getService().addFlag("importID", id);
            MCRMetadataManager.create(obj);
        }
    }
}
