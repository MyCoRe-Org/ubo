package org.mycore.ubo.ldap.picker;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.xml.MCRURIResolver;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;
import org.xml.sax.SAXException;

import org.mycore.ubo.picker.IdentityPickerService;

/**
 * Servlet that handles requests when the LDAP strategy is configured in the mycore.properties:
 * (MCR.IdentityPicker.strategy=org.mycore.ubo.ldap.picker.LDAP), requests get routed via "IdentityPicker.java" that
 * is bound to the URL "identitypicker.html" (see web.xml).
 *
 * This servlet handles the "picking" via searching or by hand of authors of (new) publications.
 *
 * @author Pascal Rost
 * TODO: rename, remove "PID"...
 */
public class LDAPPIDSearch extends MCRServlet implements IdentityPickerService {

    private final static Logger LOGGER = LogManager.getLogger(LDAPPIDSearch.class);

    @Override
    public void handleRequest(MCRServletJob job) {
        HttpServletRequest req = job.getRequest();
        HttpServletResponse res = job.getResponse();

        String session = req.getParameter("_xed_subselect_session");

        try {
            if (req.getParameter("cancel") != null) {
                doCancel(res, session);
            } else if (req.getParameter("notLDAP") != null) {
                doNameWithoutLDAP(req, res, session);
            } else if (req.getParameter("search") != null) {
                // search button is clicked
                doSearch(req, res, session);
            } else {
                // the default search when first visiting the identity picker, uses query parameters (URL params)
                doQuerySearch(req, res, session);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doSearch(HttpServletRequest req, HttpServletResponse res, String session)
            throws TransformerException, IOException, SAXException {
        Element ldappidsearch = new Element("ldappidsearch");
        ldappidsearch.setAttribute("session", session);
        Map<String, String> flatParamMap = new HashMap<>();
        for(Map.Entry<String, String[]> paramEntry : req.getParameterMap().entrySet()) {
            String paramName = paramEntry.getKey();
            String paramValue = paramEntry.getValue().length > 0 ? paramEntry.getValue()[0] : "";
            if(StringUtils.isNotEmpty(paramValue)) {
                flatParamMap.put(paramName, paramValue);
            }
        }

        preserveAttributes(ldappidsearch, flatParamMap);

        ldappidsearch.addContent(new LDAPService().searchPerson(flatParamMap));
        MCRServlet.getLayoutService().doLayout(req, res, new MCRJDOMContent(ldappidsearch));
    }

    /**
     * TODO: doc
     * querySearch = default search when entering the picker (using query params = url params)
     * @param req
     * @param res
     * @param session
     * @throws TransformerException
     * @throws IOException
     * @throws SAXException
     */
    private void doQuerySearch(HttpServletRequest req, HttpServletResponse res, String session)
            throws TransformerException, IOException, SAXException {
        Element ldappidsearch = new Element("ldappidsearch");
        ldappidsearch.setAttribute("session", session);
        Map<String, String> attributes = getFlattenedParameters(req);

        LOGGER.info("SEARCHING with: {}", attributes);
        ldappidsearch.addContent(new LDAPService().searchPerson(attributes));

        preserveAttributes(ldappidsearch, attributes);

        MCRServlet.getLayoutService().doLayout(req, res, new MCRJDOMContent(ldappidsearch));
    }

    /**
     * Used to preserve all parameters that got send to this servlet to conduct a search as attributes in the
     * resulting XML
     * @param searchResultElement the XML-Element that will be send back to the LayoutService, transformed via XSLT
     *                            and rendered on the client
     * @param attributeMap a Map consisting of all parameters that shall be added to the searchResultElement
     */
    private void preserveAttributes(Element searchResultElement, Map<String, String> attributeMap) {
        for(Map.Entry<String, String> attributeEntry: attributeMap.entrySet()) {
            String attribute = attributeEntry.getKey();
            String value = attributeEntry.getValue();
            searchResultElement.setAttribute(attribute, value);
        }
    }

    /**
     * TODO: doc
     */
    private Map<String, String> getFlattenedParameters(HttpServletRequest req) {
        Map<String, String> flatParamMap = new HashMap<>();
        String decodedQueryString = "";
        try {
            // decode twice for umlaute
            decodedQueryString = URLDecoder.decode(req.getQueryString(), "UTF-8");
            decodedQueryString = URLDecoder.decode(decodedQueryString, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        LOGGER.info("queryString: {}", decodedQueryString);
        flatParamMap = MCRURIResolver.getParameterMap(decodedQueryString);

        return flatParamMap;
    }

    private void doNameWithoutLDAP(HttpServletRequest req, HttpServletResponse res, String session) throws IOException {
        StringBuffer url = new StringBuffer(getServletBaseURL());
        url.append("XEditor?_xed_submit_return=&_xed_session=").append(session);
        addParameter(url, req, "lastName", "mods:namePart[@type='family']");
        addParameter(url, req, "firstName", "mods:namePart[@type='given']");
        addParameter(url, req, "orcid", "mods:nameIdentifier[@type='orcid']");
        res.sendRedirect(url.toString());
    }

    private void doCancel(HttpServletResponse res, String session) throws IOException {
        String href = getServletBaseURL() + "XEditor?_xed_submit_return_cancel=&_xed_session=" + session;
        res.sendRedirect(href);
    }

    private void addParameter(StringBuffer url, HttpServletRequest req, String parameter, String xPath)
            throws UnsupportedEncodingException {
        String value = req.getParameter(parameter);
        value = value == null ? "" : value.trim();

        url.append("&");
        url.append(URLEncoder.encode(xPath, "UTF-8"));
        url.append("=");
        url.append(URLEncoder.encode(value, "UTF-8"));
    }
}

