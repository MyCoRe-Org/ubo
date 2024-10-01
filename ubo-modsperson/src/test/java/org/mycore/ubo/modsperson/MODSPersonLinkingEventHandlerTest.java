package org.mycore.ubo.modsperson;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRConstants;
import org.mycore.common.MCRStoreTestCase;
import org.mycore.common.content.MCRURLContent;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.metadata.MCRObjectMetadataTest;
import org.mycore.mods.MCRMODSWrapper;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MODSPersonLinkingEventHandlerTest extends MCRStoreTestCase {

    private static boolean isInitialized = false;

    @Before
    public void init() throws IOException, JDOMException, MCRAccessException {
        if (!isInitialized) {
            URL url1 = MCRObjectMetadataTest.class.getResource("/MODSPersonLookupTest/junit_mods_00000001.xml");
            Document doc1 = new MCRURLContent(url1).asXML();
            MCRObject obj1 = new MCRObject(doc1);

            URL url2 = MCRObjectMetadataTest.class.getResource("/MODSPersonLookupTest/junit_mods_00000002.xml");
            Document doc2 = new MCRURLContent(url2).asXML();
            MCRObject obj2 = new MCRObject(doc2);

            URL url3 = MCRObjectMetadataTest.class.getResource("/MODSPersonLookupTest/junit_mods_00000003.xml");
            Document doc3 = new MCRURLContent(url3).asXML();
            MCRObject obj3 = new MCRObject(doc3);

            MCRMetadataManager.create(obj1);
            MCRMetadataManager.create(obj2);
            MCRMetadataManager.create(obj3);
            isInitialized = true;
        }
    }

    @Test
    @Ignore
    public void testHandleCreate() {

        MCRObject person1 = MCRMetadataManager.retrieveMCRObject(MCRObjectID
            .getInstance("junit_modsperson_00000001"));
        assertNotNull(person1);
        assertPerson(person1, "Müller", "Lisa", "11111", "1112222333");

        MCRObject person2 = MCRMetadataManager.retrieveMCRObject(MCRObjectID
            .getInstance("junit_modsperson_00000002"));
        assertNotNull(person2);
        assertPerson(person2, "Meyer", "Lena", "12345", "1112222444");

        MCRObject person3 = MCRMetadataManager.retrieveMCRObject(MCRObjectID
            .getInstance("junit_modsperson_00000003"));
        assertNotNull(person3);
        assertPerson(person3, "Müller", "Luisa", "22222", "444555666");
    }

    @Test
    public void testHandleCreateWithLookup() {

        MCRObject mods1 = MCRMetadataManager.retrieveMCRObject(MCRObjectID
            .getInstance("junit_mods_00000001"));
        MCRObject mods2 = MCRMetadataManager.retrieveMCRObject(MCRObjectID
            .getInstance("junit_mods_00000001"));
        MCRObject mods3 = MCRMetadataManager.retrieveMCRObject(MCRObjectID
            .getInstance("junit_mods_00000001"));

        MODSPersonLookup.add(mods1);
        MODSPersonLookup.add(mods2);
        MODSPersonLookup.add(mods3);

        MCRObject person1 = MCRMetadataManager.retrieveMCRObject(MCRObjectID
            .getInstance("junit_modsperson_00000001"));
        assertNotNull(person1);
        assertPerson(person1, "Müller", "Lisa", "11111", "1112222333");

        MCRObject person2 = MCRMetadataManager.retrieveMCRObject(MCRObjectID
            .getInstance("junit_modsperson_00000002"));
        assertNotNull(person2);
        assertPerson(person2, "Meyer", "Lena", "12345", "1112222444");

        MCRObject person3 = MCRMetadataManager.retrieveMCRObject(MCRObjectID
            .getInstance("junit_modsperson_00000003"));
        assertNotNull(person3);
        assertPerson(person3, "Müller", "Luisa", "22222", "444555666");
    }

    /**
     * Assert that the Object contains the two name parts and an LSF and SCOPUS ID
     * @param person tested {@link MCRObject}
     * @param familyName the expected family name
     * @param givenName the expected given name
     * @param lsfId the expected LSF ID
     * @param scopusId the expected Scopus ID
     */
    private void assertPerson(MCRObject person, String familyName,
        String givenName, String lsfId, String scopusId) {

        MCRMODSWrapper wrapper = new MCRMODSWrapper(person);
        List<Element> nameElements = wrapper.getMODS().getChild("name", MCRConstants.MODS_NAMESPACE)
            .getChildren();
        List<Element> familyNameElements = nameElements.stream()
            .filter(e -> "family".equals(e.getAttributeValue("type")))
            .toList();

        assertEquals(1, familyNameElements.size());
        assertEquals(familyName, familyNameElements.get(0).getText());

        List<Element> givenNameElements = nameElements.stream()
            .filter(e -> "given".equals(e.getAttributeValue("type")))
            .toList();

        assertEquals(1, givenNameElements.size());
        assertEquals(givenName, givenNameElements.get(0).getText());

        List<Element> lsfElements = nameElements.stream()
            .filter(e -> "lsf".equals(e.getAttributeValue("type")))
            .toList();

        assertEquals(1, lsfElements.size());
        assertEquals(lsfId, lsfElements.get(0).getText());

        List<Element> scopusElements = nameElements.stream()
            .filter(e -> "scopus".equals(e.getAttributeValue("type")))
            .toList();

        assertEquals(1, scopusElements.size());
        assertEquals(scopusId, scopusElements.get(0).getText());
    }
}
