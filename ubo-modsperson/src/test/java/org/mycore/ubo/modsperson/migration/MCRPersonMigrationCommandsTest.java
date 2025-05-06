package org.mycore.ubo.modsperson.migration;

import org.jdom2.Element;
import org.junit.Before;
import org.junit.Test;
import org.mycore.common.MCRStoreTestCase;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.mods.MCRMODSWrapper;
import org.mycore.ubo.modsperson.MODSPersonLookup;
import org.mycore.user2.MCRRealmFactory;
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUserManager;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.*;

public class MCRPersonMigrationCommandsTest extends MCRStoreTestCase {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        MODSPersonLookup.clear();
    }

    @Test
    public void testMigration() {
        String userName1 = String.valueOf(UUID.randomUUID());
        String userName2 = "test2";

        MCRUser userLocal = new MCRUser(userName1, MCRRealmFactory.getLocalRealm());
        userLocal.setRealName("Tester, Peter");
        MCRUser userOther = new MCRUser(userName2, MCRRealmFactory.getRealm("test-realm"));
        userOther.setRealName("Petra Tester");

        MCRUserManager.createUser(userLocal);
        MCRUserManager.createUser(userOther);

        MCRPersonMigrationCommands.migrateModsperson(userName1, "local");
        MCRPersonMigrationCommands.migrateModsperson(userName2, "test-realm");

        userLocal = MCRUserManager.getUser(userName1, "local");
        assertEquals(0, userLocal.getAttributes().size());

        userOther = MCRUserManager.getUser(userName2, "test-realm");
        assertEquals(1, userOther.getAttributes().size());
        assertEquals(MCRPersonMigrationCommands.MODSPERSON_ATTRIBUTE_NAME, userOther.getAttributes().first().getName());
        assertTrue(userOther.getAttributes().first().getValue().contains("junit_modsperson_0000"));
        assertEquals(2, MODSPersonLookup.getCacheSize());
    }

    @Test
    public void testMigrationUserNotFound() {
        MCRPersonMigrationCommands.migrateModsperson("userNotFound", "local");
        assertEquals(0, MODSPersonLookup.getCacheSize());
    }

    @Test
    public void testMigrationUserAttributes() {
        String userName = "username";
        MCRUser user = new MCRUser(userName, MCRRealmFactory.getLocalRealm());
        user.setRealName("Tester, Peter");
        user.setUserAttribute("id_connection", userName);
        user.setUserAttribute("id_orcid", "000-0001-2345-6789");
        user.setUserAttribute("id_scopus", "00011112222");

        MCRUserManager.createUser(user);

        MCRPersonMigrationCommands.migrateModsperson(userName, "local");
        user = MCRUserManager.getUser(userName, "local");
        assertEquals(4, user.getAttributes().size());
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
        assertEquals(2, personKeys.size());
        assertTrue(personKeys.contains("000-0001-2345-6789|orcid"));
        assertTrue(personKeys.contains("00011112222|scopus"));
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
        user = new MCRUser("administrator", MCRRealmFactory.getLocalRealm());
        user.setRealName("Administrator");
        MCRUserManager.createUser(user);

        List<String> commands = MCRPersonMigrationCommands.migrateAllModsperson();
        assertEquals(30, commands.size());
        for (String command : commands) {
            assertTrue(command.contains("ubo migrate user"));
            assertTrue(command.contains("in realm local to modsperson"));
            assertFalse(command.contains("administrator"));
        }
    }

    @Test
    public void testDeleteUsers() {
        MCRUser user;
        for (int i = 0; i < 10; i++) {
            String userName = UUID.randomUUID().toString();
            user = new MCRUser(userName, MCRRealmFactory.getLocalRealm());
            user.setRealName("Tester, Peter" + i);
            MCRUserManager.createUser(user);
        }
        for (int i = 10; i < 20; i++) {
            String userName = UUID.randomUUID().toString();
            user = new MCRUser(userName, MCRRealmFactory.getLocalRealm());
            user.setRealName("Tester, Petra" + i);
            user.setLastLogin();
            MCRUserManager.createUser(user);
        }
        for (int i = 20; i < 30; i++) {
            String userName = UUID.randomUUID().toString();
            user = new MCRUser(userName, MCRRealmFactory.getRealm("test-realm"));
            user.setRealName("Tester, Lisa" + i);
            MCRUserManager.createUser(user);
        }
        for (int i = 30; i < 40; i++) {
            String userName = "test" + i;
            user = new MCRUser(userName, MCRRealmFactory.getLocalRealm());
            user.setRealName("Tester, Hans" + i);
            MCRUserManager.createUser(user);
        }
        for (int i = 40; i < 50; i++) {
            String userName = "test" + i;
            user = new MCRUser(userName, MCRRealmFactory.getRealm("test-realm"));
            user.setRealName("Tester, Chris" + i);
            MCRUserManager.createUser(user);
        }
        assertEquals(50, MCRUserManager.countUsers(null, null, null, null));

        MCRPersonMigrationCommands.deleteUsersFromDatabase();
        assertEquals(40, MCRUserManager.countUsers(null, null, null, null));
    }
}
