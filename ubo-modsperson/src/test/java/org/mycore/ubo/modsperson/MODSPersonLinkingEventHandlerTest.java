package org.mycore.ubo.modsperson;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.junit.Test;
import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRConstants;
import org.mycore.common.MCRStoreTestCase;
import org.mycore.common.content.MCRURLContent;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.metadata.MCRObjectMetadataTest;
import org.mycore.frontend.cli.MCRCommandUtils;
import org.mycore.mods.MCRMODSWrapper;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import static org.junit.Assert.*;

public class MODSPersonLinkingEventHandlerTest extends MCRStoreTestCase {

    /**
     * TODO
     * @throws IOException in case of error
     * @throws JDOMException in case of error
     * @throws MCRAccessException in case of error
     */
    @Test
    public void testHandleCreate() throws IOException, JDOMException, MCRAccessException {
        URL url1 = MCRObjectMetadataTest.class.getResource(
            "/MODSPersonLinkingEventHandlerTest/junit_mods_00000001.xml");
        Document doc1 = new MCRURLContent(url1).asXML();
        MCRObject obj1 = new MCRObject(doc1);

        URL url2 = MCRObjectMetadataTest.class.getResource(
            "/MODSPersonLinkingEventHandlerTest/junit_mods_00000002.xml");
        Document doc2 = new MCRURLContent(url2).asXML();
        MCRObject obj2 = new MCRObject(doc2);

        URL url3 = MCRObjectMetadataTest.class.getResource(
            "/MODSPersonLinkingEventHandlerTest/junit_mods_00000003.xml");
        Document doc3 = new MCRURLContent(url3).asXML();
        MCRObject obj3 = new MCRObject(doc3);

        MCRMetadataManager.create(obj1);
        MCRMetadataManager.create(obj2);
        MCRMetadataManager.create(obj3);

        MCRObject person4 = MCRMetadataManager.retrieveMCRObject(MCRObjectID
            .getInstance("junit_modsperson_00000001"));
        assertNotNull(person4);
        assertPerson(person4, "Müller", "Lisa", "77777", "1112222999");

        MCRObject person5 = MCRMetadataManager.retrieveMCRObject(MCRObjectID
            .getInstance("junit_modsperson_00000002"));
        assertNotNull(person5);
        assertPerson(person5, "Meyer", "Lena", "44444", "1112222888");

        MCRObject person6 = MCRMetadataManager.retrieveMCRObject(MCRObjectID
            .getInstance("junit_modsperson_00000003"));
        assertNotNull(person6);
        assertPerson(person6, "Müller", "Luisa", "99999", "444555777");
    }

    /**
     * Tests the case that a lookup returns multiple person-objects. This needs a constellation of test data
     * that is unlikely to occur in production.
     * @throws IOException in case of error
     * @throws JDOMException in case of error
     * @throws MCRAccessException in case of error
     */
    @Test
    public void testHandleCreateDuplicatePerson() throws IOException, JDOMException, MCRAccessException {
        URL url1 = MCRObjectMetadataTest.class.getResource(
            "/MODSPersonLinkingEventHandlerTest/junit_mods_00000004.xml");
        Document doc1 = new MCRURLContent(url1).asXML();
        MCRObject obj1 = new MCRObject(doc1);

        URL url2 = MCRObjectMetadataTest.class.getResource(
            "/MODSPersonLinkingEventHandlerTest/junit_mods_00000005.xml");
        Document doc2 = new MCRURLContent(url2).asXML();
        MCRObject obj2 = new MCRObject(doc2);

        URL url3 = MCRObjectMetadataTest.class.getResource(
            "/MODSPersonLinkingEventHandlerTest/junit_mods_00000006.xml");
        Document doc3 = new MCRURLContent(url3).asXML();
        MCRObject obj3 = new MCRObject(doc3);

        MCRMetadataManager.create(obj1);
        MCRMetadataManager.create(obj2);
        MCRMetadataManager.create(obj3);

        List<String> modspersons = MCRCommandUtils.getIdsForType("modsperson").toList();
        assertEquals(2, modspersons.size());

        MCRObject modsperson1 = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(modspersons.get(0)));
        MCRMODSWrapper wrapper = new MCRMODSWrapper(modsperson1);
        List<String> lsfs = wrapper.getMODS().getChild("name", MCRConstants.MODS_NAMESPACE)
            .getChildren("nameIdentifier", MCRConstants.MODS_NAMESPACE)
            .stream()
            .filter(ni -> "lsf".equals(ni.getAttributeValue("type")))
            .map(Element::getText).toList();

        if (lsfs.size() == 1) {
            assertTrue(lsfs.get(0).equals("77766") || lsfs.get(0).equals("10101"));
        }
        else if (lsfs.size() == 2) {
            assertTrue(lsfs.get(0).equals("77788") || lsfs.get(1).equals("77788"));
        }
        else {
            fail(modsperson1.getId().toString() + " is expected to have either one or two LSF-IDs");
        }

        MCRObject modsperson2 = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(modspersons.get(1)));
        wrapper = new MCRMODSWrapper(modsperson2);
        lsfs = wrapper.getMODS().getChild("name", MCRConstants.MODS_NAMESPACE)
            .getChildren("nameIdentifier", MCRConstants.MODS_NAMESPACE)
            .stream()
            .filter(ni -> "lsf".equals(ni.getAttributeValue("type")))
            .map(Element::getText).toList();

        if (lsfs.size() == 1) {
            assertTrue(lsfs.get(0).equals("77766") || lsfs.get(0).equals("10101"));
        }
        else if (lsfs.size() == 2) {
            assertTrue(lsfs.get(0).equals("77788") || lsfs.get(1).equals("77788"));
        }
        else {
            fail(modsperson2.getId().toString() + " is expected to have either one or two LSF-IDs");
        }
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
