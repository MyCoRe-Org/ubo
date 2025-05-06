package org.mycore.ubo.modsperson.migration;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.mycore.access.MCRAccessException;
import org.mycore.backend.jpa.MCREntityManagerProvider;
import org.mycore.common.MCRConstants;
import org.mycore.common.MCRException;
import org.mycore.common.MCRPersistenceException;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.cli.annotation.MCRCommand;
import org.mycore.frontend.cli.annotation.MCRCommandGroup;
import org.mycore.mods.MCRMODSWrapper;
import org.mycore.ubo.modsperson.MODSPersonLookup;
import org.mycore.ubo.modsperson.MODSPersonUtils;
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUserManager;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@MCRCommandGroup(name = "UBO modsperson migration commands")
public class MCRPersonMigrationCommands {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Pattern UUID_REGEX_PATTERN =
        Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
    private static final boolean IS_UUID_STRATEGY = "uuid".equals(MCRConfiguration2.getString(
        "MCR.user2.matching.publication.connection.strategy").orElse(""));
    protected static final String MODSPERSON_ATTRIBUTE_NAME = "id_modsperson";

    /**
     * Migrates all  {@link MCRUser users} except for the administrator to modsperson objects.
     * Doesn't check for duplicates or existent modsperson objects.
     */
    @MCRCommand(
        syntax = "ubo migrate users to modsperson",
        help = "Creates modsperson objects for all database users. Command is part of a migration process and should "
            + "only be used once. Should only be used if no modsperson objects exist yet",
        order = 1)
    public static List<String> migrateAllModsperson() {
        List<MCRUser> users = MCRUserManager.listUsers(null, null, null, null);
        List<String> commands = new ArrayList<>(users.size());
        for (MCRUser user : users) {
            if (user.getUserName().equals("administrator")) {
                continue;
            }
            commands.add("ubo migrate user " + user.getUserName() + " in realm "
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
        syntax = "ubo migrate user {0} in realm {1} to modsperson",
        help = "Creates a modsperson object for the specific database user {0} in realm {1} (user name, realm id). "
            + "Command is part of a migration process and should only be used if the corresponding modsperson object "
            + "doesn't exist yet",
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
        }
        else {
            LOGGER.warn("User {} not found in database", userName);
        }
    }

    /**
     * Deletes all artificially created users that work with the connection-id mechanism.
     * Should be executed after migration using {@link MCRPersonMigrationCommands#migrateAllModsperson()} first.
     */
    @MCRCommand(
        syntax = "ubo delete users from database",
        help = "Deletes all users from database that have no user account. Command is part of a migration process "
            + "and should only be used once",
        order = 2)
    public static void deleteUsersFromDatabase() {
        EntityManager em = MCREntityManagerProvider.getCurrentEntityManager();
        Query query = em.createQuery("SELECT u FROM MCRUser u "
            + "WHERE realmID = 'local' AND lastLogin is null"); // TODO: is 'local' really hard-coded?
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
            if (isArtificialUser(user)) {
                MCRUserManager.deleteUser(user);
                count++;
                if (count % 1000 == 0) {
                    LOGGER.info("Deleted {} users from database...", count);
                }
            }
        }
        LOGGER.info("Successfully deleted {} users from database", count);
    }

    /**
     * TODO: Use MCRUserCommands.setUserAttribute once mycore-user2 in 2024.06.x is used.
     * Adds a new attribute {@link MCRPersonMigrationCommands#MODSPERSON_ATTRIBUTE_NAME} to the database user,
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
            if (!attribute.getName().equals("id_connection")) {
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
     *     <li>UUID-like username (only if connection strategy is uuid)</li>
     *     <li>local realm</li>
     *     <li>has not logged in yet</li>
     * </ol>
     * @param user user to test
     * @return true, if user was artificially added
     */
    private static boolean isArtificialUser(MCRUser user) {
        return (!IS_UUID_STRATEGY || UUID_REGEX_PATTERN.matcher(user.getUserName()).matches())
            && user.getRealmID().equals("local") && user.getLastLogin() == null;
    }

}
