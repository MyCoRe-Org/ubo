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
import org.jdom2.input.SAXBuilder;
import org.jdom2.transform.JDOMSource;
import org.junit.Test;
import org.mycore.common.MCRStoreTestCase;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.common.content.MCRURLContent;
import org.mycore.common.xml.MCRURIResolver;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectMetadataTest;

import java.net.URL;
import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class DeduplicationCriterionResolverTest extends MCRStoreTestCase {

    public static final String DEDUP_CALL = "dedup:search:person:junit_modsperson_00000001";

    @Override
    public void setUp() throws Exception {
        super.setUp();
        MCRConfiguration2.set("MCR.URIResolver.ModuleResolver.dedup", "org.mycore.ubo.dedup.jpa.DeduplicationCriterionResolver");

        URL url1 = MCRObjectMetadataTest.class.getResource("/DeduplicationKeyManagerTest/junit_modsperson_00000001.xml");
        Document doc1 = new MCRURLContent(url1).asXML();
        MCRObject obj1 = new MCRObject(doc1);

        URL url2 = MCRObjectMetadataTest.class.getResource("/DeduplicationKeyManagerTest/junit_modsperson_00000002.xml");
        Document doc2 = new MCRURLContent(url2).asXML();
        MCRObject obj2 = new MCRObject(doc2);

        URL url3 = MCRObjectMetadataTest.class.getResource("/DeduplicationKeyManagerTest/junit_modsperson_00000003.xml");
        Document doc3 = new MCRURLContent(url3).asXML();
        MCRObject obj3 = new MCRObject(doc3);

        MCRMetadataManager.create(obj1);
        MCRMetadataManager.create(obj2);
        MCRMetadataManager.create(obj3);
    }

    @Test
    public void testDeduplicationCriterionResolverPerson() throws Exception {
        JDOMSource source = (JDOMSource) MCRURIResolver.instance().resolve(DEDUP_CALL, null);
        SAXBuilder builder = new SAXBuilder();
        Document document = builder.build(source.getInputSource());
        List<Element> listSource = document.getRootElement().getChildren();

        assertEquals(2, listSource.size());
        listSource.sort(Comparator.comparing(e -> e.getAttributeValue("id")));
        Element duplicate1 = listSource.get(0);
        assertEquals("junit_modsperson_00000002", duplicate1.getAttributeValue("id"));
        Element duplicate2 = listSource.get(1);
        assertEquals("junit_modsperson_00000003", duplicate2.getAttributeValue("id"));
    }
}
