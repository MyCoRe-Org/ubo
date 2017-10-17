/**
 * Copyright (c) 2017 Duisburg-Essen University Library
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package unidue.ubo.importer;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;

import org.jdom2.Document;
import org.jdom2.Element;
import org.mycore.access.MCRAccessException;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;
import org.xml.sax.SAXException;

import unidue.ubo.AccessControl;
import unidue.ubo.DozBibEntryServlet;
import unidue.ubo.importer.evaluna.EvalunaImportJob;

@SuppressWarnings("serial")
public class DozBibImportServlet extends MCRServlet {

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
        Element formInput = doc.detachRootElement();

        ImportJob importJob = buildImportJob(formInput);
        importJob.transform(formInput);

        String targetType = formInput.getAttributeValue("targetType");
        if (targetType.startsWith("preview")) {
            doPreview(req, res, importJob, targetType);
        } else {
            doImport(req, res, importJob);
        }
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
        String url = "solr/select?q=" + queryString;
        res.sendRedirect(getServletBaseURL() + url);
    }
}
