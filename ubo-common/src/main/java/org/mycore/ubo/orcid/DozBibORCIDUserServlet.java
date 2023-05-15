package org.mycore.ubo.orcid;

import java.io.IOException;
import java.util.Set;
import java.util.SortedSet;

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

/**
 * Servlet removes all orcid access tokens of the current user. If you want to remove a single access token for
 * a given orcid please see {@link org.mycore.orcid2.resources.MCRORCIDResource#revoke(String)}
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

        MCRORCIDUser orcidUser = MCRORCIDSessionUtils.getCurrentUser();
        Set<String> orcidIdentifiers = orcidUser.getORCIDs();

        if (orcidIdentifiers.isEmpty()) {
            redirectToProfile(job);
            return;
        }

        if (job.getRequest().getParameterMap().get("sync").length == 1) {
            toggleSync();
        } else {
            orcidIdentifiers.forEach(orcid -> {
                LOGGER.info("Unlinking ORCID {} for user {}", orcid, orcidUser.getUser().getUserID());
                MCRORCIDUserUtils.revokeCredentialByORCID(orcidUser, orcid);
            });
        }

        redirectToProfile(job);
    }

    private void toggleSync() {
        MCRUser user = MCRUserManager.getUser(MCRSessionMgr.getCurrentSession().getUserInformation().getUserID());
        SortedSet<MCRUserAttribute> attributes = user.getAttributes();

        attributes.stream()
            .filter(attr -> attr.getName().equals("orcid_sync"))
            .findFirst()
            .ifPresentOrElse(present -> {
                attributes.remove(present);
            }, () -> {
                attributes.add(new MCRUserAttribute("orcid_sync", String.valueOf(true)));
            });
    }

    protected void redirectToProfile(MCRServletJob job) throws IOException {
        job.getResponse().sendRedirect(MCRFrontendUtil.getBaseURL() + "servlets/MCRUserServlet?action=show");
    }
}
