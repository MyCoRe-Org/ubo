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

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRPersistenceException;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.transformer.MCRContentTransformer;
import org.mycore.common.content.transformer.MCRContentTransformerFactory;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.xml.sax.SAXException;

public abstract class ImportJob {

    private final static SimpleDateFormat ID_BUILDER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private String id = ID_BUILDER.format(new Date());;

    private List<Document> publications = new ArrayList<Document>();

    public String getID() {
        return id;
    }

    public int getNumPublications() {
        return publications.size();
    }

    public void transform(MCRContent source) throws IOException, JDOMException, SAXException {
        String transformerID = getTransformerID();
        MCRContentTransformer transformer = MCRContentTransformerFactory.getTransformer(transformerID);
        MCRContent converted = transformer.transform(source);

        Element collection = converted.asXML().getRootElement();
        for (Element publication : collection.getChildren()) {
            publications.add(new Document(publication.clone()));
        }
    }

    public void addFixedCategories(Element formInput) {
        CategoryAdder adder = new CategoryAdder(formInput);
        for (Document publication : publications) {
            adder.addCategories(publication);
        }
    }

    public void savePublications() throws MCRPersistenceException, MCRAccessException {
        for (Document publication : publications) {
            MCRObject obj = new MCRObject(publication);
            MCRObjectID oid = MCRObjectID.getNextFreeId("ubo_mods");
            obj.setId(oid);
            obj.getService().addFlag("importID", id);
            MCRMetadataManager.create(obj);
        }
    }

    public void handleImport(Element formInput) throws Exception {
        MCRContent source = getSource(formInput);
        transform(source);
        addFixedCategories(formInput);
        savePublications();
    }

    protected abstract String getTransformerID();

    protected abstract MCRContent getSource(Element formInput) throws Exception;
}
