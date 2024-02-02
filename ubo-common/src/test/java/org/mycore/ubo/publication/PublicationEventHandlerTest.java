package org.mycore.ubo.publication;

import org.jdom2.Document;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRStoreTestCase;
import org.mycore.common.content.MCRURLContent;
import org.mycore.common.events.MCREvent;
import org.mycore.common.xml.MCRXMLParserFactory;
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryDAO;
import org.mycore.datamodel.classifications2.MCRCategoryDAOFactory;
import org.mycore.datamodel.classifications2.impl.MCRCategoryDAOImplTest;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectMetadataTest;
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUserAttribute;
import org.mycore.user2.MCRUserManager;
import org.xml.sax.SAXParseException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import static org.junit.Assert.fail;


public class PublicationEventHandlerTest extends MCRStoreTestCase {

    @Before
    public void before() throws SAXParseException, IOException, URISyntaxException {
        MCRCategory groupsCategory = MCRCategoryDAOImplTest.loadClassificationResource("/mcr-roles.xml");
        MCRCategoryDAO DAO = MCRCategoryDAOFactory.getInstance();
        DAO.addCategory(null, groupsCategory);
    }

    /**
     * Tests the behavior of {@link PublicationEventHandler#handleObjectRepaired(MCREvent, MCRObject)},
     * especially the case of incorrectly matched {@link MCRUserAttribute attributes} and
     * non-unique correlation-IDS. See UBO-295.
     * @throws SAXParseException in case of error
     * @throws MCRAccessException in case of error
     */
    @Test
    public void testHandleObjectRepaired() throws SAXParseException, MCRAccessException {
        // Arrange
        URL url1 = MCRObjectMetadataTest.class.getResource("/PublicationEventHandlerTest/junit_mods_00000001.xml");
        Document doc1 = MCRXMLParserFactory.getValidatingParser().parseXML(new MCRURLContent(url1));
        MCRObject obj1 = new MCRObject(doc1);

        URL url2 = MCRObjectMetadataTest.class.getResource("/PublicationEventHandlerTest/junit_mods_00000002.xml");
        Document doc2 = MCRXMLParserFactory.getValidatingParser().parseXML(new MCRURLContent(url2));
        MCRObject obj2 = new MCRObject(doc2);

        URL url3 = MCRObjectMetadataTest.class.getResource("/PublicationEventHandlerTest/junit_mods_00000003.xml");
        Document doc3 = MCRXMLParserFactory.getValidatingParser().parseXML(new MCRURLContent(url3));
        MCRObject obj3 = new MCRObject(doc3);

        MCRMetadataManager.create(obj1);
        MCRMetadataManager.create(obj2);
        MCRMetadataManager.create(obj3);

        // Act
        // tries to add second Connection-ID
        MCRMetadataManager.fireRepairEvent(obj2);

        // Assert
        Assert.assertEquals(2, MCRUserManager.countUsers(null, null, null, null));

        List<MCRUser> users = MCRUserManager.listUsers(null, null, null, null);
        Assert.assertEquals(2, users.size());


        SortedSet<MCRUserAttribute> attributesUserMueller = users.get(0).getRealName().contains("Müller") ?
                users.get(0).getAttributes() :  users.get(1).getAttributes();

        SortedSet<MCRUserAttribute> attributesUserMeyer = users.get(0).getRealName().contains("Meyer") ?
                users.get(0).getAttributes() :  users.get(1).getAttributes();

        Assert.assertEquals(4, attributesUserMueller.size());
        assertSingleAttribute(attributesUserMueller, "id_connection");
        MCRUserAttribute lsf = assertSingleAttribute(attributesUserMueller, "id_lsf");
        Assert.assertEquals(lsf.getValue(), "11111");
        Assert.assertEquals(2, attributesUserMueller.stream()
                .filter(attr -> attr.getName().equals("id_scopus")).toList().size());

        Assert.assertEquals(3, attributesUserMeyer.size());

        assertSingleAttribute(attributesUserMeyer, "id_connection");

        lsf = assertSingleAttribute(attributesUserMeyer, "id_lsf");
        Assert.assertEquals(lsf.getValue(), "12345");

        MCRUserAttribute scopus = assertSingleAttribute(attributesUserMeyer, "id_scopus");
        Assert.assertEquals(scopus.getValue(), "1112222444");

    }

    /**
     * Helper-method to assert that only a single entry of {@link MCRUserAttribute}
     * with a specific key is contained in the given Set.
     * Fails, when more than one key is found. Throws a {@link java.util.NoSuchElementException}
     * when no attribute with the given key is found.
     * @param attributes the given Set of attributes
     * @param attrName the name of the key that should only be contained once in the set
     * @return the single identified attribute
     */
    private MCRUserAttribute assertSingleAttribute(Set<MCRUserAttribute> attributes, String attrName) {
        return attributes.stream()
                .filter(attr -> attr.getName().equals(attrName))
                .reduce((a, b) -> {
                    fail("multiple IDs of type " + attrName + ": " + a.getValue() + ", " + b.getValue());
                    return null;
                }).get();
    }
}

