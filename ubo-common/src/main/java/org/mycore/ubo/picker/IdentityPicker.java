package org.mycore.ubo.picker;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

import javax.xml.transform.TransformerException;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.xml.MCRURIResolver;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;
import org.mycore.services.i18n.MCRTranslation;
import org.xml.sax.SAXException;


public class IdentityPicker extends MCRServlet {

    private final static Logger LOGGER = LogManager.getLogger(IdentityResolver.class);

    private static final String LEAD_ID = MCRConfiguration2.getStringOrThrow("MCR.user2.matching.lead_id");

    public void doGetPost(MCRServletJob job) throws IOException, TransformerException, SAXException {
        boolean isAdmin = MCRSessionMgr.getCurrentSession().getUserInformation().isUserInRole("admin");
        Document webpage = createWebpage("lsf.search");

        // add Script
        Element script = new Element("script");
        String baseURL = MCRFrontendUtil.getBaseURL(job.getRequest());
        script.setAttribute("src", baseURL + "wc/author-search/author-search.js");
        webpage.getRootElement().addContent(script);

        // add author search
        Element authorSearch = new Element("author-search");
        authorSearch.setAttribute("baseurl", baseURL);
        authorSearch.setAttribute("pidType", LEAD_ID);
        authorSearch.setAttribute("bootstrap", baseURL+"rsc/sass/scss/bootstrap-ubo.min.css");
        authorSearch.setAttribute("fontawesome", baseURL+"webjars/font-awesome/5.13.0/css/all.css");
        authorSearch.setAttribute("sessionid", job.getRequest().getParameter("_xed_subselect_session"));
        authorSearch.setAttribute("isadmin", String.valueOf(isAdmin));

        // get firstName lastName and pid
        Map<String, String> parms = getFlattenedParameters(job.getRequest());
        Optional<String> lastName = Optional.ofNullable(parms.get("lastName"));
        Optional<String> firstName = Optional.ofNullable(parms.get("firstName"));
        Optional<String> pid = Optional.ofNullable(parms.get(LEAD_ID));

        firstName.ifPresent(fn ->  authorSearch.setAttribute("firstname", fn));
        lastName.ifPresent(ln ->  authorSearch.setAttribute("lastname", ln));
        pid.ifPresent(id ->  authorSearch.setAttribute("pid", id));

        webpage.getRootElement().addContent(authorSearch);

        MCRServlet.getLayoutService().doLayout(job.getRequest(), job.getResponse(), new MCRJDOMContent(webpage));
    }

    // may be we can remove this
    private Map<String, String> getFlattenedParameters(HttpServletRequest req) {
        Map<String, String> flatParamMap;
        String decodedQueryString = "";
        // decode twice for umlaute
        decodedQueryString = URLDecoder.decode(req.getQueryString(), StandardCharsets.UTF_8);
        decodedQueryString = URLDecoder.decode(decodedQueryString, StandardCharsets.UTF_8);
        LOGGER.info("queryString: {}", decodedQueryString);
        flatParamMap = MCRURIResolver.getParameterMap(decodedQueryString);

        return flatParamMap;
    }

    public Document createWebpage(String titleI18N) {
        Element webpage = new Element("webpage");
        Document document = new Document(webpage);

        Element title = new Element("title");
        title.setText(MCRTranslation.translate(titleI18N));
        webpage.addContent(title);

        return document;
    }
}
