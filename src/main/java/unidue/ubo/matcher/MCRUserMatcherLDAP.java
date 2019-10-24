package unidue.ubo.matcher;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Multimap;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.common.config.MCRConfigurationException;
import org.mycore.user2.MCRUser;
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
 * Given a MCRUser match against the users of a LDAP-Server, enriching the attributes of the MCRUser by any matched
 * LDAP-Users attributes.
 *
 * The following properties in the mycore.properties are used:
 *
 * # LDAP attribute name used for login
 * # MCR.user2.LDAP.Login.AttributeName=$ATTRIBUTE_NAME
 * Example: MCR.user2.LDAP.Login.AttributeName=uid
 *
 * # Mappings between MyCoRe identifiers and LDAP attributes (for labeledUri)
 * # MCR.user2.LDAP.Mapping.labeledURI.$IDENTIFIER_NAME.schema=...
 * Examples (scopus):
 * MCR.user2.LDAP.Mapping.labeledURI.id_scopus.schema=https://www.scopus.com/authid/detail.uri?authorId=%s
 *
 * # Mappings between MyCoRe identifiers and LDAP attributes (for explicit attributes i.e. eduPersonOrcid)
 * MCR.user2.LDAP.Mapping.explicit=id_orcid:eduPersonOrcid
 * # Multiple mappings may be separated by ','
 * # For example: MCR.user2.LDAP.Mapping.explicit=id_orcid:eduPersonOrcid,id_his:eduPersonUniqueId
 *
 * @author Pascal Rost
 */
public class MCRUserMatcherLDAP implements MCRUserMatcher {

    private final static Logger LOGGER = LogManager.getLogger(MCRUserMatcherLDAP.class);

    private final static String CONFIG_LDAP_LOGIN_ATTRIBUTENAME = "MCR.user2.LDAP.Login.AttributeName";

    // all members regarding configuration of explicit mods/mycore nameIdentifier mapping
    private final static String CONFIG_EXPLICIT_NAMEIDENTIFIER_MAPPING = "MCR.user2.LDAP.Mapping.explicit";
    private BiMap<String, String> mycoreToLDAPIdentifiers = HashBiMap.create();

    // all members regarding configuration of LDAP attribute "labeledURI" mapping
    private final static String CONFIG_LABELEDURI_PROPERTY_KEY = "MCR.user2.LDAP.Mapping.labeledURI";
    private final static String CONFIG_SCHEMA_PROPERTY = "schema";
    private Map<String, String> mycoreToLDAPLabeledURISchemas = new HashMap<>();

    public MCRUserMatcherLDAP() {
        loadLDAPMappingConfiguration();
    }

    private void loadLDAPMappingConfiguration() {
        parseLDAPExplicitNameIdentifierConfig();
        parseLDAPLabeledURINameIdentifierConfig();
    }

    /**
     * Reads the mappings between MyCoRe identifiers and LDAP attributes for explicit attributes i.e.
     * eduPersonOrcid from the mycore.properties.
     *
     * Format:
     * MCR.user2.LDAP.Mapping.explicit=id_orcid:eduPersonOrcid
     * # Multiple mappings may be separated by ','
     * # For example: MCR.user2.LDAP.Mapping.explicit=id_orcid:eduPersonOrcid,id_his:eduPersonUniqueId
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
                mycoreToLDAPIdentifiers.put(mcrIdentifierName, ldapAttributeName);
            }
            LOGGER.debug("MyCoRe identifier names to LDAP attributes mapping: {}", mycoreToLDAPIdentifiers);
        }
    }

    /**
     * Reads the mappings between MyCoRe attributes/identifiers and LDAP attributes/identifiers for labeledUri from the mycore.properties.
     * Mapping is done with via "schema" configuration.
     *
     * "schemas" must contain exactly one "%s" substring (which is used to identify the identifier from the labeledUri)
     *
     * Examples of LDAP labeledUri-attributes include:
     * labeledURI: https://www.scopus.com/authid/detail.uri?authorId=1234567890
     * labeledURI: http://d-nb.info/gnd/135799082
     *
     * Format:
     * # MCR.user2.LDAP.Mapping.labeledURI.$IDENTIFIER_NAME.schema=...
     * Examples (scopus):
     * MCR.user2.LDAP.Mapping.labeledURI.id_scopus.schema=https://www.scopus.com/authid/detail.uri?authorId=%s
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
                // splitKey[5] is the identifier name in MyCoRe convention (id_scopus, id_orcid, id_gnd, ...)
                // splitKey[6] must be "schema"
                String identifierName = splitKey[5];
                String configType = splitKey[6];
                if (configType.equals(CONFIG_SCHEMA_PROPERTY)) {
                    if (!propertyValue.contains("%s")) {
                        throw new MCRConfigurationException("Schema " + propertyValue + " not valid (must contain '%s').");
                    } else if(StringUtils.countMatches(propertyValue, "%s") != 1) {
                        throw new MCRConfigurationException("Schema " + propertyValue + " not valid (must contain single '%s').");
                    }
                    mycoreToLDAPLabeledURISchemas.put(identifierName, propertyValue);
                } else {
                    throw new MCRConfigurationException("Property key " + propertyKey + " not valid.");
                }
            }
            LOGGER.debug("LDAP labeledURI schemas: {}", mycoreToLDAPLabeledURISchemas);
        }
    }

    @Override
    public MCRUser matchUser(MCRUser mcrUser) {

        Multimap<String, String> ldapAttributes = convertUserAttributesToLDAP(mcrUser);
        List<LDAPObject> ldapUsers = getLDAPUsersByGivenLDAPAttributes(ldapAttributes);

        if(ldapUsers.size() == 0) {
            // no match found, do nothing, return given user unchanged
        } else if(ldapUsers.size() > 1) {
            // too many matches found (conflict)
            // TODO: return conflict message/MatchType/exception(?)
        } else if(ldapUsers.size() == 1) {
            LDAPObject ldapUser = ldapUsers.get(0);

            // Gather and convert all LDAP identifiers/attributes to MCRUser Attributes and merge them
            // at this point, since we are not using MultiMap (Multi-Attributes) in MCRUsers, existing
            // identifiers/attributes might be overwritten by the LDAP-Variant
            Map<String, String> userAttributesFromLDAP = convertLDAPAttributesToMCRUserAttributes(ldapUser);
            mcrUser.getAttributes().putAll(userAttributesFromLDAP);

            /*
            As discussed, it is the responsibility of the concrete MCRUserMatcher implementation to set the
            username of the matched/returned user, or more precisely, to enable the MCRUser to be able to login to the
            corresponding identity management system (in this case LDAP).
            Since currently (October 24th 2019), the LDAP and MyCoRe/UBO login is paired via the MCRUser username,
            we have to set the username of the MCRUser to the value of the ldap attribute of the matched LDAP user
            that is used for the login at the LDAP system.
            */
            // Because the username can not be changed once set, we have to create a new user at this point
            String userName = getUserNameFromLDAPUser(ldapUser);
            MCRUser newMcrUser =  new MCRUser(userName, MCRUserMatcherUtils.UNVALIDATED_REALM);
            newMcrUser.setAttributes(mcrUser.getAttributes());
            LOGGER.info("TEST: {}", mcrUser.getAttributes());
            mcrUser = newMcrUser;
        }

        return mcrUser;
    }

    private String getUserNameFromLDAPUser(LDAPObject ldapUser) {
        String userName = "";
        MCRConfiguration config = MCRConfiguration.instance();
        String ldapLoginAttributeName = config.getString(CONFIG_LDAP_LOGIN_ATTRIBUTENAME, "");
        if(StringUtils.isEmpty(ldapLoginAttributeName)) {
            throw new MCRConfigurationException("Property " + CONFIG_LDAP_LOGIN_ATTRIBUTENAME + " not set, can not find " +
                    "suitable name for matched MCR/LDAP user.");
        }
        if(!ldapUser.getAttributes().containsKey(ldapLoginAttributeName)) {
            throw new MCRConfigurationException("Attribute: " + ldapLoginAttributeName + "(configured with: " +
                    CONFIG_LDAP_LOGIN_ATTRIBUTENAME + ") not found in matched LDAP user, " +
                    "can not find suitable username.");
        } else {
            Collection<String> attributeValues = ldapUser.getAttributes().get(ldapLoginAttributeName);
            if(attributeValues.size() == 1) {
                userName = attributeValues.iterator().next();
            } else {
                throw new MCRConfigurationException("Attribute: " + ldapLoginAttributeName + "(configured with: " +
                        CONFIG_LDAP_LOGIN_ATTRIBUTENAME + ") has 0 or more than 1 values in matched LDAP user, " +
                        "can not find suitable username.");
            }
        }
        LOGGER.info("Got userName: {} from LDAP attribute: {}", userName, ldapLoginAttributeName);
        return userName;
    }

    /**
     * Converts MyCoRe MCRUser attributes from MyCoRe style to LDAP style.
     *
     * Example Attributes of MCRUser are (in a Map as key:value-pairs):
     * id_orcid: 0000-0002-4433-1464
     * id_scopus: 7202859778
     * id_gnd: 135799082
     *
     * Returned map as LDAP attributes of the form:
     * key: eduPersonOrcid, value: 0000-0002-4433-1464
     * key: labeledURI, value: https://www.scopus.com/authid/detail.uri?authorId=7202859778
     * key: labeledURI, value: http://d-nb.info/gnd/135799082
     *
     * @param mcrUser a MCRUser with attributes that represent nameIdentifiers in MyCoRe format/style
     * @return a Multimap where the Keys are LDAP attributes and the values are corresponding LDAP values
     */
    private Multimap<String, String> convertUserAttributesToLDAP(MCRUser mcrUser) {
        Multimap<String, String> convertedNameIdentifiers = ArrayListMultimap.create();

        for(Map.Entry<String, String> userAttributeEntry : mcrUser.getAttributes().entrySet()) {
            String attributeName = userAttributeEntry.getKey();
            String attributeValue = userAttributeEntry.getValue();

            if(mycoreToLDAPIdentifiers.containsKey(attributeName)) {
                // convert "explicit" identifiers to attributes
                String ldapAttributeName = mycoreToLDAPIdentifiers.get(attributeName);
                convertedNameIdentifiers.put(ldapAttributeName, attributeValue);
            } else {
                // if not "explicit", try via "labeledURI" mapping config
                if(mycoreToLDAPLabeledURISchemas.containsKey(attributeName)) {
                    String ldapAttributeName = "labeledURI";
                    String ldapAttributeValue = mycoreToLDAPLabeledURISchemas.get(attributeName)
                            .replace("%s", attributeValue);
                    convertedNameIdentifiers.put(ldapAttributeName, ldapAttributeValue);
                }
            }
        }
        LOGGER.debug("Converted MCRUser attributes from {} to {}", mcrUser.getAttributes(), convertedNameIdentifiers);
        return convertedNameIdentifiers;
    }

    /**
     * TODO: doc
     * TODO: rename to "convertAttributesToLDAP"
     */
    public Multimap<String, String> convertNameIdentifiersToLDAP(Map<String, String> attributes, boolean onlyMapped) {
        Map<String, String> convertedAttributes = new HashMap<>();
        Multimap<String, String> convertedNameIdentifiers = ArrayListMultimap.create();

        // allowed attributes used when onlyMapped = false, used for filtering out any non-ldap-attributes
        List<String> allowedAttributes = new ArrayList<>();
        allowedAttributes.add("cn");
        allowedAttributes.add("sn");

        for(Map.Entry<String, String> attributeEntry : attributes.entrySet()) {
            // convert nameIdentifiers to "mycore style" (prefix with "id_")
            String attributeName = "id_" + attributeEntry.getKey();
            String attributeValue = attributeEntry.getValue();

            // TODO: reduce/remove code dublication (see "convertUserAttributesToLDAP")
            if(mycoreToLDAPIdentifiers.containsKey(attributeName)) {
                // convert "explicit" identifiers to attributes
                String ldapAttributeName = mycoreToLDAPIdentifiers.get(attributeName);
                convertedNameIdentifiers.put(ldapAttributeName, attributeValue);
            } else if(mycoreToLDAPLabeledURISchemas.containsKey(attributeName)) {
                // if not "explicit", try via "labeledURI" mapping config
                String ldapAttributeName = "labeledURI";
                String ldapAttributeValue = mycoreToLDAPLabeledURISchemas.get(attributeName)
                        .replace("%s", attributeValue);
                convertedNameIdentifiers.put(ldapAttributeName, ldapAttributeValue);
            } else if(!onlyMapped) {
                // if not converting only explicitly mapped identifiers to attributes
                // still need to filter out any attributes that are not used in LDAP
                if(allowedAttributes.contains(attributeEntry.getKey())) {
                    convertedNameIdentifiers.put(attributeEntry.getKey(), attributeValue);
                }
            }
        }

        return convertedNameIdentifiers;
    }

    /**
     * Converts LDAP Attributes and their values into MCRUser attributes
     *
     * Example Attributes of an LDAPObject (LDAPUser) are:
     * eduPersonOrcid: 0000-0002-4433-1464
     * labeledURI: https://www.scopus.com/authid/detail.uri?authorId=7202859778
     * labeledURI: http://d-nb.info/gnd/135799082
     *
     * Returned map as MCRUser attributes of the form:
     * key: id_orcid, value: 0000-0002-4433-1464
     * key: id_scopus, value: 7202859778
     * key: id_gnd, value: 135799082
     *
     * @param ldapUser the LDAPObject from which the attributes shall be converted
     * @return a MCRUser attribute map
     */
    public Map<String, String> convertLDAPAttributesToMCRUserAttributes(LDAPObject ldapUser) {
        Map<String, String> userAttributes = new HashMap<>();
        Multimap<String, String> attributes = ldapUser.getAttributes();

        for(Map.Entry<String, Collection<String>> ldapAttribute : attributes.asMap().entrySet()) {
            String attributeName = ldapAttribute.getKey();
            Collection<String> attributeValues = ldapAttribute.getValue();

            // 1. "simple" attributes (i.e. eduPersonOrcid: 0000-0002-4433-1464)
            if(mycoreToLDAPIdentifiers.inverse().containsKey(attributeName)) {
                if(!attributeValues.isEmpty()) {
                    String nameIdentifier = mycoreToLDAPIdentifiers.inverse().get(attributeName);
                    // TODO: we have to use a Multimap on BOTH sides but for now we only take the first value
                    userAttributes.put(nameIdentifier, (String)attributeValues.toArray()[0]);
                }
            }
            // 2. labeledURI attributes (i.e. labeledURI: http://d-nb.info/gnd/135799082)
            if(attributeName.equals("labeledURI")) {
                for(String attributeValue : attributeValues) {
                    LDAPParsedLabeledURI parsedLabeledURI = parseLDAPLabeledURI(attributeValue);
                    if(parsedLabeledURI != null) {
                        userAttributes.put(parsedLabeledURI.getIdentifierName(), parsedLabeledURI.getIdentifierValue());
                    }
                }
            }
        }
        return userAttributes;
    }

    /**
     * Parse a given LDAP 'labeledUri'-attribute into its parts, ID (actual identifier value), identifier name and
     * the labeledUri itself.
     * Example of a labeledUri-attribute: https://www.scopus.com/authid/detail.uri?authorId=7202859778
     * Uses configuration from mycore.properties to detect the identifier type of an labeledUri and to extract the
     * actual identifier with the help of a given schema (see loadLDAPMappingConfiguration)
     *
     * @param labeledUri the value of the LDAP attribute 'labeledUri'
     * @return LDAPParsedLabeledURI containing the parsed data
     */
    private LDAPParsedLabeledURI parseLDAPLabeledURI(String labeledUri) {
        LDAPParsedLabeledURI parsedLabeledURI = null;

        for(Map.Entry<String, String> schemaEntry : mycoreToLDAPLabeledURISchemas.entrySet()) {
            String attributeName = schemaEntry.getKey();
            String labeledUriSchema = schemaEntry.getValue();
            Pattern pattern = Pattern.compile(createRegexForSchema(labeledUriSchema));
            Matcher matcher = pattern.matcher(labeledUri);
            if(matcher.find()) {
                String id = matcher.group(1); // .group(0) = whole string (labeledUri), .group(1) = capture group (id)
                LOGGER.debug("Found ID: {} with attribute name: {} in labeledUri: {}", id, attributeName, labeledUri);
                parsedLabeledURI = new LDAPParsedLabeledURI(labeledUri, attributeName, id);
                break;
            }
        }
        if(parsedLabeledURI == null) {
            LOGGER.info("Could not parse labeledUri: {}, " +
                    "see mycore.properties MCR.user2.LDAP.Mapping.labeledURI.$IDENTIFIER_NAME.schema configuration",
                    labeledUri);
        }
        return parsedLabeledURI;
    }

    /**
     * From a given schema of an 'labeledUri' or generally any URI, create a regular expression that can be used to find
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
            regex = "(.+)" + Pattern.quote(schema.replace("%s", ""));
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

    public List<LDAPObject> getLDAPUsersByGivenLDAPAttributes(Multimap ldapAttributes) {
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
