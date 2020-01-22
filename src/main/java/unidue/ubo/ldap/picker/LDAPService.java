package unidue.ubo.ldap.picker;

import com.google.common.collect.Multimap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.user2.MCRUserAttribute;
import unidue.ubo.ldap.LDAPObject;
import unidue.ubo.matcher.MCRUserMatcherLDAP;
import unidue.ubo.picker.IdentityService;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

/**
 * With a configuration in mycore.properties, it is necessary to map two of the input fields of the search/pick-form,
 * "lastName" and "firstName" (see ldappidsearch.xsl) to corresponding fields of the LDAP documents, for
 * example "displayName" or "sn" or "givenName".
 *
 * The following properties in the mycore.properties are used:
 *
 * # Mapping of the "last/firstName" Search/Pick-Form-Fields of the LDAP-IdentityPicker to LDAP-Attributes
 * # Examples:
 * MCR.IdentityPicker.LDAP.SearchFormMapping.lastName=sn
 * MCR.IdentityPicker.LDAP.SearchFormMapping.firstName=givenName
 */
public class LDAPService implements IdentityService {

    private final static Logger LOGGER = LogManager.getLogger(LDAPService.class);

    private final String CONFIG_NAMEPART_FIRST_FIELD_TO_LDAP = "MCR.IdentityPicker.LDAP.SearchFormMapping.firstName";
    private final String CONFIG_NAMEPART_LAST_FIELD_TO_LDAP = "MCR.IdentityPicker.LDAP.SearchFormMapping.lastName";
    private String firstName_to_ldap;
    private String lastName_to_ldap;

    public LDAPService() {
        parseNamepartFieldsToLDAPAttributeMappingConfig();
    }

    /**
     * Reads the mappings between the "first/lastName" fields of the Search/Pick-Form and LDAP attributes
     *
     * Format:
     * MCR.IdentityPicker.LDAP.SearchFormMapping.lastName={LDAP_ATTRIBUTE}
     * MCR.IdentityPicker.LDAP.SearchFormMapping.firstName={LDAP_ATTRIBUTE}
     */
    private void parseNamepartFieldsToLDAPAttributeMappingConfig() {
        MCRConfiguration config = MCRConfiguration.instance();
        firstName_to_ldap = config.getString(CONFIG_NAMEPART_FIRST_FIELD_TO_LDAP, "cn");
        lastName_to_ldap = config.getString(CONFIG_NAMEPART_LAST_FIELD_TO_LDAP, "sn");
        LOGGER.info("Mapping input of firstName of search/pick-Form to search in LDAP Attribute: {}",
                firstName_to_ldap);
        LOGGER.info("Mapping input of lastName of search/pick-Form to search in LDAP Attribute: {}",
                lastName_to_ldap);
    }

     /**
      * Takes a map of parameters that will be used for the search for documents (persons) in LDAP
      * and maps the specific "last/firstName" parameters to specific, configured (see Class-Doc) LDAP-Attributes.
      *
      * @param paramMap a map of parameters for the search of documents (persons) in LDAP
      * @return a new paramMap, where "first/lastName"-keys are replaced/mapped to a configured LDAP-
      * Attribute
      */
     private Map<String, String> applyNamepartMapping(Map<String, String> paramMap) {
         Map<String, String> transformedMap = new HashMap<>();
         for(Map.Entry<String, String> paramEntry : paramMap.entrySet()) {
             String paramName = paramEntry.getKey();
             String paramValue = paramEntry.getValue();
             String transformedParamName = "";
             if(paramName.equals("firstName")) {
                 transformedParamName = firstName_to_ldap;
             } else if(paramName.equals("lastName")) {
                 transformedParamName = lastName_to_ldap;
             } else {
                 transformedParamName = paramName;
             }
             transformedMap.put(transformedParamName, paramValue);
         }
         return transformedMap;
     }

    @Override
    public Element getPersonDetails(Map<String, String> paramMap) {
        return null;
    }


    /**
     *
     * Format of returned XML:
     * <results>
     *    <person>
     *       <attribute1>some_value1</attribute1>
     *       <attribute2>some_value2</attribute2>
     *    </person>
     *    <person>
     *       <attribute1>other_value1</attribute1>
     *       <attribute2>other_value2</attribute2>
     *    </person>
     * </results>
     */
    @Override
    public Element searchPerson(Map<String, String> paramMap) {
        LOGGER.info("Starting LDAP person search with raw params: {}", paramMap);
        paramMap = applyNamepartMapping(paramMap);
        LOGGER.info("LDAP person search params after namepart-mapping: {}", paramMap);

        Element results = new Element("results");

        MCRUserMatcherLDAP userMatcher = new MCRUserMatcherLDAP();
        Multimap<String, String> ldapAttributes = userMatcher.convertNameIdentifiersToLDAP(paramMap, false);
        LOGGER.info("attributes to search with: {}", ldapAttributes);

        List<LDAPObject> ldapUsers = userMatcher.getLDAPUsersByGivenLDAPAttributes(ldapAttributes);

        for(LDAPObject ldapUser : ldapUsers) {
            Element person = new Element("person");
            SortedSet<MCRUserAttribute> mcrAttributes = userMatcher.convertLDAPAttributesToMCRUserAttributes(ldapUser);
            for(MCRUserAttribute mcrAttribute : mcrAttributes) {
                String mcrAttributeName = mcrAttribute.getName();
                // as convertLDAPAttributesToMCRUserAttributes returns the user attributes with a leading "id_"-substring
                // but we dont want that in our results-xml, we have to remove it
                mcrAttributeName.replace("id_", "");
                String attributeValue = mcrAttribute.getValue();
                person.addContent(new Element(mcrAttributeName.toLowerCase()).setText(attributeValue.trim()));
            }
            // when converting LDAP attributes to MCRUserAttributes, any non-configured attributes might get lost
            // at this point we want to make sure, that the first- and lastname get delivered
            addElementsFromLDAPUserAttributes("firstName", firstName_to_ldap, person, ldapUser);
            addElementsFromLDAPUserAttributes("lastName", lastName_to_ldap, person, ldapUser);

            results.addContent(person);
        }

        return results;
    }

    /**
     * TODO: doc
     */
    private void addElementsFromLDAPUserAttributes(String elementName, String ldapAttributeName, Element parent, LDAPObject ldapUser) {
        if(ldapUser.getAttributes().containsKey(ldapAttributeName)) {
            Collection<String> elementValues = ldapUser.getAttributes().get(ldapAttributeName);
            for(String elementValue : elementValues) {
                parent.addContent(new Element(elementName).setText(elementValue));
            }
        }
    }
}
