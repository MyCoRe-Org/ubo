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

public class TestOriginInfoMerger extends MCRJPATestCase {

    @Test
    public void testMerge() throws Exception {
        String a = "[mods:originInfo[mods:dateIssued='2017'][mods:publisher='Elsevier']]";
        String b = "[mods:originInfo[mods:dateIssued[@encoding='w3cdtf']='2017'][mods:edition='4. Aufl.'][mods:place='Berlin']]";
        String e = "[mods:originInfo[mods:dateIssued[@encoding='w3cdtf']='2017'][mods:publisher='Elsevier'][mods:edition='4. Aufl.'][mods:place='Berlin']]";
        TestMerger.test(a, b, e);
    }

    @Test
    public void testDateOther() throws Exception {
        String a = "[mods:originInfo[mods:dateOther[@type='accepted']='2017']]";
        String b = "[mods:originInfo[mods:dateOther[@type='submitted']='2018']]";
        String e = "[mods:originInfo[mods:dateOther[@type='accepted']='2017'][mods:dateOther[@type='submitted']='2018']]";
        TestMerger.test(a, b, e);
    }
}
