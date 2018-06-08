package org.mycore.orcid;

import java.io.IOException;

import org.jdom2.JDOMException;
import org.mycore.common.MCRSessionMgr;
import org.mycore.orcid.MCRORCIDProfile;
import org.mycore.orcid.oauth.MCRTokenResponse;
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUserManager;
import org.xml.sax.SAXException;

public class MCRORCIDUser {

    public static void storeToken(MCRTokenResponse token) throws JDOMException, IOException, SAXException {
        MCRUser user = MCRUserManager.getCurrentUser();
        user.getAttributes().put("ORCID", token.getORCID());
        user.getAttributes().put("ORCID-AccessToken", token.getAccessToken());
        MCRUserManager.updateUser(user);

    }

    public static void loadWorks() throws JDOMException, IOException, SAXException {
        MCRUser user = MCRUserManager.getCurrentUser();
        String orcid = user.getUserAttribute("ORCID");
        String token = user.getUserAttribute("ORCID-AccessToken");
        if (orcid != null) {
            MCRORCIDProfile profile = new MCRORCIDProfile(orcid);
            MCRSessionMgr.getCurrentSession().put("orcidProfile", profile);
            if (token != null) {
                profile.setAccessToken(token);
            }
            profile.getWorksSection();
        }
    }

    public static int getNumWorks() throws JDOMException, IOException, SAXException {
        MCRORCIDProfile profile = getProfile();
        return profile == null ? 0 : profile.getWorksSection().getWorks().size();
    }

    public static MCRORCIDProfile getProfile() {
        return (MCRORCIDProfile) MCRSessionMgr.getCurrentSession().get("orcidProfile");
    }

    public static boolean weAreTrustedParty() {
        MCRUser user = MCRUserManager.getCurrentUser();
        String token = user.getUserAttribute("ORCID-AccessToken");
        String orcid = user.getUserAttribute("ORCID");
        return ((orcid != null) && (token != null));
    }
}
