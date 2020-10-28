package unidue.ubo.ldap;

import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.config.MCRConfiguration;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;


/**
 * Given a current (authenticated/active) DirContext, manage searches against the connected LDAP server, returning
 * a list of LDAPObjects.
 *
 * # Global DN used for LDAP search (should be user-independent DN)
 * MCR.user2.LDAP.GlobalDN=dc=example,dc=org
 *
 * @author Pascal Rost
 */
public class LDAPSearcher {

    private static final Logger LOGGER = LogManager.getLogger();

    public List<LDAPObject> searchWithGlobalDN(DirContext ctx, String filter) throws NamingException {
        MCRConfiguration config = MCRConfiguration.instance();
        String globalDN = config.getString(LDAPAuthenticator.CONFIG_PREFIX + "GlobalDN");

        return search(ctx, globalDN, filter);
    }

    public List<LDAPObject> search(DirContext ctx, String principal, String filter) throws NamingException {

        List<LDAPObject> ldapObjects = new ArrayList<>();

        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        NamingEnumeration<SearchResult> results = ctx.search(principal, filter, controls);

        while (results.hasMore()) {
            SearchResult searchResult = results.next();
            Attributes attributes = searchResult.getAttributes();
            Multimap<String, String> attributeMultiMap = ArrayListMultimap.create();

            for (NamingEnumeration<String> attributeIDs = attributes.getIDs(); attributeIDs.hasMore();) {
                String attributeID = attributeIDs.next();
                Attribute attribute = attributes.get(attributeID);

                for (NamingEnumeration<?> values = attribute.getAll(); values.hasMore();) {
                    String attributeValue = values.next().toString();
                    attributeMultiMap.put(attributeID, attributeValue);
                }
            }

            ldapObjects.add(new LDAPObject(searchResult, attributeMultiMap));
        }

        return ldapObjects;
    }
}
