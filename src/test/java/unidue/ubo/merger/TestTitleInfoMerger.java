/**
 * Copyright (c) 2016 Duisburg-Essen University Library
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package unidue.ubo.merger;

import java.io.IOException;

import org.jaxen.JaxenException;
import org.junit.Test;
import org.mycore.common.MCRJPATestCase;

public class TestTitleInfoMerger extends MCRJPATestCase {

    @Test
    public void testMerge() throws Exception {
        String a = "[mods:titleInfo[mods:title='Testing'][mods:subTitle='All You have to know about']]";
        String b = "[mods:titleInfo[mods:title='testing: all you have to know about']]";
        String e = "[mods:titleInfo[mods:title='Testing'][mods:subTitle='All You have to know about']]";
        TestMerger.test(a, b, e);
    }

    @Test
    public void testMergingTitleSubtitle() throws JaxenException, IOException {
        String a = "[mods:titleInfo[mods:title='testing: all you have to know about']]";
        String b = "[mods:titleInfo[mods:title='Testing'][mods:subTitle='All You have to know about']]";
        String e = b;
        TestMerger.test(a, b, e);
    }

    @Test
    public void testMergingAttributes() throws JaxenException, IOException {
        String a = "[mods:titleInfo[mods:title='first'][@xml:lang='de']][mods:titleInfo[mods:title='second']]";
        String b = "[mods:titleInfo[mods:title='first']][mods:titleInfo[mods:title='second'][@xml:lang='en'][@type='alternative']]";
        String e = "[mods:titleInfo[mods:title='first'][@xml:lang='de']][mods:titleInfo[mods:title='second'][@xml:lang='en'][@type='alternative']]";
        TestMerger.test(a, b, e);
    }

    @Test
    public void testMergingDifferent() throws JaxenException, IOException {
        String a = "[mods:titleInfo[mods:title='a']]";
        String b = "[mods:titleInfo[mods:title='b']]";
        String e = "[mods:titleInfo[mods:title='a']][mods:titleInfo[mods:title='b']]";
        TestMerger.test(a, b, e);
    }

    @Test
    public void testMergingIdentical() throws JaxenException, IOException {
        String a = "[mods:titleInfo[mods:title='test']]";
        String b = a;
        String e = a;
        TestMerger.test(a, b, e);
    }
}
