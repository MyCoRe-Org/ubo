/**
 * Copyright (c) 2016 Duisburg-Essen University Library
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package org.mycore.ubo.dedup.jpa;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.junit.Before;
import org.junit.Test;
import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRStoreTestCase;
import org.mycore.common.content.MCRURLContent;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectMetadataTest;
import org.mycore.ubo.dedup.DeDupCriteriaBuilder;
import org.mycore.ubo.dedup.DeDupCriterion;
import org.mycore.ubo.dedup.PossibleDuplicate;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mycore.ubo.dedup.TestDeDupCriteriaBuilder.createElement;
import static org.mycore.ubo.dedup.jpa.DeduplicationKeyManager.PERSON_CATEGORY_GROUP;
import static org.mycore.ubo.dedup.jpa.DeduplicationKeyManager.SortOrder.*;

public class DeduplicationKeyManagerTest extends MCRStoreTestCase {

    private DeduplicationKeyManager deduplicationKeyManager;
    private DeDupCriteriaBuilder builder;

    @Before
    public void before() throws IOException, JDOMException, MCRAccessException {
        deduplicationKeyManager = new DeduplicationKeyManager();
        builder = new DeDupCriteriaBuilder();
        URL url1 = MCRObjectMetadataTest.class.getResource("/DeduplicationKeyManagerTest/junit_modsperson_00000001.xml");
        Document doc1 = new MCRURLContent(url1).asXML();
        MCRObject obj1 = new MCRObject(doc1);

        URL url2 = MCRObjectMetadataTest.class.getResource("/DeduplicationKeyManagerTest/junit_modsperson_00000002.xml");
        Document doc2 = new MCRURLContent(url2).asXML();
        MCRObject obj2 = new MCRObject(doc2);

        MCRMetadataManager.create(obj1);
        MCRMetadataManager.create(obj2);
    }

    @Test
    public void testGetDuplicatesPerson() {
        List<PossibleDuplicate> dedupKeys = deduplicationKeyManager.getDuplicates(NONE, ASC, PERSON_CATEGORY_GROUP);
        assertEquals(2, dedupKeys.size());
        PossibleDuplicate dup1 = dedupKeys.get(0);
        PossibleDuplicate dup2 = dedupKeys.get(1);
        assertEquals("junit_modsperson_00000001", dup1.getMcrId1());
        assertEquals("junit_modsperson_00000002", dup1.getMcrId2());
        assertEquals("junit_modsperson_00000001", dup2.getMcrId1());
        assertEquals("junit_modsperson_00000002", dup2.getMcrId2());

        Element firstName = createElement("Lisa", "given", "namePart");
        Element familyName = createElement("Müller", "given", "namePart");
        DeDupCriterion nameCriterion = builder.buildFromFullName(firstName, familyName);

        Element identifier1 = createElement("11111", "lsf", "nameIdentifier");
        DeDupCriterion identifierCriterion = builder.buildFromNameIdentifier(identifier1);

        assertEquals(nameCriterion.getKey(), dup1.getDeduplicationKey());
        assertEquals(identifierCriterion.getKey(), dup2.getDeduplicationKey());

        dedupKeys = deduplicationKeyManager.getDuplicates(NONE, DESC, PERSON_CATEGORY_GROUP);
        assertEquals(2, dedupKeys.size());
        dup1 = dedupKeys.get(0);
        dup2 = dedupKeys.get(1);

        assertEquals(nameCriterion.getKey(), dup2.getDeduplicationKey());
        assertEquals(identifierCriterion.getKey(), dup1.getDeduplicationKey());
    }
}
