package org.mycore.ubo.modsperson.migration;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.junit.Before;
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
import org.mycore.ubo.modsperson.MODSPersonLookup;
import org.mycore.user2.MCRRealmFactory;
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUserManager;

import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.*;

public class MODSPersonMigrationCommandsTest extends MCRStoreTestCase {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        MODSPersonLookup.clear();
    }

    @Test
    public void testMigration() {
        String userName1 = "test1";
        String userName2 = "test2";

        MCRUser userLocal = new MCRUser(userName1, MCRRealmFactory.getLocalRealm());
        userLocal.setRealName("Tester, Peter");
        userLocal.setUserAttribute("id_connection", "123");
        MCRUser userOther = new MCRUser(userName2, MCRRealmFactory.getRealm("test-realm"));
        userOther.setRealName("Petra Tester");

        MCRUserManager.createUser(userLocal);
        MCRUserManager.createUser(userOther);

        MODSPersonMigrationCommands.migrateModsperson(userName1, "local");
        MODSPersonMigrationCommands.migrateModsperson(userName2, "test-realm");

        userLocal = MCRUserManager.getUser(userName1, "local");
        assertEquals(1, userLocal.getAttributes().size());
        assertEquals(MODSPersonMigrationCommands.CONNECTION_ATTRIBUTE_NAME, userLocal.getAttributes().first().getName());
        assertEquals("123", userLocal.getAttributes().first().getValue());

        userOther = MCRUserManager.getUser(userName2, "test-realm");
        assertEquals(1, userOther.getAttributes().size());
        assertEquals(MODSPersonMigrationCommands.MODSPERSON_ATTRIBUTE_NAME, userOther.getAttributes().first().getName());
        assertTrue(userOther.getAttributes().first().getValue().contains("junit_modsperson_0000"));
        assertEquals(2, MODSPersonLookup.getCacheSize());
    }

    @Test
    public void testMigrationUserNotFound() {
        MODSPersonMigrationCommands.migrateModsperson("userNotFound", "local");
        assertEquals(0, MODSPersonLookup.getCacheSize());
    }

    @Test
    public void testMigrationUserAttributes() {
        String userName = "username";
        MCRUser user = new MCRUser(userName, MCRRealmFactory.getLocalRealm());
        user.setRealName("Tester, Peter");
        user.setLastLogin(Date.from(Instant.now()));
        user.setUserAttribute("id_connection", userName);
        user.setUserAttribute("id_orcid", "000-0001-2345-6789");
        user.setUserAttribute("id_scopus", "00011112222");
        user.setUserAttribute("orcid_credential_000-0001-2345-6789", "{something}");
        user.setUserAttribute("orcid_user_properties_000-0001-2345-6789", "{something}");
        user.setUserAttribute("id_something", "12345");
        user.setUserAttribute("somethingelse", "54321");

        MCRUserManager.createUser(user);

        MODSPersonMigrationCommands.migrateModsperson(userName, "local");
        user = MCRUserManager.getUser(userName, "local");
        assertEquals(8, user.getAttributes().size());
        assertTrue(user.getAttributes().stream().anyMatch(attr -> attr.getName().equals("id_modsperson")));
        String objId = user.getUserAttribute("id_modsperson");

        assertEquals(1, MODSPersonLookup.getCacheSize());

        MCRObject obj = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(objId));
        MCRMODSWrapper wrapper = new MCRMODSWrapper(obj);
        Element personName = wrapper.getElement("mods:name[@type='personal']");

        Set<MODSPersonLookup.PersonCache> persons = MODSPersonLookup.lookup(personName);
        assertNotNull(persons);
        assertEquals(1, persons.size());
        MODSPersonLookup.PersonCache person = persons.iterator().next();
        assertEquals("Peter", person.getGivenName());
        assertEquals("Tester", person.getFamilyName());
        Set<String> personKeys = person.getKeys();
        assertEquals(4, personKeys.size());
        assertTrue(personKeys.contains("000-0001-2345-6789|orcid"));
        assertTrue(personKeys.contains("00011112222|scopus"));
        assertTrue(personKeys.contains("12345|something"));
        assertTrue(personKeys.contains("54321|somethingelse"));
    }

    @Test
    public void testMigrateAll() {
        MCRUser user;
        for (int i = 0; i < 30; i++) {
            String userName = "test" + i;
            user = new MCRUser(userName, MCRRealmFactory.getLocalRealm());
            user.setRealName("Tester, Peter" + i);
            MCRUserManager.createUser(user);
        }

        for (int i = 30; i < 60; i++) {
            String userName = "test" + i;
            user = new MCRUser(userName, MCRRealmFactory.getLocalRealm());
            user.setRealName("Tester, Petra" + i);
            user.setUserAttribute("id_connection", UUID.randomUUID().toString());
            MCRUserManager.createUser(user);
        }

        List<String> commands = MODSPersonMigrationCommands.migrateAllModsperson();
        assertEquals(30, commands.size());
        for (String command : commands) {
            assertTrue(command.contains("modsperson migration migrate user"));
            assertTrue(command.contains("in realm local to modsperson"));
            assertFalse(command.contains("Peter"));
        }
    }

    @Test
    public void testDeleteUsers() {
        MCRUser user;
        // no connection id
        for (int i = 0; i < 10; i++) {
            String userName = "test" + i;
            user = new MCRUser(userName, MCRRealmFactory.getLocalRealm());
            user.setRealName("Tester, Peter" + i);
            user.setUserAttribute("id_connect", UUID.randomUUID().toString());
            user.setLastLogin();
            MCRUserManager.createUser(user);
        }
        // no connection id
        for (int i = 10; i < 20; i++) {
            String userName = "test" + i;
            user = new MCRUser(userName, MCRRealmFactory.getLocalRealm());
            user.setRealName("Tester, Alex" + i);
            user.setLastLogin();
            MCRUserManager.createUser(user);
        }
        // not local realm
        for (int i = 20; i < 30; i++) {
            String userName = "test" + i;
            user = new MCRUser(userName, MCRRealmFactory.getRealm("test-realm"));
            user.setRealName("Tester, Lisa" + i);
            user.setLastLogin();
            user.setUserAttribute("id_connection", UUID.randomUUID().toString());
            MCRUserManager.createUser(user);
        }
        // no last login
        for (int i = 30; i < 40; i++) {
            String userName = "test" + i;
            user = new MCRUser(userName, MCRRealmFactory.getLocalRealm());
            user.setRealName("Tester, Hans" + i);
            user.setUserAttribute("id_connection", UUID.randomUUID().toString());
            MCRUserManager.createUser(user);
        }
        // will be deleted
        for (int i = 40; i < 50; i++) {
            String userName = "test" + i;
            user = new MCRUser(userName, MCRRealmFactory.getLocalRealm());
            user.setRealName("Tester, Petra" + i);
            user.setLastLogin();
            user.setUserAttribute("id_connection", UUID.randomUUID().toString());
            MCRUserManager.createUser(user);
        }

        assertEquals(50, MCRUserManager.countUsers(null, null, null, null));

        MODSPersonMigrationCommands.deleteUsersFromDatabase();
        assertEquals(40, MCRUserManager.countUsers(null, null, null, null));
    }

    @Test
    public void deleteConnectionIdsDB() throws IOException, MCRAccessException {
        MCRUser user;
        for (int i = 0; i < 30; i++) {
            String userName = "test" + i;
            user = new MCRUser(userName, MCRRealmFactory.getLocalRealm());
            user.setRealName("Tester, Peter" + i);
            user.setUserAttribute("id_connection", UUID.randomUUID().toString());
            user.setUserAttribute("id_connect", UUID.randomUUID().toString());
            user.setUserAttribute("id_scopus", UUID.randomUUID().toString());
            MCRUserManager.createUser(user);
        }
        assertEquals(30, MCRUserManager.countUsers(null, null, null, null));

        MODSPersonMigrationCommands.deleteConnectionIds();

        List<MCRUser> allUsers = MCRUserManager.listUsers(null, null, null, null);
        assertEquals(30, allUsers.size());

        for (MCRUser userAfter : allUsers) {
            assertEquals(2, userAfter.getAttributes().size());
            assertNull(userAfter.getUserAttribute("id_connection"));
            assertNotNull(userAfter.getUserAttribute("id_connect"));
            assertNotNull(userAfter.getUserAttribute("id_scopus"));
        }
    }

    @Test
    public void deleteConnectionIdsFileSystem() throws JDOMException, IOException, MCRAccessException {
        URL url1 = MCRObjectMetadataTest.class.getResource(
            "/MODSPersonMigrationCommandsTest/junit_mods_00000013.xml");
        Document doc1 = new MCRURLContent(url1).asXML();
        MCRObject obj1 = new MCRObject(doc1);

        URL url2 = MCRObjectMetadataTest.class.getResource(
            "/MODSPersonMigrationCommandsTest/junit_mods_00000014.xml");
        Document doc2 = new MCRURLContent(url2).asXML();
        MCRObject obj2 = new MCRObject(doc2);

        URL url3 = MCRObjectMetadataTest.class.getResource(
            "/MODSPersonMigrationCommandsTest/junit_mods_00000015.xml");
        Document doc3 = new MCRURLContent(url3).asXML();
        MCRObject obj3 = new MCRObject(doc3);

        MCRMetadataManager.create(obj1);
        MCRMetadataManager.create(obj2);
        MCRMetadataManager.create(obj3);

        List<String> allModIds = MCRCommandUtils.getIdsForType("mods").toList();
        assertEquals(3, allModIds.size());

        MODSPersonMigrationCommands.deleteConnectionIds();
        allModIds = MCRCommandUtils.getIdsForType("mods").toList();
        assertEquals(3, allModIds.size());

        for (String allModId : allModIds) {
            MCRObject o = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(allModId));
            List<Element> nameIdentifiers = new MCRMODSWrapper(o).getMODS()
                .getChildren("name", MCRConstants.MODS_NAMESPACE).stream()
                .flatMap(nameElement -> nameElement.getChildren("nameIdentifier", MCRConstants.MODS_NAMESPACE).stream())
                .toList();
            assertTrue(nameIdentifiers.stream()
                .noneMatch(e -> "connection".equals(e.getAttributeValue("type"))));
            assertTrue(nameIdentifiers.stream()
                .anyMatch(e -> "lsf".equals(e.getAttributeValue("type"))));
            assertTrue(nameIdentifiers.stream()
                .anyMatch(e -> "scopus".equals(e.getAttributeValue("type"))));
        }
    }
}
