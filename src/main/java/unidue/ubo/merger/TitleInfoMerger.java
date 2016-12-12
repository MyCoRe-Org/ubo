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

public class TitleInfoMerger extends Merger {

    private String text;

    public void setElement(Element element) {
        super.setElement(element);

        text = textOf("nonSort") + " " + textOf("title") + " " + textOf("subTitle");
        text = DeDupCriteriaBuilder.normalizeText(text.trim());
    }

    @Override
    public boolean isProbablySameAs(Merger other) {
        if (!(other instanceof TitleInfoMerger))
            return false;
        else
            return text.equals(((TitleInfoMerger) other).text);
    }

    public void mergeFrom(Merger other) {
        mergeAttributes(other);

        // if the other one has a subTitle and we don't, the other one wins
        if (textOf("subTitle").isEmpty() && !((TitleInfoMerger) other).textOf("subTitle").isEmpty()) {
            this.element.setContent(other.element.cloneContent());
        }
    }

    private String textOf(String childName) {
        String text = element.getChildText(childName, MCRConstants.MODS_NAMESPACE);
        return text == null ? "" : text.trim();
    }
}
