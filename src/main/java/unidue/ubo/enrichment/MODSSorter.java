/**
 * Copyright (c) 2016 Duisburg-Essen University Library
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package unidue.ubo.enrichment;

import java.util.Arrays;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.jdom2.Element;
import org.jdom2.transform.JDOMSource;
import org.mycore.common.xml.MCRURIResolver;

public class MODSSorter implements URIResolver {

    private final static String[] order = { "genre", "typeofResource", "titleInfo", "nonSort", "subTitle", "title",
        "partNumber", "partName", "name", "namePart", "displayForm", "role", "affiliation", "originInfo", "place",
        "publisher", "dateIssued", "dateCreated", "dateModified", "dateValid", "dateOther", "edition", "issuance",
        "frequency", "relatedItem", "language", "physicalDescription", "abstract", "note", "subject", "classification",
        "location", "shelfLocator", "url", "accessCondition", "part", "extension", "recordInfo" };

    private final static List<String> orderList = Arrays.asList(order);

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        href = href.substring(href.indexOf(":") + 1);
        Element mods = MCRURIResolver.instance().resolve(href);
        MODSSorter.sort(mods);
        return new JDOMSource(mods);
    }

    public static void sort(Element mods) {
        mods.sortChildren((Element e1, Element e2) -> compare(e1, e2));
    }

    private static int compare(Element e1, Element e2) {
        int pos1 = getPos(e1);
        int pos2 = getPos(e2);

        if (pos1 == pos2)
            return e1.getName().compareTo(e2.getName());
        else
            return pos1 - pos2;
    }

    private static int getPos(Element e) {
        String name = e.getName();
        return orderList.contains(name) ? orderList.indexOf(name) : orderList.size();
    }
}