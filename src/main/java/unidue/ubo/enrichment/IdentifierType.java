/**
 * Copyright (c) 2016 Duisburg-Essen University Library
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package unidue.ubo.enrichment;

import java.util.List;

import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.common.MCRConstants;

class IdentifierType {

    private String typeID;

    private XPathExpression<Element> xPath;

    public IdentifierType(String typeID, String xPath) {
        this.typeID = typeID;
        this.xPath = XPathFactory.instance().compile(xPath, Filters.element(), null,
            MCRConstants.getStandardNamespaces());
    }

    public String getTypeID() {
        return typeID;
    }

    public List<Element> findIdentifiers(Element mods) {
        return xPath.evaluate(mods);
    }
    
    @Override
    public String toString() {
        return "identifier type " + typeID;
    }
}