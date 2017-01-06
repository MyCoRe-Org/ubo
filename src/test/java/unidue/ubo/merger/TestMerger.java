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
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.Ignore;
import org.junit.Test;
import org.mycore.common.MCRJPATestCase;
import org.mycore.common.xml.MCRXMLHelper;
import org.mycore.common.xml.MCRNodeBuilder;

import unidue.ubo.merger.Merger;
import unidue.ubo.merger.MergerFactory;

import static org.junit.Assert.*;

public class TestMerger extends MCRJPATestCase {

    @Test
    public void testAddingNew() throws Exception {
        String a = "[mods:note[@xml:lang='de']='deutsch']";
        String b = "[mods:note[@xml:lang='de']='deutsch'][mods:note[@xml:lang='en']='english']";
        String e = b;
        test(a, b, e);
    }

    @Test
    public void testJoiningDifferent() throws JaxenException, IOException {
        String a = "[mods:titleInfo[mods:title='test']]";
        String b = "[mods:abstract='abstract']";
        String e = "[mods:titleInfo[mods:title='test']][mods:abstract='abstract']";
        test(a, b, e);
    }

    @Ignore
    static void test(String xPathA, String xPathB, String xPathExpected) throws JaxenException, IOException {
        Element a = new MCRNodeBuilder().buildElement("mods:mods" + xPathA, null, null);
        Element b = new MCRNodeBuilder().buildElement("mods:mods" + xPathB, null, null);
        Element e = new MCRNodeBuilder().buildElement("mods:mods" + xPathExpected, null, null);

        Merger ea = MergerFactory.buildFrom(a);
        Merger eb = MergerFactory.buildFrom(b);
        ea.mergeFrom(eb);
        Element r = ea.element;

        boolean asExpected = MCRXMLHelper.deepEqual(e, r);

        if (!asExpected) {
            System.out.println("actual result:");
            logXML(r);
            System.out.println("expected result:");
            logXML(e);
        }

        assertTrue(asExpected);
    }

    @Ignore
    private static void logXML(Element r) throws IOException {
        System.out.println();
        new XMLOutputter(Format.getPrettyFormat()).output(r, System.out);
        System.out.println();
    }
}
