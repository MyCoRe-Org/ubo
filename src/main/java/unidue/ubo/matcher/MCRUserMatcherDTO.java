package unidue.ubo.matcher;

import org.mycore.user2.MCRUser;

public class MCRUserMatcherDTO {

    private boolean matchedOrEnriched = false;
    private MCRUser mcrUser;

    public MCRUserMatcherDTO(MCRUser mcrUser, boolean matchedOrEnriched) {
        this.mcrUser = mcrUser;
        this.matchedOrEnriched = matchedOrEnriched;
    }

    public MCRUserMatcherDTO(MCRUser mcrUser) {
        this.mcrUser = mcrUser;
        this.matchedOrEnriched = false;
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
