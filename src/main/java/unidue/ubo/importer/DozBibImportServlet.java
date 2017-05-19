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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom2.Document;
import org.mycore.frontend.editor.MCREditorSubmission;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;
import org.mycore.solr.MCRSolrClientFactory;
import org.mycore.solr.MCRSolrUtils;

import unidue.ubo.AccessControl;
import unidue.ubo.DozBibEntryServlet;
import unidue.ubo.importer.bibtex.BibTeXImportJob;
import unidue.ubo.importer.evaluna.EvalunaImportJob;

@SuppressWarnings("serial")
public class DozBibImportServlet extends MCRServlet {

    public void doGetPost(MCRServletJob job) throws Exception {
        HttpServletRequest req = job.getRequest();
        HttpServletResponse res = job.getResponse();

        if (!AccessControl.currentUserIsAdmin())
            res.sendError(HttpServletResponse.SC_FORBIDDEN);
        else if (AccessControl.systemInReadOnlyMode())
            DozBibEntryServlet.sendReadOnlyError(res);
        else
            doImport(req, res);
    }

    private void doImport(HttpServletRequest req, HttpServletResponse res) throws Exception, IOException {
        MCREditorSubmission sub = (MCREditorSubmission) (req.getAttribute("MCREditorSubmission"));

        ImportJob job = null;

        if (sub != null) { // BibTeX Import
            job = BibTeXImportJob.buildFrom(sub);
        } else { // Evaluna Import
            Document xml = (Document) (req.getAttribute("MCRXEditorSubmission"));
            job = new EvalunaImportJob(xml.getRootElement().detach());
        }

        job.transformAndImport();

        MCRSolrClientFactory.getSolrClient().optimize(true, true); // Workaround to wait for SOLR indexing finished

        String url = "solr/select?q=importID:\"" + MCRSolrUtils.escapeSearchValue(job.getID()) + "\"";
        res.sendRedirect(getServletBaseURL() + url);
    }
}
