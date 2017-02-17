/**
 * Copyright (c) 2016 Duisburg-Essen University Library
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package unidue.ubo.enrichment;

import org.jdom2.Element;
import org.junit.Ignore;
import org.junit.Test;
import org.mycore.common.MCRJPATestCase;
import org.mycore.common.xml.MCRNodeBuilder;

import unidue.ubo.enrichment.EnrichmentResolver;

public class TestEnrichmentResolver extends MCRJPATestCase {

    @Ignore
    public void test() throws Exception {
        Element mods = new MCRNodeBuilder()
            .buildElement("mods:mods[mods:identifier[@type='doi']='10.1016/0009-2614(86)80016-1 ']", null, null);
        new EnrichmentResolver().enrichPublication(mods, "import");
    }
}
