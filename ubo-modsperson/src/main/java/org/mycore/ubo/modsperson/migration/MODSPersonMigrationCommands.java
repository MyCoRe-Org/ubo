package org.mycore.ubo.modsperson.migration;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.access.MCRAccessException;
import org.mycore.backend.jpa.MCREntityManagerProvider;
import org.mycore.common.MCRConstants;
import org.mycore.common.MCRException;
import org.mycore.common.MCRPersistenceException;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.cli.MCRCommandUtils;
import org.mycore.frontend.cli.annotation.MCRCommand;
import org.mycore.frontend.cli.annotation.MCRCommandGroup;
import org.mycore.mods.MCRMODSWrapper;
import org.mycore.orcid2.user.MCRORCIDUser;
import org.mycore.ubo.modsperson.MODSPersonLookup;
import org.mycore.ubo.modsperson.MODSPersonUtils;
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUserManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@MCRCommandGroup(name = "UBO modsperson migration commands")
public class MODSPersonMigrationCommands {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final String CONNECTION_ATTRIBUTE_NAME = "id_connection";
    protected static final String MODSPERSON_ATTRIBUTE_NAME = "id_modsperson";
    protected static final String ORCID_AUTH_ATTRIBUTE_PREFIX = MCRORCIDUser.ATTR_ORCID_CREDENTIAL;
    protected static final String ORCID_USER_PROPERTIES_ATTRIBUTE_PREFIX = MCRORCIDUser.ATTR_ORCID_USER_PROPERTIES;

    /**
     * Migrates all  {@link MCRUser users} with connection IDs to modsperson objects.
     * Doesn't check for duplicates or existent modsperson objects.
     */
    @MCRCommand(
        syntax = "modsperson migration migrate users to modsperson",
        help = "FIRST migration command. Creates modsperson objects for all database users. Command is part of a migration process and should "
            + "only be used once. Should only be used if no modsperson objects exist yet.",
        order = 1)
    public static List<String> migrateAllModsperson() {
        List<MCRUser> users = MCRUserManager.listUsers(null, null, null, null, CONNECTION_ATTRIBUTE_NAME, 0, Integer.MAX_VALUE);
        List<String> commands = new ArrayList<>(users.size());
        for (MCRUser user : users) {
            commands.add("modsperson migration migrate user " + user.getUserName() + " in realm "
                + user.getRealmID() + " to modsperson");
        }
        return commands;
    }

    /**
     * Migrates a single {@link MCRUser} to a modsperson object using username and realm-id as keys.
     * @param userName the username to search for
     * @param realmId the realm to search in
     */
    @MCRCommand(
        syntax = "modsperson migration migrate user {0} in realm {1} to modsperson",
        help = "Creates a modsperson object for the specific database user {0} in realm {1} (user name, realm id). "
            + "Command is part of a migration process and should only be used if the corresponding modsperson object "
            + "doesn't exist yet.",
        order = 3)
    public static void migrateModsperson(String userName, String realmId) {
        MCRUser user = null;
        try {
            user = MCRUserManager.getUser(userName, realmId);
        } catch (Exception e) {
            LOGGER.error("Error finding user {} in database: {}", userName, e.getMessage());
        }
        if (user != null) {
            final MCRObject modsperson = migrateData(user);
            try {
                MCRMetadataManager.create(modsperson);
                MODSPersonLookup.add(modsperson);
                addModspersonAttribute(user, modsperson.getId());
            } catch (MCRPersistenceException | MCRAccessException ex) {
                LOGGER.warn("Creation of modsperson for user {} failed: {}", user.getUserName(), ex.getMessage());
            }
        } else {
            LOGGER.warn("User {} not found in database", userName);
        }
    }

    /**
     * Deletes all artificially created users that work with the connection-id mechanism.
     * Should be executed after migration using {@link MODSPersonMigrationCommands#migrateAllModsperson()} first.
     */
    @MCRCommand(
        syntax = "modsperson migration delete users from database",
        help = "SECOND migration command. Deletes all users from database that have no user account. Command is part of a migration process "
            + "and should only be used once.",
        order = 2)
    public static void deleteUsersFromDatabase() {
        EntityManager em = MCREntityManagerProvider.getCurrentEntityManager();
        Query query = em.createQuery("SELECT DISTINCT u FROM MCRUser u "
            + "JOIN u.attributes a "
            + "WHERE u.realmID = 'local' "
            + "AND u.lastLogin IS NULL "
            + "AND a.name = :connection_ID");
        query.setParameter("connection_ID", CONNECTION_ATTRIBUTE_NAME);
        List<MCRUser> users;
        try {
            users = query.getResultList();
        } catch (Exception e) {
            LOGGER.error(e);
            throw new MCRPersistenceException("Deletion of users failed: ", e);
        }
        int count = 0;
        LOGGER.info("Will delete {} users from database...", users.size());
        for (MCRUser user : users) {
            MCRUserManager.deleteUser(user);
            count++;
            if (count != 0 && count % 1000 == 0) {
                LOGGER.info("Deleted {} users from database...", count);
            }
        }
        LOGGER.info("Successfully deleted {} users from database", count);
    }

    /**
     * Deletes connection IDs from {@link org.mycore.user2.MCRUserAttribute} and from mods-type
     * files in the file system.
     * Should be executed after deletion of artificial users using
     * {@link MODSPersonMigrationCommands#deleteUsersFromDatabase()}, as artificial user's connection IDs
     * are already deleted then.
     */
    @MCRCommand(
        syntax = "modsperson migration delete connection ids",
        help = "THIRD migration command. Deletes all connection ids from database and from publications. "
            + "Command is part of a migration process and should only be used once. Should be executed after "
            + "the deletion of artificial users for performance reasons.",
        order = 4)
    public static void deleteConnectionIds() throws IOException, MCRAccessException {
        EntityManager em = MCREntityManagerProvider.getCurrentEntityManager();
        Query query = em.createQuery("SELECT DISTINCT u FROM MCRUser u "
            + "JOIN u.attributes a "
            + "WHERE a.name = :connection_ID");
        query.setParameter("connection_ID", CONNECTION_ATTRIBUTE_NAME);

        List<MCRUser> users = query.getResultList();

        for (MCRUser user : users) {
            user.getAttributes().removeIf(attr -> CONNECTION_ATTRIBUTE_NAME.equals(attr.getName()));
        }

        LOGGER.info("Successfully deleted {} connection IDs from database", users.size());

        List<String> ids = MCRCommandUtils.getIdsForType("mods").toList();
        int count = 0;
        LOGGER.info("Will search for connection ids in {} publications...", ids.size());

        for (String id : ids) {
            MCRObject obj = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(id));
            Document xmlOld = obj.createXML();
            Document xmlNew = xmlOld.clone();

            String path = "/mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/"
                + "mods:name[@type='personal']/mods:nameIdentifier[@type='connection']";

            XPathExpression<Element> xPath =
                XPathFactory.instance().compile(path, Filters.element(), null, MCRConstants.MODS_NAMESPACE);

            for (Element nameIdentifier : xPath.evaluate(xmlNew)) {
                nameIdentifier.detach();
            }

            String oldData = new MCRJDOMContent(xmlOld).asString();
            String newData = new MCRJDOMContent(xmlNew).asString();
            if (!oldData.equals(newData)) {
                MCRObject objNew = new MCRObject(xmlNew);
                try {
                    MCRMetadataManager.update(objNew);
                    count++;
                } catch (Exception e) {
                    LOGGER.error("Error while processing object {}, "
                        + "skipping deletion of connection-ID, please process manually: {}", id, e);
                }
            }
            if (count != 0 && count % 1000 == 0) {
                LOGGER.info("Deleted connection ids from {} publications...", count);
            }
        }
        LOGGER.info("Successfully deleted connection ids from {} publications", count);
    }

    /**
     * TODO: Use MCRUserCommands.setUserAttribute once mycore-user2 in 2024.06.x is used.
     * Adds a new attribute {@link MODSPersonMigrationCommands#MODSPERSON_ATTRIBUTE_NAME} to the database user,
     * if user is not artificial.
     * @param user the database user
     * @param modspersonId the ID to be added as a user attribute
     */
    private static void addModspersonAttribute(MCRUser user, MCRObjectID modspersonId) {
        if (!isArtificialUser(user)) {
            try {
                user.setUserAttribute(MODSPERSON_ATTRIBUTE_NAME, modspersonId.toString());
                MCRUserManager.updateUser(user);
            } catch (Exception e) {
                throw new MCRException(
                    "Error while setting attribute " + MODSPERSON_ATTRIBUTE_NAME + " to " + modspersonId +
                        " for user " + user.getUserID() + ": ", e);
            }
        }
    }

    /**
     * Creates a modsperson-object using the data of a {@link MCRUser}-entity.
     * @param user the user that contains the data that's migrated to a modsperson.
     */
    private static MCRObject migrateData(MCRUser user) {
        MCRObject modsperson = new MCRObject(new Document(MODSPersonUtils.getMODSPersonTemplate().clone()));
        MCRMODSWrapper wrapper = new MCRMODSWrapper(modsperson);
        Element personName = wrapper.getElement("mods:name[@type='personal']");

        String[] nameParts = user.getRealName().split(",");
        String familyName = nameParts.length < 2 ? user.getRealName() : nameParts[0];
        String givenName = nameParts.length < 2 ? user.getRealName() : nameParts[1];

        Element familyNamePart = new Element("namePart", MCRConstants.MODS_NAMESPACE)
            .setAttribute("type", "family")
            .setText(familyName);

        Element givenNamePart = new Element("namePart", MCRConstants.MODS_NAMESPACE)
            .setAttribute("type", "given")
            .setText(givenName);

        personName.addContent(familyNamePart);
        personName.addContent(givenNamePart);

        user.getAttributes().iterator().forEachRemaining(attribute -> {
            if (!isExcludedAttribute(attribute.getName())) {
                final String typeName = attribute.getName().startsWith("id_") ? attribute.getName().substring(3)
                                                                              : attribute.getName();
                Element nameIdentifier = new Element("nameIdentifier", MCRConstants.MODS_NAMESPACE)
                    .setAttribute("type", typeName)
                    .setText(attribute.getValue());
                personName.addContent(nameIdentifier);
            }
        });
        return modsperson;
    }

    /**
     * Tests if a user was artificially added by the three criteria:
     * <ol>
     *     <li>has a connection id</li>
     *     <li>local realm</li>
     *     <li>has not logged in yet</li>
     * </ol>
     * @param user user to test
     * @return true, if user was artificially added
     */
    private static boolean isArtificialUser(MCRUser user) {
        return (user.getAttributes().stream()
            .noneMatch(attr -> "id_connection".equals(attr.getName())))
            && user.getRealmID().equals("local") && user.getLastLogin() == null;
    }

    /**
     * True, if the attribute name pertains the connection id or the ORCID authentication,
     * or describes the modsperson itself.
     * Attribute name "id_orcid" will be migrated.
     * @param attributeName the name of the tested attribute
     * @return true, if excluded
     */
    private static boolean isExcludedAttribute(String attributeName) {
        return attributeName.equals(CONNECTION_ATTRIBUTE_NAME)
            || attributeName.startsWith(MODSPERSON_ATTRIBUTE_NAME)
            || attributeName.startsWith(ORCID_AUTH_ATTRIBUTE_PREFIX)
            || attributeName.startsWith(ORCID_USER_PROPERTIES_ATTRIBUTE_PREFIX);
    }

}
