/**
 * Copyright (c) 2016 Duisburg-Essen University Library
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package unidue.ubo.importer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.jdom2.Element;
import org.junit.Test;
import org.mycore.common.MCRConstants;
import org.mycore.common.MCRTestCase;
import org.mycore.common.xml.MCRURIResolver;

public class TestISSNGoldResolver extends MCRTestCase {

    @Test
    public void testGoldISSN() throws IOException {
        Element mods = MCRURIResolver.instance().resolve("gold:2074-9023");

        String title = mods.getChild("titleInfo", MCRConstants.MODS_NAMESPACE)
            .getChildText("title", MCRConstants.MODS_NAMESPACE);
        assertEquals("International journal of information engineering and electronic business", title);

        String uri = mods.getChild("classification", MCRConstants.MODS_NAMESPACE).getAttributeValue("valueURI");
        assertTrue(uri.endsWith("#gold"));
    }

    @Test
    public void testNonGoldISSN() throws IOException {
        Element mods = MCRURIResolver.instance().resolve("gold:1234-5678");
        assertEquals(0, mods.getChildren().size());
    }
}
