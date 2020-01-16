package unidue.ubo.ldap.picker;

import com.google.common.collect.Multimap;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import unidue.ubo.ldap.LDAPObject;
import unidue.ubo.matcher.MCRUserMatcherLDAP;
import unidue.ubo.picker.IdentityService;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class LDAPService implements IdentityService {

    private final static Logger LOGGER = LogManager.getLogger(LDAPService.class);

    private static final String LDAP_FIRSTNAME_ATTRIBUTE = "cn";
    private static final String LDAP_LASTNAME_ATTRIBUTE = "sn";

    @Override
    public Element getPersonDetails(Map<String, String> paramMap) {
        return null;
    }

    @Override
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
    public Element searchPerson(Map<String, String> paramMap) {
        Element results = new Element("results");

        MCRUserMatcherLDAP userMatcher = new MCRUserMatcherLDAP();
        Multimap<String, String> ldapAttributes = userMatcher.convertNameIdentifiersToLDAP(paramMap, false);
        LOGGER.info("attributes to search with: {}", ldapAttributes);

        List<LDAPObject> ldapUsers = userMatcher.getLDAPUsersByGivenLDAPAttributes(ldapAttributes);

        for(LDAPObject ldapUser : ldapUsers) {
            Element person = new Element("person");
            Map<String, String> mcrAttributes = userMatcher.convertLDAPAttributesToMCRUserAttributes(ldapUser);
            for(Map.Entry<String, String> mcrAttributeEntry : mcrAttributes.entrySet()) {
                String mcrAttributeName = mcrAttributeEntry.getKey();
                String attributeName = mcrAttributeName.startsWith("id_") ?
                        mcrAttributeName.replace("id_", "") : mcrAttributeName;
                String attributeValue = mcrAttributeEntry.getValue();
                person.addContent(new Element(mcrAttributeName.toLowerCase()).setText(attributeValue.trim()));
            }
            // when converting LDAP attributes to MCRUserAttributes, any non-configured attributes might get lost
            // at this point we want to make sure, that the first- and surname get delivered
            addElementsFromLDAPUserAttributes("firstName", LDAP_FIRSTNAME_ATTRIBUTE, person, ldapUser);
            addElementsFromLDAPUserAttributes("lastName", LDAP_LASTNAME_ATTRIBUTE, person, ldapUser);

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