/**
 * Copyright (c) 2016 Duisburg-Essen University Library
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package unidue.ubo.basket;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.mycore.common.MCRConstants;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.frontend.basket.MCRBasket;
import org.mycore.frontend.basket.MCRBasketEntry;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;

import unidue.ubo.AccessControl;
import unidue.ubo.DozBibEntryServlet;

/**
 * Servlet invoked by edit-contributors.xml to
 * change contributor name and pid entries in the basket of bibliography entries
 * and to save all changed entries when work is done.
 *
 * @author Frank L\u00FCtzenkirchen
 */
public class BasketName2PIDEditor extends MCRServlet {

    private final static Logger LOGGER = LogManager.getLogger(BasketName2PIDEditor.class);

    public void doGetPost(MCRServletJob job) throws Exception {
        HttpServletRequest req = job.getRequest();
        HttpServletResponse res = job.getResponse();

        if (!AccessControl.currentUserIsAdmin()) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        if (AccessControl.systemInReadOnlyMode()) {
            DozBibEntryServlet.sendReadOnlyError(res);
            return;
        }

        MCRBasket basket = BasketUtils.getBasket();
        Document doc = (Document) req.getAttribute("MCRXEditorSubmission");

        changeNamesInBasket(doc, basket);
        saveChangedEntries(basket);

        res.sendRedirect(getServletBaseURL() + "MCRBasketServlet?type=bibentries&action=show");
    }

    /**
     * Takes the name entries edited in edit-contributors.xml and changes the
     * contributor elements in the basket of bibliography entries that match the name key.
     */
    private void changeNamesInBasket(Document doc, MCRBasket basket) throws Exception {
        LOGGER.info("BasketName2PIDEditor change names");

        Map<String, NameEntry> key2nameEntries = buildMapOfEditedNameEntries(doc);

        for (Element contributor : BasketNameLister.getAllContributorsFromBasket()) {
            NameEntry nameEntryFromBasket = new NameEntry(contributor);
            NameEntry nameEntryEdited = key2nameEntries.get(nameEntryFromBasket.getKey());

            if (nameEntryEdited == null) {
                continue;
            }
            if (nameEntryFromBasket.getKey().equals(nameEntryEdited.getKey())) {
                continue;
            }

            changeContributorInBasket(nameEntryFromBasket, nameEntryEdited);
        }
    }

    /**
     * Changes a single name entry in the basket.
     *
     * @param nameEntryFromBasket the name entry of a contributor element in the basket.
     * @param nameEntryEdited the edited name entry returned by the editor form.
     */
    private void changeContributorInBasket(NameEntry nameEntryFromBasket, NameEntry nameEntryEdited) {

        Element contributor = nameEntryFromBasket.getModsName();
        contributor.removeChildren("namePart", MCRConstants.MODS_NAMESPACE);
        contributor.removeChildren("nameIdentifier", MCRConstants.MODS_NAMESPACE);

        for (Element child : nameEntryEdited.getModsName().getChildren()) {
            if ("namePart".equals(child.getName()) || "nameIdentifier".equals(child.getName())) {
                if (!child.getTextTrim().isEmpty()) {
                    contributor.addContent(child.clone());
                }
            }
        }

        markAsChanged(contributor);
    }

    /**
     * Marks the "mycoreobject" element in the basket that is parent of the given contributor
     * element to be changed by the editor form, so the servlet can identify the entries
     * that are changed and have to be saved later.
     *
     * @param element the contributor element that was edited.
     */
    private void markAsChanged(Element element) {
        while (!element.getName().equals("mycoreobject")) {
            element = element.getParentElement();
        }
        element.setAttribute("changed", "true");
    }

    /**
     * Takes the data returned by edit-contributors.xml and builds a map of NameEntry
     * objects from it, where key is the old NameEntry key that was source before the
     * name changes were made.
     */
    private Map<String, NameEntry> buildMapOfEditedNameEntries(Document doc) {
        Map<String, NameEntry> key2nameEntries = new HashMap<String, NameEntry>();
        List<Element> nameEntries = doc.getRootElement().getChildren("nameEntry");
        for (Element nameEntry : nameEntries) {
            String oldKey = nameEntry.getAttributeValue("key");
            Element contributor = nameEntry.getChild("name", MCRConstants.MODS_NAMESPACE);
            key2nameEntries.put(oldKey, new NameEntry(contributor));
        }
        return key2nameEntries;
    }

    /**
     * Saves all bibliography entries in the basket that are marked as changed.
     */
    private void saveChangedEntries(MCRBasket basket) throws Exception {
        LOGGER.info("BasketName2PIDEditor save changes");

        for (MCRBasketEntry entry : basket) {
            if ("true".equals(entry.getContent().getAttributeValue("changed"))) {
                saveChangedEntry(entry);
            }
        }
    }

    /**
     * Saves an entry from the basket and updates its data in the basket afterwards.
     */
    private void saveChangedEntry(MCRBasketEntry entry) throws Exception {
        Element objxml = entry.getContent();
        objxml.removeAttribute("changed");
        objxml = (objxml.clone());

        MCRObject obj = new MCRObject(new Document(objxml));
        MCRMetadataManager.update(obj);

        objxml = obj.createXML().getRootElement().clone();
        entry.setContent(objxml); // some data may have changed when saving!
    }
}
