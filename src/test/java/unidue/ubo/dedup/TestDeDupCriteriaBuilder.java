package unidue.ubo.dedup;

import org.junit.Test;

import junit.framework.TestCase;
import unidue.ubo.dedup.DeDupCriteriaBuilder;
import unidue.ubo.dedup.DeDupCriterion;

public class TestDeDupCriteriaBuilder extends TestCase {

    private static DeDupCriteriaBuilder builder = new DeDupCriteriaBuilder();

    @Test
    public void testIdentifiers() {
        DeDupCriterion c1 = builder.buildFromIdentifier("duepublico", "123");
        DeDupCriterion c2 = builder.buildFromIdentifier("duepublico", "123");
        assertEquals(c1, c2);
        
        DeDupCriterion c3 = builder.buildFromIdentifier("doi", "10.1002/0470841559.ch1");
        assertFalse(c1.equals(c3));
        
        DeDupCriterion c4 = builder.buildFromIdentifier("isbn", "978-1-56619-909-4" );
        DeDupCriterion c5 = builder.buildFromIdentifier("isbn", "9781566199094" );
        assertEquals(c4, c5);
    }
    
    @Test
    public void testTitles() {
        DeDupCriterion c0 = builder.buildFromTitleAuthor("A different short title","Meier");
        DeDupCriterion c1 = builder.buildFromTitleAuthor("This is a short title","Meier");
        DeDupCriterion c2 = builder.buildFromTitleAuthor("THIS is a SHORT Title","Meier");
        assertFalse(c0.equals(c1));
        assertEquals(c1, c2);

        DeDupCriterion c3 = builder.buildFromTitleAuthor("Der Moiré-Effekt im Zusammenhang mit Hühneraugen","Meier");
        DeDupCriterion c4 = builder.buildFromTitleAuthor("Der Moire-Effekt im Zusammenhang mit Huehneraugen","Meier");
        assertEquals(c3, c4);

        DeDupCriterion c5 = builder.buildFromTitleAuthor("Horizon-Report 2015","Meier");
        DeDupCriterion c6 = builder.buildFromTitleAuthor("Horizon-Report 2016","Meier");
        assertFalse(c5.equals(c6));

        DeDupCriterion c7 = builder.buildFromTitleAuthor("Hier ist Augenmaß gefragt","Meier");
        DeDupCriterion c8 = builder.buildFromTitleAuthor("Hier ist Augenmass gefragt","Meier");
        assertEquals(c7, c8);
    }
}
