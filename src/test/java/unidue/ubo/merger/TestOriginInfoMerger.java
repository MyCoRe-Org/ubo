package unidue.ubo.merger;

import org.junit.Test;
import org.mycore.common.MCRHibTestCase;

public class TestOriginInfoMerger extends MCRHibTestCase {

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
