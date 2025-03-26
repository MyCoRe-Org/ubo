package org.mycore.ubo.modsperson.migration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRConstants;
import org.mycore.common.MCRException;
import org.mycore.common.MCRPersistenceException;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.frontend.cli.annotation.MCRCommand;
import org.mycore.frontend.cli.annotation.MCRCommandGroup;
import org.mycore.mods.MCRMODSWrapper;
import org.mycore.ubo.modsperson.MODSPersonLookup;
import org.mycore.ubo.modsperson.MODSPersonUtils;
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUserManager;

import java.util.List;

@MCRCommandGroup(name = "UBO modsperson migration commands")
public class MCRPersonMigrationCommands {

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * TODO
     * Doesn't check for duplicates or existent modsperson objects
     */
    @MCRCommand(
        syntax = "ubo migrate users to modsperson",
        help = "Creates modsperson objects for all database users. Command is part of a migration process and should "
            + "only be used once. Should only be used if no modsperson objects exist yet",
        order = 1)
    public static void migrateAllModsperson() {
        List<MCRUser> users = MCRUserManager.listUsers(null, null, null, null);
        for (MCRUser user : users) {
            if (user.getUserName().equals("administrator")) {
                continue;
            }
            final MCRObject modsperson = new MCRObject(new Document(MODSPersonUtils.getMODSPersonTemplate().clone()));
            setData(modsperson, user);
            try {
                MCRMetadataManager.create(modsperson);
            } catch (MCRPersistenceException | MCRAccessException ex) {
                LOGGER.warn("Creation of modsperson for user {} failed: {}", user.getUserName(), ex.getMessage());
            }
            MODSPersonLookup.add(modsperson);
        }
    }

    @MCRCommand(
        syntax = "ubo migrate user {0} in realm {1} to modsperson",
        help = "Creates a modsperson object for the specific database user {0} in realm {1} (user name, realm id). "
            + "Command is part of a migration process and should only be used if the corresponding modsperson object "
            + "doesn't exist yet",
        order = 3)
    public static void migrateModsperson(String userName, String realmId) {
        MCRUser user = MCRUserManager.getUser(userName, realmId);
        final MCRObject modsperson = new MCRObject(new Document(MODSPersonUtils.getMODSPersonTemplate().clone()));
        setData(modsperson, user);
        try {
            MCRMetadataManager.create(modsperson);
        } catch (MCRPersistenceException | MCRAccessException ex) {
            LOGGER.warn("Creation of modsperson for user {} failed: {}", user.getUserName(), ex.getMessage());
        }
        Document xml = modsperson.createXML();
        LOGGER.info("Generated MODSPERSON: " + new XMLOutputter(Format.getPrettyFormat()).outputString(xml));
        MODSPersonLookup.add(modsperson);
    }

    private static void setData(MCRObject modsperson, MCRUser user) throws MCRException {
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
    }

    /**
     * TODO
     * Should be executed after migration using {@link MCRPersonMigrationCommands#migrateAllModsperson()} first.
     */
    @MCRCommand(
        syntax = "ubo delete users from database",
        help = "Deletes all users from database that have no user account. Command is part of a migration process "
            + "and should only be used once",
        order = 2)
    public static void deleteUsersFromDatabase() {

    }

}
