/**
 * $Revision: 36492 $ 
 * $Date: 2016-11-11 17:31:02 +0100 (Fr, 11 Nov 2016) $
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

package unidue.ubo.merger;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.common.xml.MCRXMLHelper;

public class Merger {

    protected Element element;

    public void setElement(Element element) {
        this.element = element;
    }

    public boolean isProbablySameAs(Merger other) {
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Merger)
            return MCRXMLHelper.deepEqual(this.element, ((Merger) obj).element);
        else
            return super.equals(obj);
    }

    public void mergeFrom(Merger other) {
        mergeAttributes(other);
        mergeElements(other);
    }

    protected void mergeAttributes(Merger other) {
        for (Attribute attribute : other.element.getAttributes()) {
            if (this.element.getAttribute(attribute.getName(), attribute.getNamespace()) == null)
                this.element.setAttribute(attribute.clone());
        }
    }

    protected void mergeElements(Merger other) {
        List<Merger> entries = new ArrayList<Merger>();
        for (Element child : this.element.getChildren())
            entries.add(MergerFactory.buildFrom(child));

        for (Element child : other.element.getChildren())
            mergeIntoExistingEntries(entries, MergerFactory.buildFrom(child));
    }

    private void mergeIntoExistingEntries(List<Merger> entries, Merger newEntry) {
        for (Merger existingEntry : entries) {
            if (newEntry.equals(existingEntry))
                return;
            else if (newEntry.isProbablySameAs(existingEntry)) {
                existingEntry.mergeFrom(newEntry);
                return;
            }
        }
        entries.add(newEntry);
        element.addContent(newEntry.element.clone());
    }

    /** Holds the MODS namespace */
    private static List<Namespace> NS = new ArrayList<Namespace>();

    static {
        NS.add(Namespace.getNamespace("mods", "http://www.loc.gov/mods/v3"));
    }

    protected List<Element> getNodes(String xPath) {
        XPathExpression<Element> xPathExpr = XPathFactory.instance().compile(xPath, Filters.element(), null, NS);
        return xPathExpr.evaluate(element);
    }
}