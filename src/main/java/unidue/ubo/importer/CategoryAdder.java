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
import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.mycore.common.MCRConstants;
import org.mycore.frontend.MCRFrontendUtil;
import org.xml.sax.SAXException;

class CategoryAdder {

    private List<Element> categories = new ArrayList<Element>();

    public CategoryAdder(Element parameters) {
        buildClassificationElement(parameters, "subject", "fachreferate");
        buildClassificationElement(parameters, "origin", "ORIGIN");
    }

    private void buildClassificationElement(Element parameters, String parameterName, String classificationID) {
        for (Element categoryID : parameters.getChildren(parameterName)) {
            Element classification = new Element("classification", MCRConstants.MODS_NAMESPACE);
            String authorityURI = MCRFrontendUtil.getBaseURL() + "classifications/" + classificationID;
            classification.setAttribute("authorityURI", authorityURI);
            classification.setAttribute("valueURI", authorityURI + "#" + categoryID.getTextTrim());
            categories.add(classification);
        }
    }

    public void addCategories(List<Document> bibentries) throws JDOMException, IOException, SAXException {
        for (Document bibentry : bibentries)
            for (Element category : categories)
                bibentry.getRootElement().getChild("mods", MCRConstants.MODS_NAMESPACE).addContent(category.clone());
    }
}
