package unidue.ubo.matcher;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.mods.MCRMODSWrapper;
import org.mycore.orcid.user.MCRORCIDUser;
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUserManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Utility class for everything related to matching authors of publications in MODS-format with MCRUsers.
 *
 * @author Pascal Rost
 */
public class MCRAuthorMatcherUtils {

    private final static Logger LOGGER = LogManager.getLogger(MCRAuthorMatcherUtils.class);

    public static final Namespace MODS_NAMESPACE = Namespace.getNamespace("mods", "http://www.loc.gov/mods/v3");

    public static List<Element> getAuthors(MCRObject obj) {
        MCRMODSWrapper wrapper = new MCRMODSWrapper(obj);
        return wrapper.getElements("mods:name[@type='personal' and mods:role/mods:roleTerm[@authority='marcrelator']='aut']");
    }

    public static Map<String, String> getNameIdentifiers(Element author) {
        Map<String, String> nameIdentifiers = new HashMap<>(); // TODO: possible use of MultiMap for multiple attributes of the same type
        List<Element> identifiers = author.getChildren("nameIdentifier", MODS_NAMESPACE);
        for(Element identifierElement : identifiers) {
            String type = identifierElement.getAttributeValue("type");
            String identifier = identifierElement.getText();
            nameIdentifiers.put(type, identifier);
        }
        return nameIdentifiers;
    }

    public static Set<MCRUser> getUsersForGivenNameIdentifiers(Map<String, String> nameIdentifiers) {
        Set<MCRUser> users = new HashSet<>();
        for(Map.Entry<String, String> nameIdentifier : nameIdentifiers.entrySet()) {
            String name = mapModsNameIdentifierTypeToMycore(nameIdentifier.getKey());
            users.addAll(MCRUserManager.getUsers(name, nameIdentifier.getValue()).collect(Collectors.toList()));
        }
        return users;
    }

    /**
     * Extend the MCRUsers attributes by the given mods:nameIdentifiers if the user does not already have these IDs
     * @param user the MCRUser whose attributes will be enriched
     * @param nameIdentifiers the mods:nameIdentifiers that should be added, if they are not already present
     */
    public static void enrichUserWithGivenNameIdentifiers(MCRUser user, Map<String, String> nameIdentifiers) {
        Map<String, String> userAttributes = user.getAttributes();
        for(Map.Entry<String, String> nameIdentifier : nameIdentifiers.entrySet()) {
            String name = mapModsNameIdentifierTypeToMycore(nameIdentifier.getKey());
            String value = nameIdentifier.getValue();
            if(!userAttributes.containsKey(name)) {
                LOGGER.debug("Enriching user: {} with attribute: {}, value: {}", user.getUserName(), name, value);
                userAttributes.put(name, value);
            }
        }
        user.setAttributes(userAttributes);
        MCRUserManager.updateUser(user);
    }

    private static String mapModsNameIdentifierTypeToMycore(String nameIdentifierType) {
        return MCRORCIDUser.ATTR_ID_PREFIX + nameIdentifierType;
    }
}
