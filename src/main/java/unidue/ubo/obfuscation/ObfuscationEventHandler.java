package unidue.ubo.obfuscation;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.common.events.MCREvent;
import org.mycore.common.events.MCREventHandlerBase;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUserAttribute;
import org.mycore.user2.MCRUserManager;
import unidue.ubo.matcher.MCRUserMatcherUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * EventHandler for obfuscation of nameIdentifiers
 *
 * There might exist IDs in services that are used for Identity Picking (for example LDAP) that should not be placed
 * in the publications that are imported since they represent sensitive information. Nevertheless we want to keep
 * the IDs for internal linking/matching to MCRUsers and Objects/Documents of the IdentityPicker and Matching services.
 *
 * 1. This EventHandler reacts if a publication contains mods:name - nameIdentifiers of a configurable type
 * 2. The specific nameIdentifier Element gets replaced by a configurable target nameIdentifier Type and the hashed
 * value of the original nameIdentifier in the publication in question, thus erasing the original nameIdentifier in
 * question
 * 3. If a MCRUser with the original nameIdentifier ID (type and value) is found in the local database, the MCRUser
 * is enriched with the configured nameIdentifier Type and the new hashed ID.
 *
 * The following properties in the mycore.properties are used:
 *
 * MCR.publication.obfuscation.targetIdentifierType
 * Example:
 * MCR.publication.obfuscation.targetIdentifierType=jena
 *
 * MCR.publication.obfuscation.replacementIdentifierType
 * Example:
 * MCR.publication.obfuscation.replacementIdentifierType=author
 *
 * MCR.publication.obfuscation.salt
 * Example:
 * MCR.publication.obfuscation.salt=1234567890
 *
 * @author Pascal Rost
 */
public class ObfuscationEventHandler extends MCREventHandlerBase {

    private final static Logger LOGGER = LogManager.getLogger(ObfuscationEventHandler.class);

    public static final Namespace MODS_NAMESPACE = Namespace.getNamespace("mods", "http://www.loc.gov/mods/v3");

    private final static String CONFIG_TARGET = "MCR.publication.obfuscation.targetIdentifierType";
    private final static String CONFIG_REPLACEMENT = "MCR.publication.obfuscation.replacementIdentifierType";
    private final static String CONFIG_SALT = "MCR.publication.obfuscation.salt";

    private final String obfuscation_target_id = MCRConfiguration.instance().getString(CONFIG_TARGET, "");
    private final String obfuscation_replacement_id = MCRConfiguration.instance().getString(CONFIG_REPLACEMENT, "");
    private final String obfuscation_salt = MCRConfiguration.instance().getString(CONFIG_SALT, "");

    @Override
    protected void handleObjectUpdated(MCREvent evt, MCRObject obj) {
        // TODO: remove this, since this EventHandler should only work for "ObjectCreated" events!
        handleObjectCreated(evt, obj);
    }

    @Override
    protected void handleObjectCreated(MCREvent evt, MCRObject obj) {
        LOGGER.debug("Input document: {}", new XMLOutputter(Format.getPrettyFormat()).outputString(obj.createXML()));

        if(StringUtils.isEmpty(obfuscation_target_id) ||
                StringUtils.isEmpty(obfuscation_replacement_id) ||
                StringUtils.isEmpty(obfuscation_salt)) {
            LOGGER.error("One or multiple of the following configuration properties are missing or empty in " +
                    "mycore.properties: {}, {}, {}, will not continue with obfuscation",
                    CONFIG_TARGET, CONFIG_REPLACEMENT, CONFIG_SALT);
            return;
        }

        // get all mods:name from persons (authors etc.) of the publication
        List<Element> modsNameElements = MCRUserMatcherUtils.getNameElements(obj);

        for(Element modsNameElement : modsNameElements) {
            if(MCRUserMatcherUtils.containsNameIdentifierWithType(modsNameElement, obfuscation_target_id)) {
                LOGGER.debug("Found mods:name with nameIdentifier of type: {}", obfuscation_target_id);
                LOGGER.debug("mods:name Element: {}", new XMLOutputter(Format.getPrettyFormat()).outputString(modsNameElement));
                Element nameIdentifierElement = getNameIdentifierByType(modsNameElement, obfuscation_target_id);
                String target_id = nameIdentifierElement.getText();
                String hashed_id = hashTargetID(target_id);
                LOGGER.debug("Got target_id to obfuscate: {}", target_id);
                LOGGER.debug("Obfuscated (hashed) target_id is: {}", hashed_id);

                // replace target_id with obfuscated ID (salted and hashed)
                nameIdentifierElement.setText(hashed_id);
                nameIdentifierElement.setAttribute("type", obfuscation_replacement_id);

                // search MCRUser with original target_id
                List<MCRUser> users = MCRUserManager.getUsers("id_" + obfuscation_target_id, target_id)
                        .collect(Collectors.toList());
                if(users.size() == 1) {
                    MCRUser target_user = users.get(0);
                    // add hashed id to user
                    Set<MCRUserAttribute> attributes = target_user.getAttributes();
                    attributes.add(new MCRUserAttribute(obfuscation_replacement_id, hashed_id));
                    // persist user changes
                    MCRUserManager.updateUser(target_user);
                } else {
                    LOGGER.error("Found multiple users ({}) for target_id: {}", users.size(), target_id);
                    // TODO: throw exception or change EventHandler to only modify data if a user is found etc...
                }

            }
        }

        LOGGER.debug("Output document: {}", new XMLOutputter(Format.getPrettyFormat()).outputString(obj.createXML()));
    }

    private Element getNameIdentifierByType(Element modsNameElement, String nameIdentifierType) {
        Element nameIdentifier = null;
        List<Element> identifiers = modsNameElement.getChildren("nameIdentifier", MODS_NAMESPACE);
        for(Element identifierElement : identifiers) {
            String type = identifierElement.getAttributeValue("type");
            if(type.equals(nameIdentifierType)) {
                nameIdentifier = identifierElement;
            }
        }
        return nameIdentifier;
    }

    private String hashTargetID(String target_id) {
        return DigestUtils.sha256Hex(target_id + obfuscation_salt);
    }
}
