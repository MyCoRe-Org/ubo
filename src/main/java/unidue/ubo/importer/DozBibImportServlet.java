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
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.mycore.frontend.basket.MCRBasket;
import org.mycore.frontend.basket.MCRBasketEntry;
import org.mycore.frontend.basket.MCRBasketManager;
import org.mycore.frontend.editor.MCREditorSubmission;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;

import unidue.ubo.AccessControl;
import unidue.ubo.DozBibEntryServlet;
import unidue.ubo.DozBibManager;
import unidue.ubo.dedup.DeDupCriteriaBuilder;
import unidue.ubo.importer.bibtex.BibTeXImportJob;
import unidue.ubo.importer.evaluna.EvalunaImportJob;

public class DozBibImportServlet extends MCRServlet {

    public void doGetPost(MCRServletJob job) throws Exception {
        HttpServletRequest req = job.getRequest();
        HttpServletResponse res = job.getResponse();

        if (!AccessControl.currentUserIsAdmin())
            res.sendError(HttpServletResponse.SC_FORBIDDEN);
        else if (AccessControl.systemInReadOnlyMode())
            DozBibEntryServlet.sendReadOnlyError(res);
        else if ("save".equals(req.getParameter("action")))
            doSaveEntries(res);
        else
            doImport(req, res);
    }

    private void doImport(HttpServletRequest req, HttpServletResponse res) throws Exception, IOException {
        MCREditorSubmission sub = (MCREditorSubmission) (req.getAttribute("MCREditorSubmission"));

        ImportJob job = null;
        Element parameters = null;

        if (sub != null) { // BibTeX Import
            job = BibTeXImportJob.buildFrom(sub);
            parameters = sub.getXML().getRootElement();
        } else { // Evaluna Import
            Document xml = (Document) (req.getAttribute("MCRXEditorSubmission"));
            job = new EvalunaImportJob(xml.getRootElement().getChild("request").detach());
            parameters = xml.getRootElement();
        }

        List<Document> entries = job.transform();
        new CategoryAdder(parameters).addCategories(entries);
        addDeDupCriteria(entries);
        addToBasket(entries);
        res.sendRedirect(getServletBaseURL() + "MCRBasketServlet?type=import&action=show");
    }

    private void addDeDupCriteria(List<Document> entries) {
        DeDupCriteriaBuilder ddcb = new DeDupCriteriaBuilder();
        for (Document entry : entries)
            ddcb.updateDeDupCriteria(entry);
    }

    private void addToBasket(List<Document> entries) {
        MCRBasket basket = MCRBasketManager.getOrCreateBasketInSession("import");
        for (Document bibentry : entries) {
            String id = String.valueOf(basket.size() + 1);
            MCRBasketEntry entry = new MCRBasketEntry(id, "imported:" + id);
            entry.setContent(bibentry.detachRootElement());
            basket.add(entry);
        }
    }

    private void doSaveEntries(HttpServletResponse res) throws IOException, JDOMException, Exception {
        MCRBasket basket = MCRBasketManager.getOrCreateBasketInSession("import");
        for (Iterator<MCRBasketEntry> iterator = basket.iterator(); iterator.hasNext();) {
            MCRBasketEntry entry = iterator.next();
            Element root = entry.getContent();
            Document bibentry = new Document(root);
            DozBibManager.instance().createEntry(bibentry);
        }
        basket.clear();
        res.sendRedirect(getServletBaseURL() + "MCRBasketServlet?type=import&action=show");
    }
}
