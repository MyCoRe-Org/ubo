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
