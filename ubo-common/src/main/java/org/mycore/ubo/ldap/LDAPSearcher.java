package org.mycore.ubo.ldap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.SizeLimitExceededException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.config.MCRConfiguration2;

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

    private static int PAGE_SIZE = MCRConfiguration2.getInt("MCR.user2.LDAP.PageSize").orElse(100);

    private static final Logger LOGGER = LogManager.getLogger(LDAPSearcher.class);

    public List<LDAPObject> searchWithGlobalDN(LdapContext ctx, String filter) throws NamingException {
        String globalDN = MCRConfiguration2.getString(LDAPAuthenticator.CONFIG_PREFIX + "GlobalDN").get();

        return search(ctx, globalDN, filter);
    }

    public List<LDAPObject> search(LdapContext ctx, String principal, String filter) throws NamingException {
        List<LDAPObject> ldapObjects = new ArrayList<>();
        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        byte[] cookie = null;

        try {
            ctx.setRequestControls(new Control[] { new PagedResultsControl(PAGE_SIZE, Control.NONCRITICAL) });

            do {
                NamingEnumeration<SearchResult> results = ctx.search(principal, filter, controls);
                while (results != null && results.hasMore()) {
                    SearchResult searchResult = results.next();
                    Attributes attributes = searchResult.getAttributes();
                    Multimap<String, String> attributeMultiMap = ArrayListMultimap.create();

                    for (NamingEnumeration<String> attributeIDs = attributes.getIDs(); attributeIDs.hasMore(); ) {
                        String attributeID = attributeIDs.next();
                        Attribute attribute = attributes.get(attributeID);

                        for (NamingEnumeration<?> values = attribute.getAll(); values.hasMore(); ) {
                            String attributeValue = values.next().toString();
                            attributeMultiMap.put(attributeID, attributeValue);
                        }
                    }

                    ldapObjects.add(new LDAPObject(searchResult, attributeMultiMap));
                }

                Control[] responseControls = ctx.getResponseControls();
                if (responseControls != null) {
                    for (int i = 0; i < responseControls.length; i++) {
                        if (responseControls[i] instanceof PagedResultsResponseControl) {
                            PagedResultsResponseControl responseControl = (PagedResultsResponseControl) responseControls[i];
                            cookie = responseControl.getCookie();
                        }
                    }
                }
                ctx.setRequestControls(new Control[] { new PagedResultsControl(PAGE_SIZE, cookie, Control.CRITICAL) });
            } while (cookie != null);

        } catch (SizeLimitExceededException slee) {
            LOGGER.error("LDAP size limit allegedly exceeded", slee);
        } catch (IOException ioe) {
            LOGGER.error("Error setting setting RequestControls to ldap context", ioe);
        }

        return ldapObjects;
    }
}
