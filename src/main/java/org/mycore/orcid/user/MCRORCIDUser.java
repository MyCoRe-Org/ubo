package org.mycore.orcid.user;

import org.mycore.orcid.MCRORCIDProfile;
import org.mycore.orcid.oauth.MCRTokenResponse;
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUserManager;

public class MCRORCIDUser {

    private static final String ATTR_ORCID_ID = "ORCID";

    private static final String ATTR_ORCID_TOKEN = "ORCID-AccessToken";

    private MCRUser user;

    private MCRORCIDProfile profile;

    public MCRORCIDUser(MCRUser user) {
        this.user = user;
    }

    public void store(MCRTokenResponse token) {
        user.getAttributes().put(ATTR_ORCID_ID, token.getORCID());
        user.getAttributes().put(ATTR_ORCID_TOKEN, token.getAccessToken());
        MCRUserManager.updateUser(user);
    }

    public boolean hasORCIDProfile() {
        return user.getUserAttribute(ATTR_ORCID_ID) != null;
    }

    public boolean weAreTrustedParty() {
        return user.getUserAttribute(ATTR_ORCID_TOKEN) != null;
    }

    public MCRORCIDProfile getORCIDProfile() {
        if (!hasORCIDProfile()) {
            return null;
        }

        if (profile == null) {
            String orcid = user.getUserAttribute(ATTR_ORCID_ID);
            profile = new MCRORCIDProfile(orcid);
            if (weAreTrustedParty()) {
                String token = user.getUserAttribute(ATTR_ORCID_TOKEN);
                profile.setAccessToken(token);
            }
        }
        return profile;
    }
}
