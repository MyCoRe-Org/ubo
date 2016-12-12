package unidue.ubo;

import org.mycore.common.MCRSessionMgr;

public class AccessControl {

    public static boolean currentUserIsAdmin() {
        return MCRSessionMgr.getCurrentSession().getUserInformation().isUserInRole("admin");
    }
}
