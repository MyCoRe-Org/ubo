package org.mycore.ubo.matcher;

import org.mycore.user2.MCRUser;

/**
 * Data Transfer Object for transferring MCRUser-Instances and a related Flag "matchedOrEnriched" between different
 * parts of the MCRUserMatcher-Framework.
 *
 * @author Pascal Rost
 */
public class MCRUserMatcherDTO {

    private boolean matchedOrEnriched;
    private MCRUser mcrUser;

    public MCRUserMatcherDTO(MCRUser mcrUser) {
        this.mcrUser = mcrUser;
    }

    public MCRUser getMCRUser() {
        return this.mcrUser;
    }

    public void setMCRUser(MCRUser mcrUser) {
        this.mcrUser = mcrUser;
    }

    public boolean wasMatchedOrEnriched() {
        return this.matchedOrEnriched;
    }

    public void setMatchedOrEnriched(boolean bool) {
        this.matchedOrEnriched = bool;
    }
}
