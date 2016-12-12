/**
 * $Revision: 34413 $ 
 * $Date: 2016-01-26 15:11:20 +0100 (Di, 26 Jan 2016) $
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

package unidue.ubo.dedup;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.common.MCRConstants;

/**
 * Builds deduplication criteria from publication metadata fields or its MODS representation 
 * 
 * @author Frank L\u00FCtzenkirchen
 */
public class DeDupCriteriaBuilder {

    /** Holds the MODS namespace */
    private static List<Namespace> NS = new ArrayList<Namespace>();

    static {
        NS.add(Namespace.getNamespace("mods", "http://www.loc.gov/mods/v3"));
    }

    /**
     * Updates the deduplication criteria stored together with the MODS publication metadata 
     * within the given XML document.
     * 
     * @param entry a &lt;bibentry /&gt; document containing a &lt;mods:mods&gt; child element 
     */
    public void updateDeDupCriteria(Document entry) {
        Element root = entry.getRootElement();
        root.removeChildren("dedup");

        Element mods = root.getChild("mods", MCRConstants.MODS_NAMESPACE);
        for (DeDupCriterion criteria : buildFromMODS(mods))
            root.addContent(criteria.toXML());
    }

    /**
     * Builds deduplication criteria from the given MODS publication metadata
     */
    public Set<DeDupCriterion> buildFromMODS(Element mods) {
        Set<DeDupCriterion> criteria = new HashSet<DeDupCriterion>();

        for (Element identifier : removeEmptyElements(getNodes(mods, "mods:identifier")))
            criteria.add(buildFromIdentifier(identifier));

        for (Element title : getNodes(mods, "mods:titleInfo"))
            for (Element name : getNodes(mods, "mods:name[@type='personal']/mods:namePart[@type='family']"))
                criteria.add(buildFromTitleAuthor(title, name));

        for (Element shelfmark : removeEmptyElements(getNodes(mods, "mods:location/mods:shelfLocator")))
            criteria.add(buildFromShelfmark(shelfmark));

        return criteria;
    }

    /**
     * Helper method to get a list of nodes by specifying an xPath 
     * 
     * @param context the context node which is base for the given xPath
     * @param xPath the xPath expression selecting the nodes
     */
    private List<Element> getNodes(Element context, String xPath) {
        XPathExpression<Element> xPathExpr = XPathFactory.instance().compile(xPath, Filters.element(), null, NS);
        return xPathExpr.evaluate(context);
    }

    /**
     * Given a list of elements, removes those elements from the list that have empty/no text nodes
     */
    private List<Element> removeEmptyElements(List<Element> elements) {
        for (Iterator<Element> iterator = elements.iterator(); iterator.hasNext();)
            if (iterator.next().getTextTrim().isEmpty())
                iterator.remove();
        return elements;
    }

    /**
     * Builds a deduplication criterion from a mods:identifier element
     */
    public DeDupCriterion buildFromIdentifier(Element identifier) {
        String type = identifier.getAttributeValue("type");
        String value = identifier.getTextTrim();
        return buildFromIdentifier(type, value);
    }

    /**
     * Builds a deduplication criterion for a given identifier type and value
     * 
     * @param type the identifier type, e.g. DOI, ISBN, ISSN, URN
     * @param value the value of the identifier, e.g. the DOI
     */
    public DeDupCriterion buildFromIdentifier(String type, String value) {
        value = value.replaceAll("-", "");
        return new DeDupCriterion("identifier", type + ":" + value);
    }

    /**
     * Builds a deduplication criterion for a given mods:shelflocator
     */
    public DeDupCriterion buildFromShelfmark(Element element) {
        return buildFromShelfmark(element.getTextTrim());
    }

    /**
     * Builds a deduplication criterion for a given shelfmark
     */
    public DeDupCriterion buildFromShelfmark(String shelfmark) {
        return new DeDupCriterion("shelfmark", shelfmark);
    }

    /**
     * Builds a combined deduplication criterion from a mods:title and mods:namePart elment.
     * That means for example title and author are combined to find duplicates, and both must match
     * together to identify duplicates. 
     */
    public DeDupCriterion buildFromTitleAuthor(Element modsTitle, Element modsNamePart) {
        String title = getCombinedTitle(modsTitle);
        String author = modsNamePart.getTextTrim();
        return buildFromTitleAuthor(title, author);
    }

    /**
     * Returns a normalized title text from the combined mods:title and mods:subtitle  
     */
    private String getCombinedTitle(Element modsTitle) {
        String mainTitle = modsTitle.getChildTextTrim("title", MCRConstants.MODS_NAMESPACE);
        String subTitle = modsTitle.getChildTextTrim("subTitle", MCRConstants.MODS_NAMESPACE);
        return mainTitle + (subTitle == null ? "" : " " + subTitle);
    }

    /**
     * Builds a combined deduplication criterion from a title and author elment.
     * That means both must match together to identify duplicates. 
     */
    public DeDupCriterion buildFromTitleAuthor(String title, String author) {
        title = normalizeText(title);
        author = normalizeText(author);
        return new DeDupCriterion("ta", author + ": " + title);
    }

    /**
     * Normalizes text to be fault tolerant when matching for duplicates.
     * Accents, umlauts, case are normalized. Punctuation and non-alphabetic/non-digit characters are removed. 
     */
    public static String normalizeText(String text) {
        text = text.toLowerCase();
        text = new HyphenNormalizer().normalize(text).replace("-", " ");
        text = Normalizer.normalize(text, Form.NFD).replaceAll("\\p{M}", ""); // canonical decomposition, then remove accents
        text = text.replace("ue", "u").replace("oe", "o").replace("ae", "a").replace("ß", "s").replace("ss", "s");
        text = text.replaceAll("[^a-z0-9]\\s]", ""); //remove all non-alphabetic characters
        // text = text.replaceAll("\\b.{1,3}\\b", " ").trim(); // remove all words with fewer than four characters
        text = text.replaceAll("\\p{Punct}", " ").trim(); // remove all punctuation
        text = text.replaceAll("\\s+", " "); // normalize whitespace
        return text;
    }
}
