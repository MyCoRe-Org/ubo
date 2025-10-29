package org.mycore.ubo.modsperson.linking;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.junit.Test;
import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRStoreTestCase;
import org.mycore.common.content.MCRURLContent;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.metadata.MCRObjectMetadataTest;
import org.mycore.mods.MCRMODSWrapper;
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUserManager;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import static org.junit.Assert.*;
import static org.mycore.common.MCRConstants.MODS_NAMESPACE;

public class MODSPersonORCIDLinkingEventHandlerTest extends MCRStoreTestCase {

    @Test
    public void testDoHandleEvent() throws IOException, JDOMException, MCRAccessException {
        URL url = MCRObjectMetadataTest.class.getResource(
            "/MODSPersonORCIDLinkingEventHandlerTest/junit_modsperson_00000018.xml");
        Document doc1 = new MCRURLContent(url).asXML();
        MCRObject person1 = new MCRObject(doc1);
        MCRMetadataManager.create(person1);

        MCRUser mcrUser = new MCRUser("userName", "local");
        mcrUser.setUserAttribute("id_modsperson", "junit_modsperson_00000018");
        mcrUser.setUserAttribute("orcid_credential_0000-0000-0000-0001", "{}");
        mcrUser.setUserAttribute("orcid_credential_0000-0000-0000-0002", "{}");

        MCRUserManager.createUser(mcrUser);

        MCRObject modsperson = MCRMetadataManager.retrieveMCRObject(MCRObjectID
            .getInstance("junit_modsperson_00000018"));
        MCRMODSWrapper wrapper = new MCRMODSWrapper(modsperson);
        Element name = wrapper.getElement("mods:name[@type='personal']");
        List<Element> nameIdentifiers = name.getChildren("nameIdentifier", MODS_NAMESPACE);
        assertEquals(3, nameIdentifiers.size());
        assertTrue(nameIdentifiers.stream().anyMatch(e ->
            "lsf".equals(e.getAttributeValue("type")) && "123456".equals(e.getText())
        ));
        assertTrue(nameIdentifiers.stream().anyMatch(e ->
            "orcid".equals(e.getAttributeValue("type")) && "0000-0000-0000-0001".equals(e.getText())
        ));
        assertTrue(nameIdentifiers.stream().anyMatch(e ->
            "orcid".equals(e.getAttributeValue("type")) && "0000-0000-0000-0002".equals(e.getText())
        ));

    }
}
