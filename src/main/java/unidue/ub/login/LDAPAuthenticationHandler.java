package unidue.ub.login;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUser2Constants;
import org.mycore.user2.MCRUserManager;
import unidue.ubo.ldap.LDAPAuthenticator;
import unidue.ubo.ldap.LDAPObject;
import unidue.ubo.ldap.LDAPSearcher;

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
 * # Mapping from LDAP attribute to real name of user
 * MCR.user2.LDAP.Mapping.Name=cn
 *
 * # Mapping from LDAP attribute to E-Mail address of user
 * MCR.user2.LDAP.Mapping.E-Mail=mail
 *
 * # Default group membership (optional) for any successful login
 * MCR.user2.LDAP.Mapping.Group.DefaultGroup=submitter
 *
 * # Mapping of any attribute.value combination to group membership of user
 * # eduPersonScopedAffiliation may be faculty|staff|employee|student|alum|member|affiliate
 * MCR.user2.LDAP.Mapping.Group.eduPersonScopedAffiliation.staff@uni-duisburg-essen.de=submitter
 *
 * # Default Role that is assigned to newly created users
 * MCR.user2.IdentityManagement.UserCreation.DefaultRole=submitter
 *
 * @author Frank L\u00FCtzenkirchen
 */
public class LDAPAuthenticationHandler extends AuthenticationHandler {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String CONFIG_PREFIX = MCRUser2Constants.CONFIG_PREFIX + "LDAP.";
    private static final String CONFIG_ROLE = MCRUser2Constants.CONFIG_PREFIX + "IdentityManagement.UserCreation.DefaultRole";

    /** Filter for user ID */
    private String uidFilter;

    /** Base DN, uid of user on actual login will be used! */
    private String baseDN;

    /** Mapping from LDAP attribute to real name of user */
    private String mapName;

    /** Mapping from LDAP attribute to E-Mail address of user */
    private String mapEMail;

    private String defaultRole;

    public LDAPAuthenticationHandler() {
        MCRConfiguration config = MCRConfiguration.instance();

        uidFilter = config.getString(CONFIG_PREFIX + "UIDFilter");
        baseDN = config.getString(CONFIG_PREFIX + "BaseDN");

        mapName = config.getString(CONFIG_PREFIX + "Mapping.Name");
        mapEMail = config.getString(CONFIG_PREFIX + "Mapping.E-Mail");

        defaultRole = config.getString(CONFIG_ROLE, "submitter");
    }

    public MCRUser authenticate(String uid, String pwd) throws Exception {
        DirContext ctx = null;

        try {
            ctx = new LDAPAuthenticator().authenticate(uid, pwd);
            if (ctx == null) {
                return null;
            }

            LOGGER.debug("Login of " + uid + " via LDAP was successful");

            MCRUser user = MCRUserManager.getUser(uid, realmID);
            if (user != null) {
                LOGGER.debug("User " + uid + " already known in store");
            } else {
                LOGGER.debug("User " + uid + " unknown in store, will create");
                user = new MCRUser(uid, realmID);
                user.assignRole(defaultRole);
                MCRUserManager.createUser(user);
            }
            List<LDAPObject> ldapObjects = searchUserInLDAP(ctx, user);
            if(ldapObjects.size() == 1) {
                setUserAttributes(ctx, user, ldapObjects.get(0));
            } else {
                // TODO: what should happen if ldapObjects contains more than one LDAPObject, i.e. the search returned multiple users?
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
    private List<LDAPObject> searchUserInLDAP(DirContext ctx, MCRUser user) throws NamingException {
        String uid = user.getUserName();
        String principal = String.format(baseDN, uid);
        String filter = String.format(Locale.ROOT, uidFilter, uid);

        return new LDAPSearcher().search(ctx, principal, filter);
    }

    /**
     * Sets the MCRUser's name, e-mail and groups by mapping LDAP attributes.
     */
    private void setUserAttributes(DirContext ctx, MCRUser user, LDAPObject userLDAPObject) {
        addToGroup(user, CONFIG_PREFIX + "Mapping.Group.DefaultGroup");

        for(Map.Entry<String, String> attributeEntry : userLDAPObject.getAttributes().entries()) {
            String attributeID = attributeEntry.getKey();
            String attributeValue = attributeEntry.getValue();
            setUserRealName(user, attributeID, attributeValue);
            setUserEMail(user, attributeID, attributeValue);
            addToGroup(user, CONFIG_PREFIX + "Mapping.Group." + attributeID + "." + attributeValue);
        }
    }

    private void setUserEMail(MCRUser user, String attributeID, String attributeValue) {
        if (attributeID.equals(mapEMail) && (user.getEMailAddress() == null)) {
            LOGGER.debug("User " + user.getUserName() + " e-mail = " + attributeValue);
            user.setEMail(attributeValue);
        }
    }

    private void setUserRealName(MCRUser user, String attributeID, String attributeValue) {
        if (attributeID.equals(mapName) && (user.getRealName() == null)) {
            attributeValue = formatName(attributeValue);
            LOGGER.debug("User " + user.getUserName() + " name = " + attributeValue);
            user.setRealName(attributeValue);
        }
    }

    private void addToGroup(MCRUser user, String groupMapping) {
        String group = MCRConfiguration.instance().getString(groupMapping, null);
        if ((group != null) && (!user.isUserInRole((group)))) {
            LOGGER.info("Add user " + user.getUserName() + " to group " + group);
            user.assignRole(group);
        }
    }

    /** Formats a user name into "lastname, firstname" syntax. */
    private static String formatName(String name) {
        name = name.replaceAll("\\s+", " ").trim();
        if (name.contains(",")) {
            return name;
        }
        int pos = name.lastIndexOf(' ');
        if (pos == -1) {
            return name;
        }
        return name.substring(pos + 1, name.length()) + ", " + name.substring(0, pos);
    }
}
