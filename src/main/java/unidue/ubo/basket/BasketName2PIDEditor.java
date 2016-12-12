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

package unidue.ubo.basket;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.mycore.common.MCRConstants;
import org.mycore.frontend.basket.MCRBasket;
import org.mycore.frontend.basket.MCRBasketEntry;
import org.mycore.frontend.basket.MCRBasketManager;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;

import unidue.ubo.AccessControl;
import unidue.ubo.DozBibEntryServlet;
import unidue.ubo.DozBibManager;

/**
 * Servlet invoked by edit-contributors.xml to
 * change contributor name and pid entries in the basket of bibliography entries
 * and to save all changed entries when work is done.
 * 
 * @author Frank L\u00FCtzenkirchen
 */
public class BasketName2PIDEditor extends MCRServlet {
    private final static Logger LOGGER = Logger.getLogger(BasketName2PIDEditor.class);

    public void doGetPost(MCRServletJob job) throws Exception {
        HttpServletRequest req = job.getRequest();
        HttpServletResponse res = job.getResponse();

        if (! AccessControl.currentUserIsAdmin()) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        if (DozBibEntryServlet.systemInReadOnlyMode()) {
            DozBibEntryServlet.sendReadOnlyError(res);
            return;
        }

        MCRBasket basket = MCRBasketManager.getOrCreateBasketInSession("bibentries");
        Document doc = (Document) req.getAttribute("MCRXEditorSubmission");

        String action = req.getParameter("action");
        if ("save".equals(action))
            saveChangedEntries(basket);
        else if (doc != null)
        {
            changeNamesInBasket(doc, basket);
        }

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

            if (nameEntryEdited == null)
                continue;
            if (nameEntryFromBasket.getKey().equals(nameEntryEdited.getKey()))
                continue;

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

        for (Element child : nameEntryEdited.getModsName().getChildren())
            if ("namePart".equals(child.getName()) || "nameIdentifier".equals(child.getName()))
                contributor.addContent(child.clone());

        markAsChanged(contributor);
    }

    /**
     * Marks the "bibentry" element in the basket that is parent of the given contributor 
     * element to be changed by the editor form, so the servlet can identify the entries
     * that are changed and have to be saved later.
     * 
     * @param element the contributor element that was edited.
     */
    private void markAsChanged(Element element) {
        while (!element.getName().equals("bibentry"))
            element = element.getParentElement();
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
            if ("true".equals(entry.getContent().getAttributeValue("changed")))
                saveChangedEntry(entry);
        }
    }

    /**
     * Saves an entry from the basket and updates its data in the basket afterwards.
     */
    private void saveChangedEntry(MCRBasketEntry entry) throws IOException, JDOMException {
        Element bibentry = entry.getContent();
        bibentry.removeAttribute("changed");
        bibentry = (Element) (bibentry.clone());
        DozBibManager.instance().saveEntry(new Document(bibentry));
        entry.setContent((Element) (bibentry.clone())); // some data may have changed when saving!
    }
}
