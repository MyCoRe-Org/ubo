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

public class TestRelatedItemMerger extends MCRJPATestCase {

    @Test
    public void testMergeHost() throws Exception {
        String a = "[mods:relatedItem[@type='host'][mods:identifier='foo']]";
        String b = "[mods:relatedItem[@type='host'][mods:note='bar']]";
        String e = "[mods:relatedItem[@type='host'][mods:identifier='foo'][mods:note='bar']]";
        TestMerger.test(a, b, e);
    }

    @Test
    public void testMergeSeries() throws Exception {
        String a = "[mods:relatedItem[@type='series'][mods:identifier='foo']]";
        String b = "[mods:relatedItem[@type='series'][mods:note='bar']]";
        String e = "[mods:relatedItem[@type='series'][mods:identifier='foo']][mods:relatedItem[@type='series'][mods:note='bar']]";
        TestMerger.test(a, b, e);
    }
}
