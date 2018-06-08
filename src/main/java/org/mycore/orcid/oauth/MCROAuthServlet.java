package org.mycore.orcid.oauth;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.JDOMException;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;
import org.mycore.orcid.MCRORCIDUser;
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUserManager;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonProcessingException;

public class MCROAuthServlet extends MCRServlet {

    private final static Logger LOGGER = LogManager.getLogger(MCROAuthServlet.class);

    private String scopes = MCRConfiguration.instance().getString("MCR.ORCID.OAuth.Scopes");

    private String redirectURL;

    @Override
    protected void doGetPost(MCRServletJob job) throws Exception {
        this.redirectURL = MCRFrontendUtil.getBaseURL() + job.getRequest().getServletPath().substring(1);

        String code = job.getRequest().getParameter("code");
        String error = job.getRequest().getParameter("error");

        if ((error != null) && !error.trim().isEmpty()) {
            job.getResponse().sendRedirect("servlets/MCRUserServlet?action=show&XSL.error=" + error);
        } else if ((code == null) || code.trim().isEmpty()) {
            redirectToGetAuthorization(job);
        } else {
            MCRTokenResponse token = exchangeCodeForAccessToken(code);
            MCRORCIDUser.storeToken(token);
            MCRORCIDUser.loadWorks();
            job.getResponse().sendRedirect("servlets/MCRUserServlet?action=show");
        }
    }

    private void redirectToGetAuthorization(MCRServletJob job)
        throws URISyntaxException, MalformedURLException, IOException {
        String url = MCROAuthClient.instance().getCodeRequestURL(redirectURL, scopes);
        job.getResponse().sendRedirect(url);
    }

    private MCRTokenResponse exchangeCodeForAccessToken(String code)
        throws JsonProcessingException, IOException, JDOMException, SAXException {
        MCRTokenRequest request = MCROAuthClient.instance().getTokenRequest();
        request.set("grant_type", "authorization_code");
        request.set("code", code);
        request.set("redirect_uri", redirectURL);

        MCRTokenResponse token = request.post();
        LOGGER.info("access granted for " + token.getORCID() + " " + token.getAccessToken());
        return token;
    }
}
