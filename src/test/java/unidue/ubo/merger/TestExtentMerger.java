/**
 * Copyright (c) 2016 Duisburg-Essen University Library
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package unidue.ubo.merger;

import org.junit.Test;
import org.mycore.common.MCRJPATestCase;

public class TestExtentMerger extends MCRJPATestCase {

    @Test
    public void testPhysicalDescriptionExtent() throws Exception {
        String a = "[mods:physicalDescription[mods:extent='360 pages']]";
        String b = "[mods:physicalDescription[mods:extent='7\" x 9\"']]";
        TestMerger.test(a, b, a);
    }

    @Test
    public void testPartExtentList() throws Exception {
        String a = "[mods:part[mods:extent[@unit='pages'][mods:list='S. 64-67']]]";
        String b = "[mods:part[mods:extent[@unit='pages'][mods:list='pp. 64-67']]]";
        TestMerger.test(a, b, a);
    }

    @Test
    public void testPartExtentStartEnd() throws Exception {
        String a = "[mods:part[mods:extent[@unit='pages'][mods:list='S. 64-67']]]";
        String b = "[mods:part[mods:extent[@unit='pages'][mods:start='64'][mods:end='67']]]";
        TestMerger.test(a, b, b);
        TestMerger.test(b, a, b);
    }
}
