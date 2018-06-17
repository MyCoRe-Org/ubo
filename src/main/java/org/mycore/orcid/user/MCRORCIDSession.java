package org.mycore.orcid.user;

import java.io.IOException;

import org.jdom2.JDOMException;
import org.mycore.common.MCRSessionMgr;
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUserManager;
import org.xml.sax.SAXException;

public class MCRORCIDSession {

    private static final String KEY_ORCID_USER = "ORCID_USER";

    public static MCRORCIDUser setORCIDUser() {
        MCRUser user = MCRUserManager.getCurrentUser();
        MCRORCIDUser orcidUser = new MCRORCIDUser(user);
        MCRSessionMgr.getCurrentSession().put(KEY_ORCID_USER, orcidUser);
        return orcidUser;
    }

    public static MCRORCIDUser getORCIDUser() {
        MCRORCIDUser orcidUser = (MCRORCIDUser) MCRSessionMgr.getCurrentSession().get(KEY_ORCID_USER);
        return (orcidUser == null ? setORCIDUser() : orcidUser);
    }

    public static int getNumWorks() throws JDOMException, IOException, SAXException {
        return getORCIDUser().getORCIDProfile().getWorksSection().getWorks().size();
    }

    public static boolean weAreTrustedParty() {
        return getORCIDUser().weAreTrustedParty();
    }
}
