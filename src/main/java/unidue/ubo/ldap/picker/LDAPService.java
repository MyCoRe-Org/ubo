package unidue.ubo.ldap.picker;

import com.google.common.collect.Multimap;
import org.jdom2.Element;
import unidue.ubo.ldap.LDAPObject;
import unidue.ubo.matcher.MCRUserMatcherLDAP;
import unidue.ubo.picker.IdentityService;

import java.util.List;
import java.util.Map;

public class LDAPService implements IdentityService {

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
        Multimap<String, String> ldapAttributes = userMatcher.convertNameIdentifiersToLDAP(paramMap);

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
            results.addContent(person);
        }

        return results;
    }
}
