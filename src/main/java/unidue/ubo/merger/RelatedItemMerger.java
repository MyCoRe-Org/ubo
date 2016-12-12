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

public class RelatedItemMerger extends Merger {

    public void setElement(Element element) {
        super.setElement(element);
    }

    @Override
    public boolean isProbablySameAs(Merger other) {
        if (!(other instanceof RelatedItemMerger))
            return false;
        else
            return isRelatedItemTypeHost() && ((RelatedItemMerger) other).isRelatedItemTypeHost();
    }

    private boolean isRelatedItemTypeHost() {
        return "host".equals(element.getAttributeValue("type"));
    }
}
