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

import org.jdom2.Document;
import org.jdom2.Element;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;

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
            doImport(req, res);
        }
    }

    private void doImport(HttpServletRequest req, HttpServletResponse res) throws Exception {
        Document doc = (Document) (req.getAttribute("MCRXEditorSubmission"));
        Element formInput = doc.detachRootElement();

        ImportJob importJob = buildImportJob(formInput);
        importJob.transform(formInput);
        importJob.saveAndIndex();

        redirectToResultsPage(res, importJob);
    }

    private ImportJob buildImportJob(Element formInput) {
        String type = formInput.getAttributeValue("type");
        return ("Evaluna".equals(type) ? new EvalunaImportJob() : new ListImportJob(type));
    }

    private void redirectToResultsPage(HttpServletResponse res, ImportJob importJob) throws IOException {
        String queryString = importJob.getQueryString();
        String url = "solr/select?q=" + queryString;
        res.sendRedirect(getServletBaseURL() + url);
    }
}
