package unidue.ubo.ldap;

import com.google.common.collect.Multimap;
import javax.naming.directory.SearchResult;

/**
 * Abstraction class of an LDAP entity.
 *
 * @author Pascal Rost
 */
public class LDAPObject {

    private SearchResult searchResult;
    private Multimap<String, String> attributeMultiMap;

    public LDAPObject(SearchResult searchResult, Multimap<String, String> attributeMultiMap) {
        this.searchResult = searchResult;
        this.attributeMultiMap = attributeMultiMap;
    }

    public Multimap<String, String> getAttributes() {
        return this.attributeMultiMap;
    }
}
