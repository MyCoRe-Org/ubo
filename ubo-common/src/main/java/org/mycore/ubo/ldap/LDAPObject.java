package org.mycore.ubo.ldap;

import javax.naming.directory.SearchResult;

import com.google.common.collect.Multimap;

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
