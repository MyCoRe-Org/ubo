package org.mycore.ubo.login;

import java.util.List;
import java.util.Locale;
import java.util.SortedSet;

import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.ubo.ldap.LDAPAuthenticator;
import org.mycore.ubo.ldap.LDAPObject;
import org.mycore.ubo.ldap.LDAPSearcher;
import org.mycore.ubo.matcher.MCRUserMatcherLDAP;
import org.mycore.ubo.matcher.MCRUserMatcherUtils;
import org.mycore.user2.MCRRealmFactory;
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUser2Constants;
import org.mycore.user2.MCRUserAttribute;
import org.mycore.user2.MCRUserManager;

/**
 * Checks the given user ID and password combination against remote LDAP server
 * and returns the user if authentication is OK. Configuration is done via mycore.properties:
 *
 * # Base DN, uid of user on actual login will be used!
 * # We do not use any "global" credentials, just the user's own uid and password to connect
 * MCR.user2.LDAP.BaseDN=uid=%s,ou=people,dc=uni-duisburg-essen,dc=de
 *
 * # Filter for user ID
 * MCR.user2.LDAP.UIDFilter=(uid=%s)
 *
 * # Default Role/Group that is assigned to newly created users
 * MCR.user2.IdentityManagement.UserCreation.DefaultRole=submitter
 *
 * # Realm that newly created users get assigned to
 * MCR.user2.IdentityManagement.UserCreation.LDAP.Realm=ldap
 *
 * @author Frank L\u00FCtzenkirchen, Pascal Rost
 */
public class LDAPAuthenticationHandler extends AuthenticationHandler {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String CONFIG_PREFIX = MCRUser2Constants.CONFIG_PREFIX + "LDAP.";

    private static final String CONFIG_ROLE = MCRUser2Constants.CONFIG_PREFIX + "IdentityManagement.UserCreation.DefaultRole";

    private static final String CONFIG_REALM = MCRUser2Constants.CONFIG_PREFIX + "IdentityManagement.UserCreation.LDAP.Realm";

    /** Filter for user ID */
    private String uidFilter;

    /** Base DN, uid of user on actual login will be used! */
    private String baseDN;

    private String defaultRole;

    private String realm;

    public LDAPAuthenticationHandler() {
        uidFilter = MCRConfiguration2.getString(CONFIG_PREFIX + "UIDFilter").get();
        baseDN = MCRConfiguration2.getString(CONFIG_PREFIX + "BaseDN").get();

        defaultRole = MCRConfiguration2.getString(CONFIG_ROLE).orElse("submitter");
        realm = MCRConfiguration2.getString(CONFIG_REALM).orElse(MCRRealmFactory.getLocalRealm().getID());
    }

    public MCRUser authenticate(String uid, String pwd) throws Exception {
        LdapContext ctx = null;

        try {
            ctx = new LDAPAuthenticator().authenticate(uid, pwd);
            if (ctx == null) {
                return null;
            }

            LOGGER.info("Login of " + uid + " via LDAP was successful");

            MCRUser user = MCRUserManager.getUser(uid, realm);

            if (user != null) {
                LOGGER.info("User " + uid + " already known in store");
            } else {
                LOGGER.info("User " + uid + " unknown in store, will create with realm: " + realm);
                user = new MCRUser(uid, realm);
                user.assignRole(defaultRole);

                // logic to make new MCRUser connected to LDAPUser if possible (matching is done by MCR/LDAP-Attributes)
                List<LDAPObject> ldapObjects = searchUserInLDAP(ctx, user);
                if (ldapObjects.size() == 1) {
                    LDAPObject ldapUser = ldapObjects.get(0);

                    MCRUserMatcherUtils.setStaticMCRUserAttributes(user, ldapUser);
                    MCRUserMatcherUtils.addMCRUserToDynamicGroups(user, ldapUser);

                    // convert LDAP-Attributes/Values into MCRUser Attributes and put them into new MCRUser
                    MCRUserMatcherLDAP userMatcher = new MCRUserMatcherLDAP();
                    SortedSet<MCRUserAttribute> userAttributesFromLDAP =
                        userMatcher.convertLDAPAttributesToMCRUserAttributes(ldapUser);
                    user.getAttributes().addAll(userAttributesFromLDAP);
                } else {
                    // TODO: what should happen if ldapObjects contains more than one LDAPObject, i.e. the search returned multiple users?
                }

                MCRUserManager.createUser(user);
            }

            return user;
        } finally {
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (NamingException ex) {
                    LOGGER.warn("could not close context " + ex);
                }
            }
        }
    }

    /**
     * Searches the user in the LDAP-System and returns it's corresponding LDAPObject containing all attributes
     */
    private List<LDAPObject> searchUserInLDAP(LdapContext ctx, MCRUser user) throws NamingException {
        String uid = user.getUserName();
        String principal = String.format(Locale.ROOT, baseDN, uid);
        String filter = String.format(Locale.ROOT, uidFilter, uid);

        return new LDAPSearcher().search(ctx, principal, filter);
    }
}
