package org.mycore.ubo.ldap.picker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.naming.OperationNotSupportedException;

import com.google.common.collect.Multimap;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.ubo.ldap.LDAPObject;
import org.mycore.ubo.matcher.MCRUserMatcherLDAP;
import org.mycore.ubo.picker.IdentityService;
import org.mycore.ubo.picker.PersonSearchResult;
import org.mycore.user2.MCRUserAttribute;

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
 *
 * # Schema for creating an Identity String (to be shown in the result list of the LDAP-Identity-Picker)
 * # Configured value will be interpreted as a single String where every variable inside of "{}" will be replaced
 * # by the value of an LDAP-Attribute with the exact same name as the variable
 * # Examples:
 * MCR.IdentityPicker.LDAP.identitySchema={mail}; {eduPersonAffiliation}
 */
public class LDAPService implements IdentityService {

    private final static Logger LOGGER = LogManager.getLogger(LDAPService.class);

    private final String CONFIG_NAMEPART_FIRST_FIELD_TO_LDAP = "MCR.IdentityPicker.LDAP.SearchFormMapping.firstName";
    private final String CONFIG_NAMEPART_LAST_FIELD_TO_LDAP = "MCR.IdentityPicker.LDAP.SearchFormMapping.lastName";
    private final String CONFIG_IDENTITY_SCHEMA = "MCR.IdentityPicker.LDAP.identitySchema";

    private final String CONFIG_LEAD_ID = "MCR.user2.matching.lead_id";

    private String firstName_to_ldap;
    private String lastName_to_ldap;
    private String identity_schema;

    private String lead_id;

    public LDAPService() {
        parseNamepartFieldsToLDAPAttributeMappingConfig();
        parseIdentitySchemaConfig();
        lead_id = MCRConfiguration2.getStringOrThrow(CONFIG_LEAD_ID);
    }

    /**
     * Reads the mappings between the "first/lastName" fields of the Search/Pick-Form and LDAP attributes
     *
     * Format:
     * MCR.IdentityPicker.LDAP.SearchFormMapping.lastName={LDAP_ATTRIBUTE}
     * MCR.IdentityPicker.LDAP.SearchFormMapping.firstName={LDAP_ATTRIBUTE}
     */
    private void parseNamepartFieldsToLDAPAttributeMappingConfig() {
        firstName_to_ldap = MCRConfiguration2.getString(CONFIG_NAMEPART_FIRST_FIELD_TO_LDAP).orElse("cn");
        lastName_to_ldap = MCRConfiguration2.getString(CONFIG_NAMEPART_LAST_FIELD_TO_LDAP).orElse("sn");
        LOGGER.info("Mapping input of firstName of search/pick-Form to search in LDAP Attribute: {}",
                firstName_to_ldap);
        LOGGER.info("Mapping input of lastName of search/pick-Form to search in LDAP Attribute: {}",
                lastName_to_ldap);
    }

    /**
     * Reads the configurable Schema for creating the Identity String (that will be shown in the result list of the LDAP
     * -Identity-Picker page after a search)
     * Format/Examples: see class doc
     */
    private void parseIdentitySchemaConfig() {
        identity_schema = MCRConfiguration2.getString(CONFIG_IDENTITY_SCHEMA).orElse("");
        LOGGER.info("Schema to create the information of users in the identity picker result list: {}",
                identity_schema);
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
     * &lt;results&gt;
     *    &lt;person&gt;
     *       &lt;attribute1&gt;some_value1&lt;/attribute1&gt;
     *       &lt;attribute2&gt;some_value2&lt;/attribute2&gt;
     *       &lt;identity&gt;some configurable identity string (email, affiliation etc.)&lt;/identity&gt;
     *    &lt;/person&gt;
     *    &lt;person&gt;
     *       &lt;attribute1&gt;other_value1&lt;/attribute1&gt;
     *       &lt;attribute2&gt;other_value2&lt;/attribute2&gt;
     *       &lt;identity&gt;some configurable identity string (email, affiliation etc.)&lt;/identity&gt;
     *    &lt;/person&gt;
     * &lt;/results&gt;
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

        List<LDAPObject> ldapUsers = userMatcher.getLDAPUsersByGivenLDAPAttributes(ldapAttributes, userMatcher.SIMILAR_SEARCH_TEMPLATE);

        for(LDAPObject ldapUser : ldapUsers) {
            Element person = new Element("person");
            SortedSet<MCRUserAttribute> mcrAttributes = userMatcher.convertLDAPAttributesToMCRUserAttributes(ldapUser);
            for(MCRUserAttribute mcrAttribute : mcrAttributes) {
                String mcrAttributeName = mcrAttribute.getName();
                // as convertLDAPAttributesToMCRUserAttributes returns the user attributes with a leading "id_"-substring
                // but we dont want that in our results-xml, we have to remove it
                mcrAttributeName.replace("id_", "");
                String attributeValue = mcrAttribute.getValue();
                person
                    .addContent(new Element(mcrAttributeName.toLowerCase(Locale.ROOT)).setText(attributeValue.trim()));
            }
            // when converting LDAP attributes to MCRUserAttributes, any non-configured attributes might get lost
            // at this point we want to make sure, that the first- and lastname get delivered
            addElementsFromLDAPUserAttributes("firstName", firstName_to_ldap, person, ldapUser);
            addElementsFromLDAPUserAttributes("lastName", lastName_to_ldap, person, ldapUser);

            addIdentityElement(person, ldapUser);

            results.addContent(person);
        }

        return results;
    }

    @Override
    public PersonSearchResult searchPerson(String query) throws OperationNotSupportedException {
        PersonSearchResult personSearchResult = new PersonSearchResult();
        personSearchResult.personList = new ArrayList<>();
        String[] s = query.split("[ ,]", 2);
        HashMap<String, String> parms = new HashMap<>();

        parms.put(lastName_to_ldap, s[0]);
        if (s.length > 1) {
            parms.put(lastName_to_ldap, s[1]);
            parms.put(firstName_to_ldap, s[0]);
        }


        Element result = this.searchPerson(parms);

        List<Element> persons = result.getChildren("person");

        final boolean isAdministrator = Optional.ofNullable(MCRSessionMgr.getCurrentSession())
                .map(MCRSession::getUserInformation)
                .filter(user -> user.isUserInRole("admin")).isPresent();

        persons.forEach(person-> {
            PersonSearchResult.PersonResult pr = new PersonSearchResult.PersonResult(this);
            pr.firstName = person.getChildText("firstName");
            pr.lastName = person.getChildText("lastName");
            pr.displayName = ((pr.firstName != null ? pr.firstName : "") + " "
                + ((pr.lastName != null) ? pr.lastName : "")).trim();
            pr.pid = person.getChildText("id_" + lead_id);
            pr.information = new ArrayList<>();

            if(isAdministrator){
                pr.information.add(person.getChildText("mail"));
            }

            pr.information.add(person.getChildText("identity"));
            //LOGGER.info("Person: {}", new XMLOutputter().outputString(person));
            personSearchResult.personList.add(pr);
        });

        personSearchResult.count = personSearchResult.personList.size();

        // Sort results by best LevenshteinDistance from (fn + ln) or (ln + fn) to query
        LevenshteinDistance ld = new LevenshteinDistance(50);
        BiFunction<String, String, String> namePartCombiner = (n1, n2) -> Stream.of(n1, n2).filter(Objects::nonNull)
            .collect(Collectors.joining(" "));

        Function<PersonSearchResult.PersonResult, String> nameBuilder1 = (o) -> namePartCombiner.apply(o.firstName,
            o.lastName);

        Function<PersonSearchResult.PersonResult, String> nameBuilder2 = (o) -> namePartCombiner.apply(o.lastName,
            o.firstName);

        personSearchResult.personList.sort((o1, o2) -> {
            String o1Name1 = nameBuilder1.apply(o1);
            String o1Name2 = nameBuilder2.apply(o1);
            int bestForO1 = Math.min(ld.apply(query, o1Name1), ld.apply(query, o1Name2));

            String o2Name1 = nameBuilder1.apply(o2);
            String o2Name2 = nameBuilder2.apply(o2);
            int bestForO2 = Math.min(ld.apply(query, o2Name1), ld.apply(query, o2Name2));

            boolean o1StartsWith = o1Name1.startsWith(query) || o1Name2.startsWith(query);
            boolean o2StartsWith = o2Name1.startsWith(query) || o2Name2.startsWith(query);

            if(o1StartsWith && !o2StartsWith) {
                return -1000;
            } else if(!o1StartsWith && o2StartsWith) {
                return +1000;
            }

            return bestForO1 - bestForO2;
        });

        return personSearchResult;
    }

    private void addIdentityElement(Element parent, LDAPObject ldapUser) {
        String identityString = identity_schema;
        for(Map.Entry<String, String> attributeEntry : ldapUser.getAttributes().entries()) {
            String attributeName = attributeEntry.getKey();
            String attributeValue = attributeEntry.getValue();
            identityString = identityString.replace("{" + attributeName + "}", attributeValue);
        }
        String placeholdersRemoved = identityString.replaceAll("\\{.*\\}","");
        if (!placeholdersRemoved.isEmpty()) {
            parent.addContent(new Element("identity").setText(placeholdersRemoved));
        }
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
