/**
 * Copyright (c) 2016 Duisburg-Essen University Library
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package org.mycore.ubo.local;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.transform.TransformerException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.xml.MCRURIResolver;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;
import org.mycore.ubo.picker.IdentityPickerService;
import org.xml.sax.SAXException;

public class LocalPIDSearch extends MCRServlet implements IdentityPickerService {

    private final static Logger LOGGER = LogManager.getLogger(LocalPIDSearch.class);

    private static final String LEAD_ID = MCRConfiguration2.getStringOrThrow("MCR.user2.matching.lead_id");

    public void doGetPost(MCRServletJob job) throws IOException, TransformerException, SAXException {
        HttpServletRequest req = job.getRequest();
        HttpServletResponse res = job.getResponse();

        String session = req.getParameter("_xed_subselect_session");

        if (req.getParameter("cancel") != null)
            doCancel(res, session);
        else if (req.getParameter("notLSF") != null) {
            doNameWithoutLSF(req, res, session);
        } else if (req.getParameter("search") != null) {
            doSearch(req, res, session);
        } else {
            doInitialSearch(req, res, session);
        }
    }

    @Override
    public void handleRequest(MCRServletJob job) {
        try {
            doGetPost(job);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doNameWithoutLSF(HttpServletRequest req, HttpServletResponse res, String session) throws IOException {
        StringBuffer url = new StringBuffer(getServletBaseURL());
        url.append("XEditor?_xed_submit_return=&_xed_session=").append(session);
        String term = req.getParameter("term");
        term = term == null ? "" : term.trim();

        int commaIndex = term.indexOf(",");

        String familyName, firstName;
        if (commaIndex == -1) {
            familyName = term;
            firstName = "";
        } else {
            familyName = term.substring(0, commaIndex);
            firstName = term.substring(commaIndex + 1);
        }

        addParameter(url, familyName, "mods:namePart[@type='family']");
        addParameter(url, firstName, "mods:namePart[@type='given']");
        addParameter(url, Optional.ofNullable(req.getParameter("pid")).orElse(" "),
                "mods:nameIdentifier[@type='" + LEAD_ID + "']");
        res.sendRedirect(url.toString());
    }

    private void addParameter(StringBuffer url, String value, String xPath)
            throws UnsupportedEncodingException {
        url.append("&");
        url.append(URLEncoder.encode(xPath, StandardCharsets.UTF_8));
        url.append("=");
        url.append(URLEncoder.encode(value, StandardCharsets.UTF_8));
    }

    private void doSearch(HttpServletRequest req, HttpServletResponse res, String session)
            throws TransformerException, IOException, SAXException {
        Element koelnpidsearch = new Element("localpidsearch");
        koelnpidsearch.setAttribute("session", session);
        final Enumeration<String> parameterNames = req.getParameterNames();
        Map<String, String> parameters = new HashMap<>();
        parameterNames.asIterator().forEachRemaining(name -> {
            parameters.put(name, req.getParameter(name));
        });

        setReferrer(req, koelnpidsearch);
        setParametersToElement(koelnpidsearch, parameters);
        performSearch(koelnpidsearch, parameters);
        MCRServlet.getLayoutService().doLayout(req, res, new MCRJDOMContent(koelnpidsearch));
    }

    private void setReferrer(HttpServletRequest req, Element localSearch) {
        String referrer = req.getParameter("_referrer");
        if (referrer == null)
            referrer = req.getHeader("referer"); // Yes, with the legendary misspelling.
        localSearch.setAttribute("referrer", referrer);
    }

    private void doInitialSearch(HttpServletRequest req, HttpServletResponse res, String session)
            throws IOException, TransformerException, SAXException {
        Element localsearch = new Element("localpidsearch");
        localsearch.setAttribute("session", session);

        setReferrer(req, localsearch);

        final Map<String, String> parameters = getFlattenedParameters(req);
        performSearch(localsearch, parameters);
        setParametersToElement(localsearch, parameters);
        MCRServlet.getLayoutService().doLayout(req, res, new MCRJDOMContent(localsearch));
    }

    private void performSearch(Element localsearch, Map<String, String> parameters) {
        String lastName = parameters.get("lastName");
        String firstName = parameters.get("firstName");


        final boolean lnPresent = lastName != null && !lastName.isEmpty();
        final boolean fnPresent = firstName != null && !firstName.isEmpty();
        if (lnPresent || fnPresent) {
            parameters.put("term", Stream.concat(Stream.ofNullable(firstName), Stream.ofNullable(lastName)).collect(Collectors.joining(" ")));
        }

        String term = parameters.get("term");
        final boolean termPresent = term != null && !term.isEmpty();

        if (termPresent) {
            LOGGER.info("UBO search Local Users for term " + term);
            localsearch
                    .addContent(new LocalSearcher().search(term));
        }
    }

    private void setParametersToElement(Element localsearch, Map<String, String> parameters) {
        handleParameter(parameters, "term", localsearch);
        handleParameter(parameters, "pid", localsearch);
    }

    private Map<String, String> getFlattenedParameters(HttpServletRequest req) {
        final Map<String, String> flatParamMap = new HashMap<>();
        String decodedQueryString = "";
        // decode twice for umlaute
        decodedQueryString = URLDecoder.decode(req.getQueryString(), StandardCharsets.UTF_8);
        decodedQueryString = URLDecoder.decode(decodedQueryString, StandardCharsets.UTF_8);
        LOGGER.info("queryString: {}", decodedQueryString);
        flatParamMap.putAll(MCRURIResolver.getParameterMap(decodedQueryString));

        return flatParamMap;
    }

    private String handleParameter(Map<String, String> parameters, String type, Element parent) {
        String value = parameters.getOrDefault(type, "");
        parent.setAttribute(type, value);
        return value;
    }

    private void doCancel(HttpServletResponse res, String session) throws IOException {
        String href = getServletBaseURL() + "XEditor?_xed_submit_return_cancel=&_xed_session=" + session;
        res.sendRedirect(href);
    }
}
