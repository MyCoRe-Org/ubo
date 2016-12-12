package unidue.ubo.merger;

import org.junit.Test;
import org.mycore.common.MCRHibTestCase;

public class TestRelatedItemMerger extends MCRHibTestCase {

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
