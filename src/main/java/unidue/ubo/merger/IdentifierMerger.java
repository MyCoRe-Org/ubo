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

public class IdentifierMerger extends Merger {

    public void setElement(Element element) {
        super.setElement(element);
    }

    private String getType() {
        return this.element.getAttributeValue("type", "");
    }

    @Override
    public boolean isProbablySameAs(Merger other) {
        if (!(other instanceof IdentifierMerger))
            return false;
        else
            return this.getType().equals(((IdentifierMerger) other).getType());
    }

    @Override
    public void mergeFrom(Merger other) {
        if (other.element.getText().contains("-"))
            this.element.setText(other.element.getText());
    }
}
