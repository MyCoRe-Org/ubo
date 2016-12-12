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

public class ExtentMerger extends Merger {

    public void setElement(Element element) {
        super.setElement(element);
    }

    @Override
    public boolean isProbablySameAs(Merger other) {
        return (other instanceof ExtentMerger);
    }

    private boolean hasStartPage() {
        return element.getChild("start", MCRConstants.MODS_NAMESPACE) != null;
    }

    @Override
    public void mergeFrom(Merger other) {
        if (element.getParentElement().getName().equals("physicalDescription"))
            super.mergeFrom(other);
        else { // parent is "mods:part"
            if ((!this.hasStartPage()) && ((ExtentMerger) other).hasStartPage()) {
                mergeAttributes(other);
                this.element.setContent(other.element.cloneContent());
            }
        }
    }
}
