/**
 * Copyright (c) 2016 Duisburg-Essen University Library
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package unidue.ubo.merger;

import org.jdom2.Element;
import org.mycore.common.MCRConstants;

import unidue.ubo.dedup.DeDupCriteriaBuilder;

public class AbstractMerger extends Merger {

    private String text;

    public void setElement(Element element) {
        super.setElement(element);
        this.text = DeDupCriteriaBuilder.normalizeText(element.getText());
        this.text += element.getAttributeValue("href", MCRConstants.XLINK_NAMESPACE, "");
    }

    @Override
    public boolean isProbablySameAs(Merger other) {
        if (!(other instanceof AbstractMerger))
            return false;
        else
            return text.equals(((AbstractMerger) other).text);
    }
}
