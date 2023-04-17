/**
 * Copyright (c) 2016 Duisburg-Essen University Library
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package org.mycore.ubo.lsf;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.TransformerException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;
import org.mycore.ubo.picker.IdentityPickerService;
import org.xml.sax.SAXException;

public class LSFPIDSearch extends MCRServlet implements IdentityPickerService {

    private final static Logger LOGGER = LogManager.getLogger(LSFPIDSearch.class);

    public void doGetPost(MCRServletJob job) throws IOException, TransformerException, SAXException {
        HttpServletRequest req = job.getRequest();
        HttpServletResponse res = job.getResponse();

        String session = req.getParameter("_xed_subselect_session");

        if (req.getParameter("cancel") != null)
            doCancel(res, session);
        else if (req.getParameter("notLSF") != null) {
            doNameWithoutLSF(req, res, session);
        } else
            doSearch(req, res, session);
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
        addParameter(url, req, "lastName", "mods:namePart[@type='family']");
        addParameter(url, req, "firstName", "mods:namePart[@type='given']");
        addParameter(url, req, "pid", "mods:nameIdentifier[@type='lsf']");
        res.sendRedirect(url.toString());
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

    private void doSearch(HttpServletRequest req, HttpServletResponse res, String session)
        throws IOException, TransformerException, SAXException {
        Element lsfpidsearch = new Element("lsfpidsearch");
        lsfpidsearch.setAttribute("session", session);

        String referrer = req.getParameter("_referrer");
        if (referrer == null)
            referrer = req.getHeader("referer"); // Yes, with the legendary misspelling.
        lsfpidsearch.setAttribute("referrer", referrer);
        
        String lastName = handleParameter(req, "lastName", lsfpidsearch);
        String firstName = handleParameter(req, "firstName", lsfpidsearch);
        handleParameter(req, "pid", lsfpidsearch);

        if (!lastName.isEmpty()) {
            LOGGER.info("UBO search LSF for person name " + lastName + ", " + firstName);
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("lastName", lastName);
            paramMap.put("firstname", firstName);
            lsfpidsearch.addContent(new LSFService().searchPerson(paramMap));
        }

        MCRServlet.getLayoutService().doLayout(req, res, new MCRJDOMContent(lsfpidsearch));
    }

    private String handleParameter(HttpServletRequest req, String type, Element parent) {
        String value = req.getParameter(type);
        value = value == null ? "" : value.trim();
        if (!value.isEmpty())
            parent.setAttribute(type, value);
        return value;
    }

    private void doCancel(HttpServletResponse res, String session) throws IOException {
        String href = getServletBaseURL() + "XEditor?_xed_submit_return_cancel=&_xed_session=" + session;
        res.sendRedirect(href);
    }
}
