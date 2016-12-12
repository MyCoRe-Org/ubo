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
import org.mycore.common.config.MCRConfiguration;

public class MergerFactory {

    private static Merger getEntryInstance(String name) {
        String prefix = "UBO.MODSMerger.";
        String defaultClass = MCRConfiguration.instance().getString(prefix + "default");
        return (Merger) (MCRConfiguration.instance().getInstanceOf(prefix + name, defaultClass));
    }

    public static Merger buildFrom(Element element) {
        String name = element.getName();
        Merger entry = getEntryInstance(name);
        entry.setElement(element);
        return entry;
    }
}
