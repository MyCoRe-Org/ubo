package unidue.ubo.matcher;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.mods.MCRMODSWrapper;
import org.mycore.orcid.user.MCRORCIDUser;
import org.mycore.user2.MCRUser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Utility class for everything related to matching users of publications in MODS-format with MCRUsers and other
 * applications/servers/APIs.
 *
 * @author Pascal Rost
 */
public class MCRUserMatcherUtils {

    private final static Logger LOGGER = LogManager.getLogger(MCRUserMatcherUtils.class);

    public static final Namespace MODS_NAMESPACE = Namespace.getNamespace("mods", "http://www.loc.gov/mods/v3");

    private final static String UNVALIDATED_REALM = "ude"; // TODO: create specific realm for unvalidated users

    public static List<Element> getNameElements(MCRObject obj) {
        MCRMODSWrapper wrapper = new MCRMODSWrapper(obj);
        return wrapper.getElements("mods:name[@type='personal']");
    }

    private static Map<String, String> getNameIdentifiers(Element modsNameElement) {
        Map<String, String> nameIdentifiers = new HashMap<>(); // TODO: possible use of MultiMap for multiple attributes of the same type
        List<Element> identifiers = modsNameElement.getChildren("nameIdentifier", MODS_NAMESPACE);
        for(Element identifierElement : identifiers) {
            String type = identifierElement.getAttributeValue("type");
            String identifier = identifierElement.getText();
            nameIdentifiers.put(type, identifier);
        }
        LOGGER.debug("Found nameIdentifiers from XML: {}", nameIdentifiers);
        return nameIdentifiers;
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
    }

    private static String mapModsNameIdentifierTypeToMycore(String nameIdentifierType) {
        return MCRORCIDUser.ATTR_ID_PREFIX + nameIdentifierType;
    }

    public static MCRUser createNewMCRUserFromModsNameElement(Element modsNameElement) {
        String userName = getUserNameFromModsNameElement(modsNameElement);
        Map<String, String> nameIdentifiers = MCRUserMatcherUtils.getNameIdentifiers(modsNameElement);
        MCRUser mcrUser =  new MCRUser(userName, UNVALIDATED_REALM);
        enrichUserWithGivenNameIdentifiers(mcrUser, nameIdentifiers);
        return mcrUser;
    }

    private static String getUserNameFromModsNameElement(Element modsNameElement) {
        // TODO: IMPORTANT -> creating the name this way, the connection between any matched user and the login of the
        // TODO: target API (for example LDAP) will not work. For LDAP, the MCRUsers username needs to be the same as
        // TODO: the uid (or cn) in LDAP
        // TODO: adapt the LDAP login to use a special MCRUser-Attribute (ldap_login...) instead of the username (?)

        XPathFactory xFactory = XPathFactory.instance();

        XPathExpression<Element> givenNameExpr = xFactory.compile("mods:namePart[@type='given']",
                Filters.element(), null, MODS_NAMESPACE);
        Element givenNameElem = givenNameExpr.evaluateFirst(modsNameElement);
        XPathExpression<Element> familyNameExpr = xFactory.compile("mods:namePart[@type='family']",
                Filters.element(), null, MODS_NAMESPACE);
        Element familyNameElem = familyNameExpr.evaluateFirst(modsNameElement);

        String userName = "";

        if((givenNameElem != null) && (familyNameElem != null)) {
            userName = (givenNameElem.getText() + "_" + familyNameElem.getText()).toLowerCase();
        }

        return userName;
    }
}
