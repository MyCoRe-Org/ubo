package org.mycore.ubo.user;

import java.util.SortedSet;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.xml.MCRXMLFunctions;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;
import org.mycore.orcid.oauth.MCROAuthClient;
import org.mycore.orcid.oauth.MCRRevokeRequest;
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUserAttribute;
import org.mycore.user2.MCRUserManager;

public class DozBibUserServlet extends MCRServlet {

    public final static Logger LOGGER = LogManager.getLogger(DozBibUserServlet.class);

    @Override
    protected void doGetPost(MCRServletJob job) throws Exception {
        if (MCRXMLFunctions.isCurrentUserGuestUser()) {
            job.getResponse().sendError(HttpServletResponse.SC_FORBIDDEN);
        }

        String userID = MCRSessionMgr.getCurrentSession().getUserInformation().getUserID();

        LOGGER.info("Unlink ORCID for user {}", userID);
        MCRUser user = MCRUserManager.getUser(userID);
        SortedSet<MCRUserAttribute> userAttributes = user.getAttributes();
        for (MCRUserAttribute attribute : userAttributes) {
            if (attribute.getName().matches("token_orcid")) {
                MCRRevokeRequest request = MCROAuthClient.instance().getRevokeRequest(attribute.getValue());
                request.post();
            }
        }
        userAttributes.removeIf(attribute -> attribute.getName().equals("token_orcid"));
        user.setAttributes(userAttributes);

        job.getResponse().sendRedirect(MCRFrontendUtil.getBaseURL() + "servlets/MCRUserServlet?action=show");
    }
}
