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
import org.mycore.common.MCRHibTestCase;

public class TestAbstractMerger extends MCRHibTestCase {

    @Test
    public void testMerge() throws Exception {
        String a = "[mods:abstract[@xml:lang='de']='deutsch']";
        String b = "[mods:abstract='deutsch'][mods:abstract[@xml:lang='en']='english']";
        String e = "[mods:abstract[@xml:lang='de']='deutsch'][mods:abstract[@xml:lang='en']='english']";
        TestMerger.test(a, b, e);
    }

    @Test
    public void testXLink() throws Exception {
        String a = "[mods:abstract[@xlink:href='foo']]";
        String b = "[mods:abstract[@xml:lang='de'][@xlink:href='foo']][mods:abstract[@xml:lang='en'][@xlink:href='bar']]";
        String e = b;
        TestMerger.test(a, b, e);
    }
}
