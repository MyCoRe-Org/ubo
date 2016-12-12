/**
 * $Revision$ 
 * $Date$
 *
 * This file is part of the MILESS repository software.
 * Copyright (C) 2011 MILESS/MyCoRe developer team
 * See http://duepublico.uni-duisburg-essen.de/ and http://www.mycore.de/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
        else if (DozBibEntryServlet.systemInReadOnlyMode())
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

    private void doSaveEntries(HttpServletResponse res) throws IOException, JDOMException {
        MCRBasket basket = MCRBasketManager.getOrCreateBasketInSession("import");
        for (Iterator<MCRBasketEntry> iterator = basket.iterator(); iterator.hasNext();) {
            MCRBasketEntry entry = iterator.next();
            Element root = entry.getContent();
            Document bibentry = new Document(root);
            DozBibManager.instance().saveEntry(bibentry);
        }
        basket.clear();
        res.sendRedirect(getServletBaseURL() + "MCRBasketServlet?type=import&action=show");
    }
}
