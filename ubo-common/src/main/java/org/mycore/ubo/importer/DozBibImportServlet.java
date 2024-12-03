/**
 * Copyright (c) 2017 Duisburg-Essen University Library
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package org.mycore.ubo.importer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;
import org.mycore.access.MCRAccessException;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;
import org.mycore.services.queuedjob.MCRJob;
import org.mycore.services.queuedjob.MCRJobQueueManager;
import org.mycore.ubo.AccessControl;
import org.mycore.ubo.DozBibEntryServlet;
import org.mycore.ubo.importer.evaluna.EvalunaImportJob;
import org.mycore.user2.MCRUserManager;
import org.xml.sax.SAXException;

import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("serial")
public class DozBibImportServlet extends MCRServlet {

    /**
     * Cache dynamically created enrichment
     * */
    protected static final HashMap<String, String> DYNAMIC_ENRICHMENT_CONFIG_IDS = new HashMap<>();

    @Override
    public void doGetPost(MCRServletJob job) throws Exception {
        HttpServletRequest req = job.getRequest();
        HttpServletResponse res = job.getResponse();

        if (!AccessControl.currentUserIsAdmin()) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN);
        } else if (AccessControl.systemInReadOnlyMode()) {
            DozBibEntryServlet.sendReadOnlyError(res);
        } else {
            handleImportJob(req, res);
        }
    }

    private void handleImportJob(HttpServletRequest req, HttpServletResponse res) throws Exception {
        Document doc = (Document) (req.getAttribute("MCRXEditorSubmission"));
        String doAsync = doc.getRootElement().getAttributeValue("async");
        if ("true".equals(doAsync)) {
            MCRJob job = new MCRJob(ImportListJobAction.class);
            job.setParameter(ImportListJobAction.EDITOR_SUBMISSION_PARAMETER, new XMLOutputter().outputString(doc));
            job.setParameter(ImportListJobAction.USER_ID_PARAMETER,
                MCRUserManager.getCurrentUser().getUserName() + "@" + MCRUserManager.getCurrentUser().getRealmID());
            MCRJobQueueManager.getInstance().getJobQueue(ImportListJobAction.class).offer(job);

            String referer = req.getHeader("Referer");
            if (referer != null) {
                res.sendRedirect(referer + "&list-submitted=true");
            } else {
                res.sendRedirect(MCRFrontendUtil.getBaseURL());
            }
            return;
        }

        Element formInput = doc.detachRootElement();
        ImportJob importJob = buildImportJob(formInput);
        importJob.transform(formInput);

        boolean enrich = "true".equals(formInput.getAttributeValue("enrich"));
        if (enrich) {
            String dataSources = getDataSource(formInput);
            if (dataSources != null) {
                String enricherId = getOrCreateEnrichmentConfig(dataSources);
                importJob.enrich(enricherId);
            } else {
                importJob.enrich();
            }
        }

        String targetType = formInput.getAttributeValue("targetType");
        if (targetType.startsWith("preview")) {
            doPreview(req, res, importJob, targetType);
        } else {
            doImport(req, res, importJob);
        }
    }

    /**
     * Creates and registers an enrichment configuration id.
     *
     * @param dataSources the data source
     *
     * @return the enrichment configuration id for the given data source
     * */
    private String getOrCreateEnrichmentConfig(String dataSources) {
        if (DYNAMIC_ENRICHMENT_CONFIG_IDS.containsKey(dataSources)) {
            return DYNAMIC_ENRICHMENT_CONFIG_IDS.get(dataSources);
        }

        String id = UUID.nameUUIDFromBytes(dataSources.getBytes(StandardCharsets.UTF_8)).toString();
        String property = "MCR.MODS.EnrichmentResolver.DataSources." + id;
        MCRConfiguration2.set(property, dataSources);
        DYNAMIC_ENRICHMENT_CONFIG_IDS.put(dataSources, id);
        return id;
    }

    private String getDataSource(Element formInput) {
        Optional<Element> dataSource = formInput.getChildren("DataSources")
            .stream()
            .filter(element -> !element.getText().isEmpty())
            .findFirst();
        return dataSource.isPresent() ? dataSource.get().getText() : null;
    }

    private ImportJob buildImportJob(Element formInput) {
        String sourceType = formInput.getAttributeValue("sourceType");
        return ("Evaluna".equals(sourceType) ? new EvalunaImportJob() : new ListImportJob(sourceType));
    }

    private void doPreview(HttpServletRequest req, HttpServletResponse res, ImportJob importJob, String targetType)
        throws IOException, TransformerException, SAXException {
        Element export = new Element("export");
        for (Document mcrObj : importJob.getPublications()) {
            export.addContent(mcrObj.getRootElement().detach());
        }

        req.setAttribute("XSL.Style", targetType.substring(targetType.indexOf('-') + 1));
        getLayoutService().doLayout(req, res, new MCRJDOMContent(export));
    }

    private void doImport(HttpServletRequest req, HttpServletResponse res, ImportJob importJob)
        throws MCRAccessException, IOException {
        importJob.saveAndIndex();
        String queryString = importJob.getQueryString();
        String url = "solr/select?q=" + URLEncoder.encode(queryString, StandardCharsets.UTF_8);
        res.sendRedirect(getServletBaseURL() + url);
    }
}
