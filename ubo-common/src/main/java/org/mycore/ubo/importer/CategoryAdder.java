/**
 * Copyright (c) 2016 Duisburg-Essen University Library
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package org.mycore.ubo.importer;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.mycore.common.MCRConstants;
import org.mycore.frontend.MCRFrontendUtil;

import java.util.ArrayList;
import java.util.List;

import static org.mycore.common.MCRConstants.XPATH_FACTORY;

class CategoryAdder {

    private List<Element> categories = new ArrayList<Element>();
    private Element status;

    public CategoryAdder(Element parameters) {
        buildClassificationElement(parameters, "partOf", "partOf");
        buildClassificationElement(parameters, "subject", "fachreferate");
        buildClassificationElement(parameters, "origin", "ORIGIN");
        buildClassificationElement(parameters, "project", "project");
        status = parameters.getChild("status");
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

    public void setStatus(Document publication) {
        if (status == null || status.getText().isEmpty()) {
            return;
        }
        Element statusElement = XPATH_FACTORY.compile("//service/servflags/servflag[@type='status']", Filters.element())
            .evaluateFirst(publication);

        if(statusElement != null) {
            statusElement.setText(status.getText());
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
