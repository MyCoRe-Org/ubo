/**
 * Copyright (c) 2016 Duisburg-Essen University Library
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package org.mycore.ubo.importer;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRConstants;
import org.mycore.common.MCRPersistenceException;
import org.mycore.common.MCRTransactionHelper;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.transformer.MCRContentTransformer;
import org.mycore.common.content.transformer.MCRContentTransformerFactory;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.mods.enrichment.MCREnricher;
import org.mycore.solr.MCRSolrClientFactory;
import org.mycore.solr.MCRSolrUtils;
import org.xml.sax.SAXException;

public abstract class ImportJob {

    private static final Logger LOGGER = LogManager.getLogger(ImportJob.class);

    private static final SimpleDateFormat ID_BUILDER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT);

    private static final String PROJECT_ID = MCRConfiguration2.getString("UBO.projectid.default").get();

    private static final String ENRICHER_CONFIG_ID = "import-list";

    private List<Document> publications = new ArrayList<Document>();

    private String id = ID_BUILDER.format(new Date());

    public String getID() {
        return id;
    }

    public List<Document> getPublications() {
        return publications;
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

    public void enrich() {
        for (Document publication : publications) {
            MCREnricher enricher = new MCREnricher(ENRICHER_CONFIG_ID);
            enricher.enrich(getContainedMODS(publication));
        }
    }

    private Element getContainedMODS(Document publication) {
        Element mcrobject = publication.getRootElement();
        Element container = mcrobject.getChild("metadata").getChild("def.modsContainer").getChild("modsContainer");
        Element mods = container.getChild("mods", MCRConstants.MODS_NAMESPACE);
        return mods;
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
            MCRObjectID oid = MCRObjectID.getNextFreeId(PROJECT_ID + "_mods");
            obj.setId(oid);
            obj.getService().addFlag("importID", id);
            MCRMetadataManager.create(obj);
        }
    }

    public void transform(Element formInput) throws Exception {
        LOGGER.info("Importing from " + getTransformerID() + "...");
        MCRContent source = getSource(formInput);
        transform(source);
        addFixedCategories(formInput);
        LOGGER.info("Transformed " + getNumPublications() + " publications");
    }

    public void saveAndIndex() throws MCRAccessException {
        savePublications();
        MCRTransactionHelper.commitTransaction();
        tryToWaitUntilSolrIndexingFinished();
    }

    public String getQueryString() {
        return "importID:\"" + MCRSolrUtils.escapeSearchValue(this.id) + "\"";
    }

    private static final int MAX_SOLR_CHECKS = 10; // times

    private static final int SECONDS_TO_WAIT_BETWEEN_SOLR_CHECKS = 2;

    private void tryToWaitUntilSolrIndexingFinished() {
        SolrClient solrClient = MCRSolrClientFactory.getMainSolrClient();
        SolrQuery query = new SolrQuery();
        query.setQuery(getQueryString());
        query.setRows(0);

        try {
            int numTries = 0;
            long numFound;
            do {
                TimeUnit.SECONDS.sleep(SECONDS_TO_WAIT_BETWEEN_SOLR_CHECKS);
                numFound = solrClient.query(query).getResults().getNumFound();
                LOGGER.info("Check if SOLR indexed all publications: #" + numTries + " " + numFound + " / "
                    + getNumPublications());
            } while ((numFound < getNumPublications()) && (++numTries < MAX_SOLR_CHECKS));
        } catch (Exception ex) {
            LOGGER.warn(ex);
        }
    }

    protected abstract String getTransformerID();

    protected abstract MCRContent getSource(Element formInput) throws Exception;
}
