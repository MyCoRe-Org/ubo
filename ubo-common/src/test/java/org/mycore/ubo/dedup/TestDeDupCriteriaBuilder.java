/**
 * Copyright (c) 2016 Duisburg-Essen University Library
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package org.mycore.ubo.dedup;

import org.jdom2.Element;
import org.junit.Test;

import org.mycore.common.MCRConstants;
import org.mycore.common.MCRJPATestCase;

import static org.junit.Assert.*;

public class TestDeDupCriteriaBuilder extends MCRJPATestCase {

    private static DeDupCriteriaBuilder builder = new DeDupCriteriaBuilder();

    @Test
    public void testIdentifiers() {
        DeDupCriterion c1 = builder.buildFromIdentifier("duepublico", "123");
        DeDupCriterion c2 = builder.buildFromIdentifier("duepublico", "123");
        assertEquals(c1, c2);
        
        DeDupCriterion c3 = builder.buildFromIdentifier("doi", "10.1002/0470841559.ch1");
        assertNotEquals(c1, c3);
        
        DeDupCriterion c4 = builder.buildFromIdentifier("isbn", "978-1-56619-909-4" );
        DeDupCriterion c5 = builder.buildFromIdentifier("isbn", "9781566199094" );
        assertEquals(c4, c5);
    }
    
    @Test
    public void testTitles() {
        DeDupCriterion c0 = builder.buildFromTitleAuthor("A different short title","Meier");
        DeDupCriterion c1 = builder.buildFromTitleAuthor("This is a short title","Meier");
        DeDupCriterion c2 = builder.buildFromTitleAuthor("THIS is a SHORT Title","Meier");
        assertNotEquals(c0, c1);
        assertEquals(c1, c2);

        DeDupCriterion c3 = builder.buildFromTitleAuthor("Der Moiré-Effekt im Zusammenhang mit Hühneraugen","Meier");
        DeDupCriterion c4 = builder.buildFromTitleAuthor("Der Moire-Effekt im Zusammenhang mit Huehneraugen","Meier");
        assertEquals(c3, c4);

        DeDupCriterion c5 = builder.buildFromTitleAuthor("Horizon-Report 2015","Meier");
        DeDupCriterion c6 = builder.buildFromTitleAuthor("Horizon-Report 2016","Meier");
        assertNotEquals(c5, c6);

        DeDupCriterion c7 = builder.buildFromTitleAuthor("Hier ist Augenmaß gefragt","Meier");
        DeDupCriterion c8 = builder.buildFromTitleAuthor("Hier ist Augenmass gefragt","Meier");
        assertEquals(c7, c8);
    }

    @Test
    public void testNames() {
        Element firstName1 = createElement("Peter", "given", "namePart");
        Element firstName2 = createElement("Hans", "given", "namePart");
        Element firstName3 = createElement("Hans P.", "given", "namePart");
        Element familyName1 = createElement("Müller", "family", "namePart");
        Element familyName2 = createElement("Mueller", "family", "namePart");
        Element familyName3 = createElement("Mueller-Meyer", "family", "namePart");

        DeDupCriterion c1 = builder.buildFromFullName(firstName1, familyName1);
        DeDupCriterion c2 = builder.buildFromFullName(firstName1, familyName2);
        DeDupCriterion c3 = builder.buildFromFullName(firstName1, familyName3);
        DeDupCriterion c4 = builder.buildFromFullName(firstName2, familyName1);
        DeDupCriterion c5 = builder.buildFromFullName(firstName3, familyName1);

        assertEquals(c1, c2);
        assertNotEquals(c2, c3);
        assertNotEquals(c1, c4);
        assertNotEquals(c4, c5);
    }

    @Test
    public void testNameIdentifiers() {
        Element identifier1 = createElement("12345", "lsf", "nameIdentifier");
        Element identifier2 = createElement("12345", "lsf", "nameIdentifier");
        Element identifier3 = createElement("54321", "lsf", "nameIdentifier");
        Element identifier4 = createElement("12345", "scopus", "nameIdentifier");

        DeDupCriterion c1 = builder.buildFromNameIdentifier(identifier1);
        DeDupCriterion c2 = builder.buildFromNameIdentifier(identifier2);
        DeDupCriterion c3 = builder.buildFromNameIdentifier(identifier3);
        DeDupCriterion c4 = builder.buildFromNameIdentifier(identifier4);

        assertEquals(c1, c2);
        assertNotEquals(c1, c3);
        assertNotEquals(c1, c4);
        assertNotEquals(c3, c4);
    }

    public static Element createElement(String value, String type, String elementName) {
        Element element = new Element(elementName, MCRConstants.MODS_NAMESPACE);
        element.setAttribute("type", type);
        element.setText(value);
        return element;
    }
}
