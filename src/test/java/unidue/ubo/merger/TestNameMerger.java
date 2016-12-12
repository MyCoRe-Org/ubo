/**
 * Copyright (c) 2016 Duisburg-Essen University Library
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package unidue.ubo.merger;

import org.jaxen.JaxenException;
import org.jdom2.Element;
import org.junit.Test;
import org.mycore.common.MCRHibTestCase;
import org.mycore.frontend.xeditor.MCRNodeBuilder;

import unidue.ubo.merger.NameMerger;

import static org.junit.Assert.*;

import java.io.IOException;

public class TestNameMerger extends MCRHibTestCase {

    @Test
    public void testIsProbablySameAs() throws Exception {
        NameMerger a = buildNameEntry("[mods:namePart='Thomas Müller']");
        NameMerger b = buildNameEntry("[mods:namePart='thomas Mueller']");
        assertTrue(a.isProbablySameAs(b));

        NameMerger c = buildNameEntry("[mods:namePart='Muller, T.']");
        assertTrue(a.isProbablySameAs(c));

        NameMerger d = buildNameEntry("[mods:namePart='Mueller, T']");
        assertTrue(a.isProbablySameAs(d));

        NameMerger e = buildNameEntry("[mods:namePart='Müller, Egon']");
        assertFalse(a.isProbablySameAs(e));

        NameMerger f = buildNameEntry("[mods:namePart='Thorsten Mueller']");
        assertTrue(c.isProbablySameAs(f));
        assertFalse(a.isProbablySameAs(f));

        NameMerger g = buildNameEntry("[mods:namePart='Thorsten Egon Mueller']");
        assertTrue(e.isProbablySameAs(g));
        assertTrue(f.isProbablySameAs(g));

        NameMerger h = buildNameEntry(
            "[mods:namePart[@type='given']='Thomas'][mods:namePart[@type='family']='Müller']");
        assertTrue(h.isProbablySameAs(a));
        assertTrue(h.isProbablySameAs(d));

        NameMerger i = buildNameEntry(
            "[mods:namePart[@type='given']='T.'][mods:namePart[@type='family']='Müller'][mods:namePart[@type='termsOfAddress']='Jun.']");
        assertTrue(i.isProbablySameAs(h));
        assertTrue(i.isProbablySameAs(a));
        assertTrue(i.isProbablySameAs(d));
    }

    @Test
    public void testCompareBasedOnDisplayForm() throws Exception {
        NameMerger a = buildNameEntry("[mods:displayForm='Thomas Müller']");
        NameMerger b = buildNameEntry(
            "[mods:namePart[@type='given']='Thomas'][mods:namePart[@type='family']='Müller']");
        assertTrue(a.isProbablySameAs(b));
    }

    @Test
    public void testMergeTermsOfAddress() throws JaxenException, IOException {
        String a = "[mods:name[@type='personal'][mods:namePart[@type='given']='Thomas'][mods:namePart[@type='family']='Müller']]";
        String b = "[mods:name[@type='personal'][mods:namePart[@type='given']='T.'][mods:namePart[@type='family']='Müller'][mods:namePart[@type='termsOfAddress']='Jun.']]";
        String e = "[mods:name[@type='personal'][mods:namePart[@type='given']='Thomas'][mods:namePart[@type='family']='Müller'][mods:namePart[@type='termsOfAddress']='Jun.']]";
        TestMerger.test(a, b, e);
    }

    @Test
    public void testMergeNameIdentifier() throws JaxenException, IOException {
        String a = "[mods:name[@type='personal'][mods:namePart='Thomas Müller'][mods:nameIdentifier[@type='gnd']='2']]";
        String b = "[mods:name[@type='personal'][mods:namePart='Mueller, T'][mods:nameIdentifier[@type='lsf']='1'][mods:nameIdentifier[@type='gnd']='2']]";
        String e = "[mods:name[@type='personal'][mods:namePart='Thomas Müller'][mods:nameIdentifier[@type='gnd']='2'][mods:nameIdentifier[@type='lsf']='1']]";
        TestMerger.test(a, b, e);
    }

    @Test
    public void testMergeDisplayForm() throws JaxenException, IOException {
        String a = "[mods:name[@type='personal'][mods:namePart='Thomas Müller'][mods:displayForm='Tommy']]";
        String b = "[mods:name[@type='personal'][mods:namePart='Mueller, T'][mods:displayForm='Tom']]";
        String e = a;
        TestMerger.test(a, b, e);
    }

    @Test
    public void testMergeSubElements() throws JaxenException, IOException {
        String a = "[mods:name[@type='personal'][mods:namePart='Thomas Müller'][mods:affiliation='UDE']]";
        String b = "[mods:name[@type='personal'][mods:namePart='Mueller, T'][mods:affiliation='UB der UDE'][mods:nameIdentifier[@type='gnd']='2']]";
        String e = "[mods:name[@type='personal'][mods:namePart='Thomas Müller'][mods:affiliation='UDE'][mods:affiliation='UB der UDE'][mods:nameIdentifier[@type='gnd']='2']]";
        TestMerger.test(a, b, e);
    }

    @Test
    public void testMergeFirstLastVsLastFirst() throws JaxenException, IOException {
        String a = "[mods:name[@type='personal'][mods:namePart='Thomas Müller']]";
        String b = "[mods:name[@type='personal'][mods:namePart='Mueller, T']]";
        String e = a;
        TestMerger.test(a, b, e);
    }

    @Test
    public void testPreferFamilyGiven() throws JaxenException, IOException {
        String a = "[mods:name[@type='personal'][mods:namePart='Thomas Müller']]";
        String b = "[mods:name[@type='personal'][mods:namePart[@type='family']='Müller'][mods:namePart[@type='given']='T.']]";
        String e = b;
        TestMerger.test(a, b, e);
    }

    private NameMerger buildNameEntry(String predicates) throws JaxenException {
        Element modsName = new MCRNodeBuilder().buildElement("mods:name[@type='personal']" + predicates, null, null);
        NameMerger ne = new NameMerger();
        ne.setElement(modsName);
        return ne;
    }
}
