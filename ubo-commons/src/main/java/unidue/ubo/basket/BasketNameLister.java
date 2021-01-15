/**
 * Copyright (c) 2016 Duisburg-Essen University Library
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package unidue.ubo.basket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.ElementFilter;
import org.jdom2.transform.JDOMSource;
import org.mycore.common.MCRConstants;
import org.mycore.frontend.basket.MCRBasket;
import org.mycore.frontend.basket.MCRBasketEntry;

/**
 * URIResolver that outputs a list of name entries of all
 * contributors of all bibliography entries currently in the basket.
 * The xml returned is taken in edit-contributors.xml to
 * edit the names and PID assignment of contributors in the basket.
 * It maps URIs of scheme nameLister:<anyDummyValue>.
 * 
 * @author Frank L\u00FCtzenkirchen
 */
public class BasketNameLister implements URIResolver {
    public Source resolve(String href, String base) throws TransformerException {
        List<NameEntry> names = new ArrayList<NameEntry>();
        List<Element> contributors = getAllContributorsFromBasket();

        for (Element contributor : contributors) {
            NameEntry nameEntry = new NameEntry(contributor);
            countOrAddName(names, nameEntry);
        }

        Collections.sort(names);
        names = names.subList(0, Math.min(names.size(), 50));
        List<Element> nameEntryElements = nameEntries2xml(names);
        return new JDOMSource(new Document(new Element("nameEntries").addContent(nameEntryElements)));
    }

    /**
     * When a NameEntry equal to the given NameEntry is already contained
     * in the list, the number of occurrences is incremented by one, 
     * otherwise the NameEntry is added as new entry to the list. 
     */
    private void countOrAddName(List<NameEntry> names, NameEntry nameEntry) {
        int index = names.indexOf(nameEntry);
        if (index >= 0)
            names.get(index).count();
        else
            names.add(nameEntry);
    }

    /**
     * Iterates over all contributor elements of all entries in the basket.
     */
    static List<Element> getAllContributorsFromBasket() {
        List<Element> contributors = new ArrayList<Element>();
        MCRBasket basket = BasketUtils.getBasket();
        for (MCRBasketEntry entry : basket) {
            Element obj = entry.getContent();
            Iterator<Element> iter = obj.getDescendants(new ElementFilter("name", MCRConstants.MODS_NAMESPACE));
            while (iter.hasNext()) {
                Element modsName = iter.next();
                if ("personal".equals(modsName.getAttributeValue("type"))) // ignore corporate or conference
                    contributors.add(modsName);

            }
        }
        return contributors;
    }

    /**
     * Given a list of NameEntry objects, builds a list of xml elements
     * that contain the data of the objects using NameEntry's buildXML() method.
     * 
     * @see NameEntry#buildXML()
     */
    private List<Element> nameEntries2xml(List<NameEntry> neList) {
        List<Element> xml = new ArrayList<Element>(neList.size());
        for (NameEntry ne : neList)
            xml.add(ne.buildXML());
        return xml;
    }
}
