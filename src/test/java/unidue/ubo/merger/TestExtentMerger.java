package unidue.ubo.merger;

import org.junit.Test;
import org.mycore.common.MCRHibTestCase;

public class TestExtentMerger extends MCRHibTestCase {

    @Test
    public void testPhysicalDescriptionExtent() throws Exception {
        String a = "[mods:physicalDescription[mods:extent='360 pages']]";
        String b = "[mods:physicalDescription[mods:extent='7\" x 9\"']]";
        TestMerger.test(a, b, a);
    }

    @Test
    public void testPartExtentList() throws Exception {
        String a = "[mods:part[mods:extent[@unit='pages'][mods:list='S. 64-67']]]";
        String b = "[mods:part[mods:extent[@unit='pages'][mods:list='pp. 64-67']]]";
        TestMerger.test(a, b, a);
    }

    @Test
    public void testPartExtentStartEnd() throws Exception {
        String a = "[mods:part[mods:extent[@unit='pages'][mods:list='S. 64-67']]]";
        String b = "[mods:part[mods:extent[@unit='pages'][mods:start='64'][mods:end='67']]]";
        TestMerger.test(a, b, b);
        TestMerger.test(b, a, b);
    }
}
