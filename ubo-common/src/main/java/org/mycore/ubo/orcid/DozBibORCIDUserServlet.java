package org.mycore.ubo.orcid;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.xml.MCRXMLFunctions;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;
import org.mycore.orcid2.user.MCRORCIDSessionUtils;
import org.mycore.orcid2.user.MCRORCIDUser;
import org.mycore.orcid2.user.MCRORCIDUserUtils;
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUserAttribute;
import org.mycore.user2.MCRUserManager;

import java.io.IOException;
import java.util.Set;
import java.util.SortedSet;

/**
 * Servlet removes all orcid access tokens of the current user. If you want to remove a single access token for
 * a given orcid please see {@link org.mycore.orcid2.rest.resources.MCRORCIDResource#revoke(String)} )}
 *
 * @author shermann
 * */
public class DozBibORCIDUserServlet extends MCRServlet {

    public final static Logger LOGGER = LogManager.getLogger(DozBibORCIDUserServlet.class);

    @Override
    protected void doGetPost(MCRServletJob job) throws Exception {
        if (MCRXMLFunctions.isCurrentUserGuestUser()) {
            job.getResponse().sendError(HttpServletResponse.SC_FORBIDDEN);
        }

        String action = job.getRequest().getParameter("action");
        if (action == null) {
            redirectToProfile(job);
            return;
        }

        MCRORCIDUser orcidUser;
        try {
            orcidUser = MCRORCIDSessionUtils.getCurrentUser();
        }catch(Exception e){
            LOGGER.error("Could not get orcid user for user {}", MCRUserManager.getCurrentUser(), e);
            redirectToProfile(job);
            return;
        }

        Set<String> orcidIdentifiers = orcidUser.getORCIDs();
        if (orcidIdentifiers.isEmpty()) {
            redirectToProfile(job);
            return;
        }

        switch (action) {
            case "sync":
                toggleSync();
                break;
            case "revoke":
                orcidUser.getUser()
                    .getAttributes()
                    .removeIf(a -> a.getName().equals("orcid_update_profile"));

                orcidIdentifiers.forEach(orcid -> {
                    LOGGER.info("Unlinking ORCID {} for user {}", orcid, orcidUser.getUser().getUserID());
                    MCRORCIDUserUtils.revokeCredentialByORCID(orcidUser, orcid);
                });
                break;
        }

        redirectToProfile(job);
    }

    private void toggleSync() {
        MCRUser user = MCRUserManager.getUser(MCRSessionMgr.getCurrentSession().getUserInformation().getUserID());
        SortedSet<MCRUserAttribute> attributes = user.getAttributes();

        attributes.stream()
            .filter(attr -> attr.getName().equals("orcid_update_profile"))
            .findFirst()
            .ifPresentOrElse(present -> {
                attributes.remove(present);
            }, () -> {
                attributes.add(new MCRUserAttribute("orcid_update_profile", String.valueOf(true)));
            });
    }

    protected void redirectToProfile(MCRServletJob job) throws IOException {
        job.getResponse().sendRedirect(MCRFrontendUtil.getBaseURL() + "servlets/MCRUserServlet?action=show");
    }
}
