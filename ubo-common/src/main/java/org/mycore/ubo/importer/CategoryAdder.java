/**
 * Copyright (c) 2016 Duisburg-Essen University Library
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package org.mycore.ubo.importer;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.mycore.common.MCRConstants;
import org.mycore.frontend.MCRFrontendUtil;

class CategoryAdder {

    private List<Element> categories = new ArrayList<Element>();

    public CategoryAdder(Element parameters) {
        buildClassificationElement(parameters, "subject", "fachreferate");
        buildClassificationElement(parameters, "origin", "ORIGIN");
        buildClassificationElement(parameters, "project", "project");
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

    public void addCategories(Document publication) {
        Element metadata = publication.getRootElement().getChild("metadata");
        Element mods = metadata.getChild("def.modsContainer").getChild("modsContainer").getChildren().get(0);
        for (Element category : categories) {
            mods.addContent(category.clone());
        }
    }
}
