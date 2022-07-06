package org.mycore.ubo.ldap;

import java.util.Hashtable;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.user2.MCRUser2Constants;

/**
 * Manages the authentication and connection to a configured LDAP server.
 * Configuration is done via mycore.properties:
 *
 * # LDAP server
 * MCR.user2.LDAP.ProviderURL=ldaps://ldap2.uni-duisburg-essen.de
 *
 * # Timeout when connecting to LDAP server
 * MCR.user2.LDAP.ReadTimeout=5000
 *
 * # Base DN, uid of user on actual login will be used!
 * MCR.user2.LDAP.BaseDN=uid=%s,ou=people,dc=uni-duisburg-essen,dc=de
 *
 * # Global User used for LDAP authentication (should be uid of LDAP 'global read-only user')
 * MCR.user2.LDAP.GlobalUser=admin
 *
 * # Global Password used for LDAP authentication (should be password of LDAP 'global read-only user')
 * MCR.user2.LDAP.GlobalPassword=*****
 *
 * @author Pascal Rost, Frank L\u00FCtzenkirchen
 */
public class LDAPAuthenticator {

    /** If this pattern is given in LDAP error message, login is invalid */
    private static final String PATTERN_INVALID_CREDENTIALS = "error code 49";

    private static final Logger LOGGER = LogManager.getLogger();

    public static final String CONFIG_PREFIX = MCRUser2Constants.CONFIG_PREFIX + "LDAP.";

    /** Base DN, uid of user on actual login will be used! */
    private String baseDN;

    /** user (uid) of global readonly user **/
    private String globalUid;

    /** dn of global readonly user **/
    private final String globalUDN;

    /** password of global readonly user **/
    private String globalPassword;

    /** LDAP configuration template */
    private Hashtable<String, String> ldapEnvironment;

    public LDAPAuthenticator() {
        ldapEnvironment = new Hashtable<>();
        ldapEnvironment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");

        String readTimeout = MCRConfiguration2.getString(CONFIG_PREFIX + "ReadTimeout").orElse("10000");
        ldapEnvironment.put("com.sun.jndi.ldap.read.timeout", readTimeout);

        String providerURL = MCRConfiguration2.getString(CONFIG_PREFIX + "ProviderURL").get();
        ldapEnvironment.put(Context.PROVIDER_URL, providerURL);
        if (providerURL.startsWith("ldaps")) {
            ldapEnvironment.put(Context.SECURITY_PROTOCOL, "ssl");
        }

        ldapEnvironment.put(Context.SECURITY_AUTHENTICATION, "simple");

        baseDN = MCRConfiguration2.getString(CONFIG_PREFIX + "BaseDN").get();

        globalUid = MCRConfiguration2.getString(CONFIG_PREFIX + "GlobalUser").get();

        globalUDN = MCRConfiguration2.getString(CONFIG_PREFIX + "GlobalUserDN").orElse(baseDN);

        globalPassword = MCRConfiguration2.getString(CONFIG_PREFIX + "GlobalPassword").get();
    }

    /**
     * Authenticate the User with a given uid and password
     * @param uid The uid of the user on the LDAP-Server
     * @param credentials The password of the user on the LDAP-Server
     * @return DirContext, the context of the connected LDAP-Directory
     */
    public LdapContext authenticate(String uid, String credentials) throws NamingException {
        return getLDAPContext(uid, credentials);
    }

    /**
     * Authenticate the User, using configured Global-Settings (GlobalUser, GlobalPassword)
     * @return DirContext, the context of the connected LDAP-Directory
     */
    public LdapContext authenticate() throws NamingException {
        return getLDAPContext(globalUid, globalPassword, globalUDN);
    }

    @SuppressWarnings("unchecked")
    private LdapContext getLDAPContext(String uid, String credentials) throws NamingException {
        return getLDAPContext(uid, credentials, baseDN);
    }

    @SuppressWarnings("unchecked")
    private LdapContext getLDAPContext(String uid, String credentials, String dn) throws NamingException {
        try {
            Hashtable<String, String> env = (Hashtable<String, String>) (ldapEnvironment.clone());
            if (uid != null && !uid.isBlank() && credentials != null && !credentials.isBlank()) {
                env.put(Context.SECURITY_PRINCIPAL, String.format(dn, uid));
                env.put(Context.SECURITY_CREDENTIALS, credentials);
            }

            return new InitialLdapContext(env, null);
        } catch (AuthenticationException ex) {
            if (ex.getMessage().contains(PATTERN_INVALID_CREDENTIALS)) {
                LOGGER.info("Could not authenticate LDAP user " + uid + ": " + ex.getMessage());
                return null;
            } else {
                throw ex;
            }
        }
    }
}
