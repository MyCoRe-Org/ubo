package org.mycore.ubo.modsperson;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.junit.Test;
import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRConstants;
import org.mycore.common.MCRStoreTestCase;
import org.mycore.common.content.MCRURLContent;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.common.MCRLinkTableManager;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.metadata.MCRObjectMetadataTest;
import org.mycore.mods.MCRMODSWrapper;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class MODSPersonEventHandlerTest extends MCRStoreTestCase {

    @Test
    public void testHandleObjectUpdated() throws IOException, JDOMException, MCRAccessException {
        URL url1 = MCRObjectMetadataTest.class.getResource(
            "/MODSPersonEventHandlerTest/junit_modsperson_00000016.xml");
        Document doc1 = new MCRURLContent(url1).asXML();
        MCRObject person1 = new MCRObject(doc1);

        MCRMetadataManager.create(person1);

        MCRMODSWrapper wrapper = new MCRMODSWrapper(person1);
        Element modsName = wrapper.getMODS().getChild("name", MCRConstants.MODS_NAMESPACE);
        Element familyName = modsName.getChildren("namePart", MCRConstants.MODS_NAMESPACE)
            .stream()
            .filter(el -> "family".equals(el.getAttributeValue("type"))).findFirst().get();
        familyName.setText("Meyer");

        Set<MODSPersonLookup.PersonCache> foundPersons = MODSPersonLookup.lookup(modsName);
        assertNull(foundPersons);

        MCRMetadataManager.update(person1);

        foundPersons = MODSPersonLookup.lookup(modsName);
        assertNotNull(foundPersons);
        assertEquals(1, foundPersons.size());
        assertEquals("Meyer", foundPersons.iterator().next().getFamilyName());
        assertEquals("Peter", foundPersons.iterator().next().getGivenName());
        assertEquals("99999|lsf", foundPersons.iterator().next().getKeys().iterator().next());

        familyName.setText("Müller");
        foundPersons = MODSPersonLookup.lookup(modsName);
        assertNull(foundPersons);
    }

    @Test
    public void testHandleObjectDeleted() throws IOException, JDOMException, MCRAccessException,
        MCRActiveLinkException {
        MCRLinkTableManager linkTableManager = MCRLinkTableManager.getInstance();

        URL url1 = MCRObjectMetadataTest.class.getResource(
            "/MODSPersonEventHandlerTest/junit_modsperson_00000016.xml");
        Document doc1 = new MCRURLContent(url1).asXML();
        MCRObject person1 = new MCRObject(doc1);

        URL url2 = MCRObjectMetadataTest.class.getResource(
            "/MODSPersonEventHandlerTest/junit_mods_00000017.xml");
        Document doc2 = new MCRURLContent(url2).asXML();
        MCRObject obj1 = new MCRObject(doc2);

        MCRMODSWrapper wrapper = new MCRMODSWrapper(obj1);
        Element modsName = wrapper.getMODS().getChild("name", MCRConstants.MODS_NAMESPACE);
        assertNotNull(modsName.getAttribute("href", MCRConstants.XLINK_NAMESPACE));

        MCRMetadataManager.create(person1);
        MCRMetadataManager.create(obj1);

        assertEquals(1, linkTableManager.countReferenceLinkTo("junit_modsperson_00000016"));
        List<String> destinations = (List) linkTableManager
            .getDestinationOf("junit_mods_00000017", "reference");
        assertEquals(1, destinations.size());
        assertEquals("junit_modsperson_00000016", destinations.getFirst());

        assertThrows(MCRActiveLinkException.class, () -> MCRMetadataManager.delete(person1));

        linkTableManager.deleteReferenceLink(obj1.getId());
        MCRMetadataManager.delete(person1);

        assertEquals(0, linkTableManager.countReferenceLinkTo("junit_modsperson_00000016"));
        destinations = (List) linkTableManager
            .getDestinationOf("junit_mods_00000017", "reference");
        assertEquals(0, destinations.size());

        obj1 = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance("junit_mods_00000017"));
        assertNotNull(obj1);

        wrapper = new MCRMODSWrapper(obj1);
        modsName = wrapper.getMODS().getChild("name", MCRConstants.MODS_NAMESPACE);
        assertNull(modsName.getAttribute("href", MCRConstants.XLINK_NAMESPACE));

        // TODO delete person1 and check for no error
    }
}
