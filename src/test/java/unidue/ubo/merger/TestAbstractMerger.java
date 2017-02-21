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

public class TestAbstractMerger extends MCRJPATestCase {

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

    @Test
    public void testSimilar() throws Exception {
        String a = "[mods:abstract[@xml:lang='de']='Dies ist der deutsche Abstract']";
        String b = "[mods:abstract='Dies ist der deitsche Abstract']";
        String e = a;
        TestMerger.test(a, b, e);

        String a2 = "[mods:abstract[@xml:lang='de']='Dies ist der deutsche Abstract']";
        String b2 = "[mods:abstract='Dieses ist der doitsche Äbschträkt']";
        String e2 = a2 + b2;
        TestMerger.test(a2, b2, e2);
    }
}