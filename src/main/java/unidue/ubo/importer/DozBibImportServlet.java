/**
 * Copyright (c) 2017 Duisburg-Essen University Library
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package unidue.ubo.importer;

import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.jdom2.Document;
import org.jdom2.Element;
import org.mycore.common.MCRSessionMgr;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;
import org.mycore.solr.MCRSolrClientFactory;
import org.mycore.solr.MCRSolrUtils;

import unidue.ubo.AccessControl;
import unidue.ubo.DozBibEntryServlet;
import unidue.ubo.importer.evaluna.EvalunaImportJob;

@SuppressWarnings("serial")
public class DozBibImportServlet extends MCRServlet {

    private final static Logger LOGGER = LogManager.getLogger(DozBibImportServlet.class);

    public void doGetPost(MCRServletJob job) throws Exception {
        HttpServletRequest req = job.getRequest();
        HttpServletResponse res = job.getResponse();

        if (!AccessControl.currentUserIsAdmin()) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN);
        } else if (AccessControl.systemInReadOnlyMode()) {
            DozBibEntryServlet.sendReadOnlyError(res);
        } else {
            doImport(req, res);
        }
    }

    private void doImport(HttpServletRequest req, HttpServletResponse res) throws Exception {
        Document doc = (Document) (req.getAttribute("MCRXEditorSubmission"));
        Element formInput = doc.detachRootElement();

        ImportJob importJob = buildImportJob(formInput);
        importJob.handleImport(formInput);

        //redirectToWaitForIndexingFinished(res, importJob);

        MCRSessionMgr.getCurrentSession().commitTransaction();

        String queryString = tryToWaitUntilSolrIndexingFinished(importJob);
        String url = "solr/select?q=" + queryString;
        res.sendRedirect(getServletBaseURL() + url);
    }

    private ImportJob buildImportJob(Element formInput) {
        String type = formInput.getAttributeValue("type");
        LOGGER.info("Importing from " + type + "...");
        return ("Evaluna".equals(type) ? new EvalunaImportJob() : new ListImportJob(type));
    }

    private static final int MAX_SOLR_CHECKS = 10; // times
    private static final int SECONDS_TO_WAIT_BETWEEN_SOLR_CHECKS = 2;

    private String tryToWaitUntilSolrIndexingFinished(ImportJob importJob) {
        String importID = importJob.getID();
        int numResultsExpected = importJob.getNumPublications();

        String queryString = "importID:\"" + MCRSolrUtils.escapeSearchValue(importID) + "\"";

        SolrClient solrClient = MCRSolrClientFactory.getSolrClient();
        SolrQuery query = new SolrQuery();
        query.setQuery(queryString);
        query.setRows(0);

        try {
            int numTries = 0;
            long numFound;
            do {
                TimeUnit.SECONDS.sleep(SECONDS_TO_WAIT_BETWEEN_SOLR_CHECKS);
                numFound = solrClient.query(query).getResults().getNumFound();
                LOGGER.info("Check if SOLR indexed all publications: #" + numTries + " " + numFound + " / "
                        + numResultsExpected);
            } while ((numFound < numResultsExpected) && (++numTries < MAX_SOLR_CHECKS));
        } catch (Exception ex) {
            LOGGER.warn(ex);
        }

        return queryString;
    }
}
