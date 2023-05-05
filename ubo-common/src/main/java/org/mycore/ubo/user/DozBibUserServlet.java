package org.mycore.ubo.user;

import java.io.IOException;
import java.util.Set;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.xml.MCRXMLFunctions;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;
import org.mycore.orcid2.user.MCRORCIDSessionUtils;
import org.mycore.orcid2.user.MCRORCIDUser;
import org.mycore.orcid2.user.MCRORCIDUserUtils;

/**
 * Servlet removes all orcid access tokens of the current user. If you want to remove a single access token for
 * a given orcid please see {@link org.mycore.orcid2.resources.MCRORCIDResource#revoke(String)}
 *
 * @author shermann
 * */
public class DozBibUserServlet extends MCRServlet {

    public final static Logger LOGGER = LogManager.getLogger(DozBibUserServlet.class);

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

        orcidIdentifiers.forEach(orcid -> {
            LOGGER.info("Unlinking ORCID {} for user {}", orcid, orcidUser.getUser().getUserID());
            MCRORCIDUserUtils.revokeCredentialByORCID(orcidUser, orcid);
        });

        redirectToProfile(job);
    }

    protected void redirectToProfile(MCRServletJob job) throws IOException {
        job.getResponse().sendRedirect(MCRFrontendUtil.getBaseURL() + "servlets/MCRUserServlet?action=show");
    }
}
