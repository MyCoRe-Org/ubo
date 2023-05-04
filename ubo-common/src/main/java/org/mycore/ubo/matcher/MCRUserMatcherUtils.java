package org.mycore.ubo.matcher;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.mods.MCRMODSWrapper;
import org.mycore.ubo.ldap.LDAPObject;
import org.mycore.user2.MCRRealmFactory;
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUser2Constants;
import org.mycore.user2.MCRUserAttribute;


/**
 * Utility class for everything related to matching users of publications in MODS-format with MCRUsers and other
 * applications/servers/APIs.
 *
 * The following properties in the mycore.properties are used:
 *
 * # Used to check the affiliation of publication authors
 * MCR.user2.IdentityManagement.UserCreation.Affiliation=Uni Jena
 *
 * # Mapping from LDAP attribute to real name of user
 * MCR.user2.LDAP.Mapping.Name=cn
 *
 * # Mapping from LDAP attribute to E-Mail address of user
 * MCR.user2.LDAP.Mapping.E-Mail=mail
 *
 * # Mapping of any attribute.value combination to group membership of user
 * # eduPersonScopedAffiliation may be faculty|staff|employee|student|alum|member|affiliate
 * MCR.user2.LDAP.Mapping.Group.eduPersonScopedAffiliation.staff@uni-duisburg-essen.de=submitter *
 *
 * @author Pascal Rost
 */
public class MCRUserMatcherUtils {

    private final static Logger LOGGER = LogManager.getLogger(MCRUserMatcherUtils.class);

    public static final Namespace MODS_NAMESPACE = Namespace.getNamespace("mods", "http://www.loc.gov/mods/v3");

    private static final String CONFIG_PREFIX = MCRUser2Constants.CONFIG_PREFIX + "LDAP.";

    public static List<Element> getNameElements(MCRObject obj) {
        MCRMODSWrapper wrapper = new MCRMODSWrapper(obj);
        return wrapper.getElements("mods:name[@type='personal']");
    }

    public static Map<String, String> getNameIdentifiers(Element modsNameElement) {
        Map<String, String> nameIdentifiers = new HashMap<>(); // TODO: possible use of MultiMap for multiple attributes of the same type
        List<Element> identifiers = modsNameElement.getChildren("nameIdentifier", MODS_NAMESPACE);
        for(Element identifierElement : identifiers) {
            String type = identifierElement.getAttributeValue("type");
            if(type!=null){
                String identifier = identifierElement.getText();
                nameIdentifiers.put(type, identifier);
            }
        }
        LOGGER.debug("Found nameIdentifiers from XML: {}", nameIdentifiers);
        return nameIdentifiers;
    }

    public static boolean containsNameIdentifierWithType(Element modsNameElement, String identifierType) {
        return MCRUserMatcherUtils.getNameIdentifiers(modsNameElement).containsKey(identifierType);
    }

    /**
     * Extend the MCRUsers attributes by the given mods:nameIdentifiers if the user does not already have these IDs
     * @param user the MCRUser whose attributes will be enriched
     * @param nameIdentifiers the mods:nameIdentifiers that should be added, if they are not already present
     */
    public static void enrichUserWithGivenNameIdentifiers(MCRUser user, Map<String, String> nameIdentifiers) {
        SortedSet<MCRUserAttribute> userAttributes = user.getAttributes();
        for(Map.Entry<String, String> nameIdentifier : nameIdentifiers.entrySet()) {
            String name = mapModsNameIdentifierTypeToMycore(nameIdentifier.getKey());
            String value = nameIdentifier.getValue();
            if(user.getUserAttribute(name) == null) {
                LOGGER.debug("Enriching user: {} with attribute: {}, value: {}", user.getUserName(), name, value);
                userAttributes.add(new MCRUserAttribute(name, value));
            }
        }
        user.setAttributes(userAttributes);
    }

    private static String mapModsNameIdentifierTypeToMycore(String nameIdentifierType) {
        //TODO
        return "TODO";/*MCRORCIDUser.ATTR_ID_PREFIX + nameIdentifierType;*/
    }

    /**
     * Given a mods:name Element, create a new transient (not persisted) MCRUser where the mods:namePart and
     * mods:nameIdentifier child-elements are used for the user name and attributes of the new MCRUser. Does not set a
     * realm for the new MCRUser (the default one is the configured "local"-realm).
     * @param modsNameElement the mods:name-Element (xml) from which a new transient MCRUser shall be created
     * @return MCRUser, a transient MCRUser in the realm "local" (as configured)
     */
    public static MCRUser createNewMCRUserFromModsNameElement(Element modsNameElement) {
        return createNewMCRUserFromModsNameElement(modsNameElement, MCRRealmFactory.getLocalRealm().getID());
    }

    public static MCRUser createNewMCRUserFromModsNameElement(Element modsNameElement, String realmID) {
        String userName = UUID.randomUUID().toString();
        Map<String, String> nameIdentifiers = MCRUserMatcherUtils.getNameIdentifiers(modsNameElement);
        MCRUser mcrUser = new MCRUser(userName, realmID);
        enrichUserWithGivenNameIdentifiers(mcrUser, nameIdentifiers);
        return mcrUser;
    }

    public static String getAttributesAsURLString(List<Element> modsNameElements) {
        String parameters = "";
        for(Element modsNameElement : modsNameElements) {
            Map<String, String> parametersMap = getNameIdentifiers(modsNameElement);

            XPathFactory xFactory = XPathFactory.instance();

            XPathExpression<Element> givenNameExpr = xFactory.compile("mods:namePart[@type='given']",
                    Filters.element(), null, MODS_NAMESPACE);
            Element givenNameElem = givenNameExpr.evaluateFirst(modsNameElement);
            XPathExpression<Element> familyNameExpr = xFactory.compile("mods:namePart[@type='family']",
                    Filters.element(), null, MODS_NAMESPACE);
            Element familyNameElem = familyNameExpr.evaluateFirst(modsNameElement);

            if(familyNameElem != null) {
                parametersMap.put("lastName", familyNameElem.getText());
            }
            if(givenNameElem != null) {
                // the following is a compatibility preserving hack, the LSF-Search works with "firstname" WITHOUT CAMELCASE
                parametersMap.put("firstname", givenNameElem.getText());
                // the LDAP-Search works WITH CAMELCASE so at this point we just provide both parameters
                parametersMap.put("firstName", givenNameElem.getText());
            }
            List<String> singleParameters = new ArrayList<>();
            for(Map.Entry<String, String> parameter : parametersMap.entrySet()) {
                String encodedValue = null;
                try {
                    encodedValue = URLEncoder.encode(parameter.getValue(), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                if(!parameter.getKey().isEmpty()){
                    singleParameters.add(parameter.getKey() + '=' + encodedValue);
                }
            }
            //String ampersand = escapeAmpersand ? "&amp;" : "&";
            String ampersand = "&";
            parameters = String.join(ampersand, singleParameters);
        }
        LOGGER.info("parameters: " + parameters);
        return parameters;
    }

    public static boolean checkAffiliation(Element modsNameElement) {
        String affiliation = MCRConfiguration2.getString("MCR.user2.IdentityManagement.UserCreation.Affiliation").orElse(null);
        if(affiliation == null) {
            return false;
        }

        boolean affiliated = false;

        LOGGER.debug("Checking affiliation of:");
        LOGGER.debug(new XMLOutputter(Format.getPrettyFormat()).outputString(modsNameElement));

        XPathFactory xFactory = XPathFactory.instance();
        XPathExpression<Element> affiliationExpr = xFactory.compile("mods:affiliation",
                Filters.element(), null, MODS_NAMESPACE);
        Element affiliationElem = affiliationExpr.evaluateFirst(modsNameElement);
        if(affiliationElem != null) {
            String modsNameAffiliation = affiliationElem.getText();
            if(modsNameAffiliation.contains(affiliation)) {
                affiliated = true;
            }
        }
        LOGGER.debug("Affiliated: {}", affiliated);
        return affiliated;
    }

    /**
     * Method to set the static MCRUser-Attributes (RealName and Email)
     * @param mcrUser the MCRUser whose static Attributes should be set
     * @param ldapUser the ldapUser whose Attributes are used to fill the static attributes of the MCRUser
     */
    public static void setStaticMCRUserAttributes(MCRUser mcrUser, LDAPObject ldapUser) {
        for(Map.Entry<String, String> attributeEntry : ldapUser.getAttributes().entries()) {
            String attributeID = attributeEntry.getKey();
            String attributeValue = attributeEntry.getValue();
            setUserRealName(mcrUser, attributeID, attributeValue);
            setUserEMail(mcrUser, attributeID, attributeValue);
        }
    }

    private static void setUserEMail(MCRUser user, String attributeID, String attributeValue) {
        String mapEMail = MCRConfiguration2.getString(CONFIG_PREFIX + "Mapping.E-Mail").get();
        if (attributeID.equals(mapEMail) && (user.getEMailAddress() == null)) {
            LOGGER.debug("User " + user.getUserName() + " e-mail = " + attributeValue);
            user.setEMail(attributeValue);
        }
    }

    private static void setUserRealName(MCRUser user, String attributeID, String attributeValue) {
        String mapName = MCRConfiguration2.getString(CONFIG_PREFIX + "Mapping.Name").get();
        if (attributeID.equals(mapName) && (user.getRealName() == null)) {
            attributeValue = formatName(attributeValue);
            LOGGER.debug("User " + user.getUserName() + " name = " + attributeValue);
            user.setRealName(attributeValue);
        }
    }

    /** Formats a user name into "lastname, firstname" syntax. */
    private static String formatName(String name) {
        name = name.replaceAll("\\s+", " ").trim();
        if (name.contains(",")) {
            return name;
        }
        int pos = name.lastIndexOf(' ');
        if (pos == -1) {
            return name;
        }
        return name.substring(pos + 1, name.length()) + ", " + name.substring(0, pos);
    }

    /**
     * Uses specific mycore.properties configuration to add a MCRUser dynamically to groups/roles
     * Example:
     * # Mapping of any attribute.value combination to group membership of user
     * # eduPersonScopedAffiliation may be faculty|staff|employee|student|alum|member|affiliate
     * MCR.user2.LDAP.Mapping.Group.eduPersonScopedAffiliation.staff@uni-duisburg-essen.de=submitter *
     * @param mcrUser the MCRUser that shall be added to the configured groups/roles if the corresponding
     *                LDAP-Attributes exist
     * @param ldapUser the LDAPObject/User from which the LDAP-Attributes should be used to map against the configured
     *                 groups/roles for the MCRUser
     */
    public static void addMCRUserToDynamicGroups(MCRUser mcrUser, LDAPObject ldapUser) {
        for(Map.Entry<String, String> attributeEntry : ldapUser.getAttributes().entries()) {
            String attributeID = attributeEntry.getKey();
            String attributeValue = attributeEntry.getValue();
            addToGroup(mcrUser, attributeID, attributeValue);
        }
    }

    private static void addToGroup(MCRUser mcrUser, String attributeID, String attributeValue) {
        String groupMapping = CONFIG_PREFIX + "Mapping.Group." + attributeID + "." + attributeValue;
        String group = MCRConfiguration2.getString(groupMapping).orElse(null);
        if ((group != null) && (!mcrUser.isUserInRole((group)))) {
            LOGGER.info("Add user " + mcrUser.getUserName() + " to group " + group);
            mcrUser.assignRole(group);
        }
    }
}
