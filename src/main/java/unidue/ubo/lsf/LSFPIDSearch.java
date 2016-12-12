/**
 * $Revision$ 
 * $Date$
 *
 * This file is part of the MILESS repository software.
 * Copyright (C) 2011 MILESS/MyCoRe developer team
 * See http://duepublico.uni-duisburg-essen.de/ and http://www.mycore.de/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **/

package unidue.ubo.lsf;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.jdom2.Element;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;
import org.xml.sax.SAXException;

public class LSFPIDSearch extends MCRServlet {

    private final static Logger LOGGER = Logger.getLogger(LSFPIDSearch.class);

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

        String lastName = handleParameter(req, "lastName", lsfpidsearch);
        String firstName = handleParameter(req, "firstName", lsfpidsearch);
        handleParameter(req, "pid", lsfpidsearch);

        if (!lastName.isEmpty()) {
            LOGGER.info("UBO search LSF for person name " + lastName + ", " + firstName);
            lsfpidsearch.addContent(LSFClient.instance().searchPerson(lastName, firstName));
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
