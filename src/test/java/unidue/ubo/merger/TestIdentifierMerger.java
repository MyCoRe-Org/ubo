package unidue.ubo.merger;

import org.junit.Test;
import org.mycore.common.MCRHibTestCase;

public class TestIdentifierMerger extends MCRHibTestCase {

    @Test
    public void testMergeDifferent() throws Exception {
        String a = "[mods:identifier[@type='doi']='10.123/456']";
        String b = "[mods:identifier[@type='issn']='1234-5678']";
        String e = "[mods:identifier[@type='doi']='10.123/456'][mods:identifier[@type='issn']='1234-5678']";
        TestMerger.test(a, b, e);
    }

    @Test
    public void testMergeSame() throws Exception {
        String a = "[mods:identifier[@type='issn']='12345678']";
        String b = "[mods:identifier[@type='issn']='1234-5678']";
        String e = b;
        TestMerger.test(a, b, e);
    }
}
