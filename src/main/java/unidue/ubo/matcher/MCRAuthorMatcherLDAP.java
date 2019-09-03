package unidue.ubo.matcher;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Multimap;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.common.config.MCRConfigurationException;
import org.mycore.orcid.user.MCRORCIDUser;
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUserManager;
import unidue.ubo.ldap.LDAPAuthenticator;
import unidue.ubo.ldap.LDAPObject;
import unidue.ubo.ldap.LDAPParsedLabeledURI;
import unidue.ubo.ldap.LDAPSearcher;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Given a mods:name-element (as an author of a publication in MODS-format) match against the users of a LDAP-Server and
 * the local MCRUsers, returning either a new MCRUser or an already existing one.
 * When an author is matched against exactly one LDAP-user, the combined identifiers of both the mods:name (author)
 * element as well as the LDAP-user-attributes are used to match against the local MCRUsers. In case a match against
 * a local MCRUser is found, the MCRUsers attributes are enriched and the matched MCRUser is returned. Otherwise, a new
 * MCRUser with a special realm is created (the realm denotes the necessity to validate the user).
 *
 * The following properties in the mycore.properties are used:
 *
 * # Mappings between mods/mycore identifiers and LDAP attributes (for labeledUri)
 * # MCR.user2.LDAP.Mapping.labeledURI.$IDENTIFIER_NAME.schema=...
 * # MCR.user2.LDAP.Mapping.labeledURI.$IDENTIFIER_NAME.detection=...
 * Examples (scopus):
 * MCR.user2.LDAP.Mapping.labeledURI.scopus.schema=https://www.scopus.com/authid/detail.uri?authorId=%s
 * MCR.user2.LDAP.Mapping.labeledURI.scopus.detection=https://www.scopus.com/authid/detail.uri?authorId=
 *
 * # Mappings between mods/mycore identifiers and LDAP attributes (for explicit attributes i.e. eduPersonOrcid)
 * MCR.user2.LDAP.Mapping.explicit=orcid:eduPersonOrcid
 * # Multiple mappings may be separated by ','
 * # For example: MCR.user2.LDAP.Mapping.explicit=orcid:eduPersonOrcid,his:eduPersonUniqueId
 *
 * @author Pascal Rost
 */
public class MCRAuthorMatcherLDAP implements MCRAuthorMatcher {

    private final static Logger LOGGER = LogManager.getLogger(MCRAuthorMatcherLDAP.class);

    private final static String UNVALIDATED_REALM = "ude"; // TODO: create specific realm for unvalidated users

    // all members regarding configuration of explicit mods/mycore nameIdentifier mapping
    private final static String CONFIG_EXPLICIT_NAMEIDENTIFIER_MAPPING = "MCR.user2.LDAP.Mapping.explicit";
    private BiMap<String, String> modsToLDAPIdentifiers = HashBiMap.create(); // maps between mods/mycore and LDAP identifiers

    // all members regarding configuration of LDAP attribute "labeledURI" mapping
    private final static String CONFIG_LABELEDURI_PROPERTY_KEY = "MCR.user2.LDAP.Mapping.labeledURI";
    private final static String CONFIG_SCHEMA_PROPERTY = "schema";
    private final static String CONFIG_DETECTION_PROPERTY = "detection";
    private Map<String, String> modsToLDAPLabeledURISchemas = new HashMap<>();
    private Map<String, String> modsToLDAPLabeledURIDetectors = new HashMap<>();

    public MCRAuthorMatcherLDAP() {
        loadLDAPMappingConfiguration();
    }

    private void loadLDAPMappingConfiguration() {
        parseLDAPExplicitNameIdentifierConfig();
        parseLDAPLabeledURINameIdentifierConfig();
    }

    /**
     * Reads the mappings between mods/mycore identifiers and LDAP attributes for explicit attributes i.e.
     * eduPersonOrcid from the mycore.properties.
     *
     * Format:
     * MCR.user2.LDAP.Mapping.explicit=orcid:eduPersonOrcid
     * # Multiple mappings may be separated by ','
     * # For example: MCR.user2.LDAP.Mapping.explicit=orcid:eduPersonOrcid,his:eduPersonUniqueId
     */
    private void parseLDAPExplicitNameIdentifierConfig() {
        MCRConfiguration config = MCRConfiguration.instance();
        String explicitNameIdentifierConfig = config.getString(CONFIG_EXPLICIT_NAMEIDENTIFIER_MAPPING, "");
        if(StringUtils.isNotEmpty(explicitNameIdentifierConfig)) {
            String[] splitConfigValueParts = explicitNameIdentifierConfig.split(",");
            for (int i = 0; i < splitConfigValueParts.length; i++) {
                String[] splitConfigValue = splitConfigValueParts[i].split(":");
                if (splitConfigValue.length != 2) {
                    throw new MCRConfigurationException("Property key " + CONFIG_EXPLICIT_NAMEIDENTIFIER_MAPPING + " not valid.");
                }
                String mcrIdentifierName = splitConfigValue[0];
                String ldapAttributeName = splitConfigValue[1];
                modsToLDAPIdentifiers.put(mcrIdentifierName, ldapAttributeName);
            }
            LOGGER.debug("MODS/MyCoRe identifier names to LDAP attributes mapping: {}", modsToLDAPIdentifiers);
        }
    }

    /**
     * Reads the mappings between mods/mycore identifiers and LDAP attributes for labeledUri from the mycore.properties.
     * Mapping is done with "detection" and "schema" configuration.
     *
     * "schemas" must contain exactly one "%s" substring (which is used to identify the identifier from the labeledUri)
     *
     * Examples of LDAP labeledUri-attributes include:
     * labeledURI: https://www.scopus.com/authid/detail.uri?authorId=1234567890
     * labeledURI: http://d-nb.info/gnd/135799082
     *
     * Format:
     * # MCR.user2.LDAP.Mapping.labeledURI.$IDENTIFIER_NAME.schema=...
     * # MCR.user2.LDAP.Mapping.labeledURI.$IDENTIFIER_NAME.detection=...
     * Examples (scopus):
     * MCR.user2.LDAP.Mapping.labeledURI.scopus.schema=https://www.scopus.com/authid/detail.uri?authorId=%s
     * MCR.user2.LDAP.Mapping.labeledURI.scopus.detection=https://www.scopus.com/authid/detail.uri?authorId=
     *
     */
    private void parseLDAPLabeledURINameIdentifierConfig() {
        MCRConfiguration config = MCRConfiguration.instance();
        Map<String, String> nameIdentifierConfigMap = config.getPropertiesMap(CONFIG_LABELEDURI_PROPERTY_KEY);

        if(!nameIdentifierConfigMap.isEmpty()) {
            for (Map.Entry<String, String> nameIdentifierConfig : nameIdentifierConfigMap.entrySet()) {
                String propertyKey = nameIdentifierConfig.getKey();
                String propertyValue = nameIdentifierConfig.getValue();
                String[] splitKey = propertyKey.split("\\.");
                if (splitKey.length != 7) {
                    throw new MCRConfigurationException("Property key " + propertyKey + " not valid.");
                }
                // splitKey[5] is the identifier name in mods/mycore convention (scopus, orcid, gnd, ...)
                // splitKey[6] must be either "schema" or "detection" (type of configuration)
                String identifierName = splitKey[5];
                String configType = splitKey[6];
                if ((!configType.equals(CONFIG_SCHEMA_PROPERTY)) && (!configType.equals(CONFIG_DETECTION_PROPERTY))) {
                    throw new MCRConfigurationException("Property key " + propertyKey + " not valid.");
                }
                if (configType.equals(CONFIG_SCHEMA_PROPERTY)) {
                    if (!propertyValue.contains("%s")) {
                        throw new MCRConfigurationException("Schema " + propertyValue + " not valid (must contain '%s').");
                    } else if(StringUtils.countMatches(propertyValue, "%s") != 1) {
                        throw new MCRConfigurationException("Schema " + propertyValue + " not valid (must contain single '%s').");
                    }

                    modsToLDAPLabeledURISchemas.put(identifierName, propertyValue);
                }
                if (configType.equals(CONFIG_DETECTION_PROPERTY)) {
                    modsToLDAPLabeledURIDetectors.put(identifierName, propertyValue);
                }
            }
            LOGGER.debug("LDAP labeledURI schemas: {}", modsToLDAPLabeledURISchemas);
            LOGGER.debug("LDAP labeledURI detectors: {}", modsToLDAPLabeledURIDetectors);
        }
    }

    @Override
    public MCRUser matchModsAuthor(Element modsAuthor) {

        MCRUser mcrUser = null;
        LDAPObject ldapUser = null;

        Map<String, String> nameIdentifiers = MCRAuthorMatcherUtils.getNameIdentifiers(modsAuthor);
        Multimap<String, String> ldapAttributes = convertNameIdentifiersToLDAP(nameIdentifiers);

        List<LDAPObject> ldapUsers = getLDAPUsersByGivenLDAPAttributes(ldapAttributes);

        if(ldapUsers.size() == 0) {
            // no match found
            // TODO: return appropriate message/MatchType/exception(?)
        } else if(ldapUsers.size() > 1) {
            // to many matches found (conflict)
            // TODO: return conflict message/MatchType/exception(?)
        } else if(ldapUsers.size() == 1) {
            ldapUser = ldapUsers.get(0);

            // 1. gather/merge all nameIdentifiers (MyCoRe schema)
            nameIdentifiers.putAll(convertLDAPAttributesToNameIdentifiers(ldapUser));

            // 2. Match with local users (and enrich their attributes if possible)
            mcrUser = matchWithLocalUsers(nameIdentifiers);

            if(mcrUser == null) {
                // if not matched with local user:
                // create new user with new realm
                // TODO: secure against NullPointerException and decide which name to use for new user and from which attribute
                String newUserName = (String) ldapUser.getAttributes().get("cn").toArray()[0];
                mcrUser = createNewMCRUser(newUserName, nameIdentifiers);
            }
        }

        return mcrUser;
    }

    private MCRUser matchWithLocalUsers(Map<String, String> nameIdentifiers) {
        return new MCRAuthorMatcherLocal().matchByNameIdentifiers(nameIdentifiers);
    }

    private MCRUser createNewMCRUser(String userName, Map<String, String> attributes) {
        // append prefix to attributes
        Map<String, String> prefixed_attributes = new HashMap<>();
        for(Map.Entry<String, String> attribute : attributes.entrySet()) {
            prefixed_attributes.put(MCRORCIDUser.ATTR_ID_PREFIX + attribute.getKey(), attribute.getValue());
        }
        LOGGER.debug("Creating new MCRUser with userName: {}, realm: {} and attributes: {}", userName, UNVALIDATED_REALM, prefixed_attributes);
        MCRUser newUser = new MCRUser(userName, UNVALIDATED_REALM);
        newUser.setAttributes(prefixed_attributes);
        MCRUserManager.createUser(newUser);
        return newUser;
    }

    /**
     * Converts identifier names and values (derived from mods:nameIdentifier) from mods/mycore style to LDAP style.
     *
     * Example Attributes of nameIdentifiers (mods/mycore) are (in a Map as key:value-pairs):
     * ORCID: 0000-0002-4433-1464
     * Scopus: 7202859778
     * GND: 135799082
     *
     * Returned map as LDAP attributes of the form:
     * key: eduPersonOrcid, value: 0000-0002-4433-1464
     * key: labeledURI, value: https://www.scopus.com/authid/detail.uri?authorId=7202859778
     * key: labeledURI, value: http://d-nb.info/gnd/135799082
     *
     * @param nameIdentifiers a Map of identifier names to values in mods/mycore "style"
     * @return a Multimap where the Keys are LDAP attributes and the values are corresponding LDAP values
     */
    private Multimap<String, String> convertNameIdentifiersToLDAP(Map<String, String> nameIdentifiers) {
        Multimap<String, String> convertedNameIdentifiers = ArrayListMultimap.create();

        for(Map.Entry<String, String> nameIdentifierEntry : nameIdentifiers.entrySet()) {
            String identifierName = nameIdentifierEntry.getKey();
            String identifierValue = nameIdentifierEntry.getValue();

            if(modsToLDAPIdentifiers.containsKey(identifierName)) {
                // convert "explicit" identifiers to attributes
                String ldapAttributeName = modsToLDAPIdentifiers.get(identifierName);
                convertedNameIdentifiers.put(ldapAttributeName, identifierValue);
            } else {
                // if not "explicit", try via "labeledURI" mapping config
                if(modsToLDAPLabeledURISchemas.containsKey(identifierName)) {
                    String ldapAttributeName = "labeledURI";
                    String ldapAttributeValue = modsToLDAPLabeledURISchemas.get(identifierName)
                            .replace("%s", identifierValue);
                    convertedNameIdentifiers.put(ldapAttributeName, ldapAttributeValue);
                }
            }
        }
        LOGGER.debug("Converted nameIdentifiers from {} to {}", nameIdentifiers, convertedNameIdentifiers);
        return convertedNameIdentifiers;
    }

    /**
     * Converts LDAP Attributes and their values into a ("normalized") "nameIdentifierMap" with mods/mycore convention.
     *
     * Example Attributes of an LDAPObject (LDAPUser) are:
     * eduPersonOrcid: 0000-0002-4433-1464
     * labeledURI: https://www.scopus.com/authid/detail.uri?authorId=7202859778
     * labeledURI: http://d-nb.info/gnd/135799082
     *
     * Returned map in mods/mycore convention of the form:
     * key: orcid, value: 0000-0002-4433-1464
     * key: scopus, value: 7202859778
     * key: gnd, value: 135799082
     *
     * @param ldapUser the LDAPObject from which the attributes shall be converted
     * @return a "normalized" "nameIdentifierMap" in mods/mycore convention
     */
    private Map<String, String> convertLDAPAttributesToNameIdentifiers(LDAPObject ldapUser) {
        Map<String, String> nameIdentifiers = new HashMap<>();
        Multimap<String, String> attributes = ldapUser.getAttributes();

        for(Map.Entry<String, Collection<String>> ldapAttribute : attributes.asMap().entrySet()) {
            String attributeName = ldapAttribute.getKey();
            Collection<String> attributeValues = ldapAttribute.getValue();

            // 1. "simple" attributes (i.e. eduPersonOrcid: 0000-0002-4433-1464
            if(modsToLDAPIdentifiers.inverse().containsKey(attributeName)) {
                if(!attributeValues.isEmpty()) {
                    String nameIdentifier = modsToLDAPIdentifiers.inverse().get(attributeName);
                    // TODO: we have to use a Multimap on BOTH sides but for now we only take the first value
                    nameIdentifiers.put(nameIdentifier, (String)attributeValues.toArray()[0]);
                }
            }
            // 2. labeledURI attributes (i.e. labeledURI: http://d-nb.info/gnd/135799082)
            if(attributeName.equals("labeledURI")) {
                for(String attributeValue : attributeValues) {
                    LDAPParsedLabeledURI parsedLabeledURI = parseLDAPLabeledURI(attributeValue);
                    if(parsedLabeledURI != null) {
                        nameIdentifiers.put(parsedLabeledURI.getIdentifierName(), parsedLabeledURI.getIdentifierValue());
                    }
                }
            }
        }
        return nameIdentifiers;
    }

    /**
     * Parse a given LDAP 'labeledUri'-attribute into its parts, ID (actual identifier value), identifier name and
     * the labeledUri itself.
     * Example of a labeledUri-attribute: https://www.scopus.com/authid/detail.uri?authorId=7202859778
     * Uses configuration from mycore.properties to detect the identifier type of an labeledUri and to extract the
     * actuall identifier with the help of a given schema (see loadLDAPMappingConfiguration)
     *
     * @param labeledUri LDAP attribute 'labeledUri'
     * @return LDAPParsedLabeledURI containing the parsed data
     */
    private LDAPParsedLabeledURI parseLDAPLabeledURI(String labeledUri) {
        LDAPParsedLabeledURI parsedLabeledURI = null;
        for(Map.Entry<String, String> detectorEntry : modsToLDAPLabeledURIDetectors.entrySet()) {
            String identifierName = detectorEntry.getKey();
            String detector = detectorEntry.getValue();
            if(labeledUri.contains(detector)) {
                if(!modsToLDAPLabeledURISchemas.containsKey(identifierName)) {
                    throw new MCRConfigurationException("'schema' missing for labeledUri 'detection' property entry: " + identifierName);
                }
                String schema = modsToLDAPLabeledURISchemas.get(identifierName);
                Pattern pattern = Pattern.compile(createRegexForSchema(schema));
                Matcher matcher = pattern.matcher(labeledUri);
                if(matcher.find()) {
                    String id = matcher.group(1); // .group(0) = whole string (labeledUri), .group(1) = capture group (id)
                    LOGGER.debug("Found ID: {} with identifier name: {} in labeledUri: {}", id, identifierName, labeledUri);
                    parsedLabeledURI = new LDAPParsedLabeledURI(labeledUri, identifierName, id);
                }
            }
        }
        return parsedLabeledURI;
    }

    /**
     * From a given schema of an 'labeledUri' or generally any URI create a regular expression that can be used to find
     * the actual identifier value in the URI.
     * Example of a schema (Scopus): https://www.scopus.com/authid/detail.uri?authorId=%s
     * Schemas must exactly contain one '%s'-substring.
     * Java-Regex special metacharacters '\Q' and '\E' are used to escape any characters surrounding the '%s'-substring.
     *
     * @param schema (URI that contains a '%s'-substring)
     * @return a regular expression in Java-Regex-Format where the first (and only) capture group captures the
     * identifier value
     */
    private String createRegexForSchema(String schema) {
        String regex = "";

        int position = schema.indexOf("%s");
        int lastPosition = schema.length() -2; // indexOf with "%s" at last position is length() -2!

        if(position == 0) {
            regex = "(-+)" + Pattern.quote(schema.replace("%s", ""));
        } else if((position > 0) && (position < lastPosition)) {
            String[] splitSchema = schema.split("%s");
            // according to convention, the resulting split array has to have a length of 2 (since there should always
            // be only one "%s" in the schema)
            if(splitSchema.length == 2) {
                regex = Pattern.quote(splitSchema[0]) + "(.+)" + Pattern.quote(splitSchema[1]);
            }
        } else if(position == lastPosition) {
            regex = Pattern.quote(schema.replace("%s", "")) + "(.+)";
        }

        LOGGER.debug("Java-Regex-Pattern for schema {} is {}", schema, regex);
        return regex;
    }

    private List<LDAPObject> getLDAPUsersByGivenLDAPAttributes(Multimap ldapAttributes) {
        DirContext ctx = null;
        List<LDAPObject> ldapUsers = new ArrayList<>();

        String ldapSearchFilter = createLDAPSearchFilter(ldapAttributes);
        try {
            ctx = new LDAPAuthenticator().authenticate();
            ldapUsers = new LDAPSearcher().searchWithGlobalDN(ctx, ldapSearchFilter);
        } catch (NamingException ex) {
            LOGGER.error("Exception occurred: " +  ex);
        } finally {
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (NamingException ex) {
                    LOGGER.warn("could not close context " + ex);
                }
            }
        }
        return ldapUsers;
    }

    /**
     * Creates a LDAP-searchfilter based on the given LDAP attributes of the form:
     * (&(objectClass=eduPerson)(|(%a1=%v1)(%a2=%v2)...(%aN=%vN))) where %a denotes the attribute name and %v the value.
     * Such a searchfilter can possibly result in multiple found LDAP entities (since the attribute name/value pairs are
     * compounded with "OR" ('|')).
     * @param ldapAttributes a Multimap where the keys are the LDAP attribute names
     * @return A LDAP-searchfilter of the form (&(objectClass=eduPerson)(|(%a1=%v1)(%a2=%v2)...(%aN=%vN)))
     */
    private String createLDAPSearchFilter(Multimap<String, String> ldapAttributes) {
        // TODO: take into consideration the member-status of the (email?) of the LDAP-users
        String searchFilterBaseTemplate = "(&(objectClass=eduPerson)(|%s))";
        String searchFilterInnerTemplate = "(%s=%s)"; // attributeName=attributeValue

        String searchFilterInner = "";
        for(Map.Entry<String, Collection<String>> ldapAttribute : ldapAttributes.asMap().entrySet()) {
            String attributeName = ldapAttribute.getKey();
            Collection<String> attributeValues = ldapAttribute.getValue();
            for(String attributeValue : attributeValues) {
                String attributeFilter = String.format(searchFilterInnerTemplate, attributeName, attributeValue);
                searchFilterInner += attributeFilter;
            }
        }
        return String.format(searchFilterBaseTemplate, searchFilterInner);
    }
}
