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
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.basket.MCRBasket;
import org.mycore.frontend.basket.MCRBasketEntry;
import org.mycore.frontend.basket.MCRBasketManager;
import org.mycore.frontend.editor.MCREditorSubmission;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;
import org.mycore.mods.MCRMODSWrapper;

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

        List<Document> publications = job.transform();
        new CategoryAdder(parameters).addCategories(publications);
        addDeDupCriteria(publications);
        addToBasket(publications);
        res.sendRedirect(getServletBaseURL() + "MCRBasketServlet?type=import&action=show");
    }

    private void addDeDupCriteria(List<Document> publications) {
        DeDupCriteriaBuilder ddcb = new DeDupCriteriaBuilder();
        for (Document publication : publications)
            ddcb.updateDeDupCriteria(publication);
    }

    private void addToBasket(List<Document> publications) {
        MCRBasket basket = MCRBasketManager.getOrCreateBasketInSession("import");
        for (Document publication : publications) {
            String id = String.valueOf(basket.size() + 1);
            MCRBasketEntry entry = new MCRBasketEntry(id, "imported:" + id);
            entry.setContent(publication.detachRootElement());
            basket.add(entry);
        }
    }

    private void doSaveEntries(HttpServletResponse res) throws IOException, JDOMException, Exception {
        MCRBasket basket = MCRBasketManager.getOrCreateBasketInSession("import");
        for (Iterator<MCRBasketEntry> iterator = basket.iterator(); iterator.hasNext();) {
            MCRBasketEntry entry = iterator.next();
            Element root = entry.getContent();
            MCRObject obj = new MCRObject(new Document(root));
            MCRObjectID oid = DozBibManager.buildMCRObjectID(0);
            oid = MCRObjectID.getNextFreeId(oid.getBase());
            obj.setId(oid);
            MCRMetadataManager.create(obj);
        }
        basket.clear();
        res.sendRedirect(getServletBaseURL() + "MCRBasketServlet?type=import&action=show");
    }
}
