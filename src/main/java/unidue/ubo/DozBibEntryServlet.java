/**
 * Copyright (c) 2016 Duisburg-Essen University Library
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package unidue.ubo;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.MCRMailer;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;

public class DozBibEntryServlet extends MCRServlet {
    private final static Logger LOGGER = LogManager.getLogger(DozBibEntryServlet.class);

    public void doGetPost(MCRServletJob job) throws Exception {
        HttpServletRequest req = job.getRequest();
        HttpServletResponse res = job.getResponse();

        String mode = req.getParameter("mode");
        if( ( mode == null ) || mode.isEmpty() ) mode = "show";
       
        if( "show".equals(mode))
            showEntry(req, res);
        else if (systemInReadOnlyMode()) 
            sendReadOnlyError(res);
        else if ("delete".equals(mode))
            deleteEntry(req, res);
        else if ("save".equals(mode))
            saveEntry(req, res);
    }

    public static boolean systemInReadOnlyMode() {
        return MCRConfiguration.instance().getBoolean("UBO.System.ReadOnly", false);
    }

    public static void sendReadOnlyError(HttpServletResponse res) throws IOException {
        String msg = MCRConfiguration.instance().getString("UBO.System.ReadOnly.Message");
        res.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, msg);
        LOGGER.info("rejected editing because system is in read-only mode");
    }

    private void showEntry(HttpServletRequest req, HttpServletResponse res) throws Exception {
        int ID = checkRequestedID(req, res);
        if (ID == 0)
            return;

        LOGGER.info("UBO show entry " + ID);
        Document xml = DozBibManager.instance().getEntry(ID);
        getLayoutService().doLayout(req, res, new MCRJDOMContent(xml));
    }

    private int checkRequestedID(HttpServletRequest req, HttpServletResponse res) throws Exception {
        String id = req.getParameter("id");
        if ((id == null) || (id.trim().length() == 0)) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parameter 'id' is missing.");
            return 0;
        }

        int ID = Integer.parseInt(id);
        if (!DozBibManager.instance().exists(ID)) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND, "There is no entry with ID " + id);
            return 0;
        }
        return ID;
    }

    private void deleteEntry(HttpServletRequest req, HttpServletResponse res) throws Exception {
        int ID = checkRequestedID(req, res);
        if (ID == 0)
            return;

        Document entry = DozBibManager.instance().getEntry(ID);
        if (!AccessControl.currentUserIsAdmin()) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        LOGGER.info("UBO delete entry " + ID);
        entry.getRootElement().setAttribute("status", "deleted");
        DozBibManager.instance().deleteEntry(ID);
        req.setAttribute("XSL.step", "confirm.deleted");
        getLayoutService().doLayout(req, res, new MCRJDOMContent(entry));
    }

    private void sendNotificationMail(Document doc) throws Exception {
        LOGGER.info("UBO sending notification e-mail");

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("WebApplicationBaseURL", MCRFrontendUtil.getBaseURL());
        parameters.put("MCR.Mail.Address", MCRConfiguration.instance().getString("MCR.Mail.Address"));
        MCRMailer.sendMail(doc, "bibentry-e-mail", parameters);
    }

    private void saveEntry(HttpServletRequest req, HttpServletResponse res) throws Exception {
        Document doc = (Document) req.getAttribute("MCRXEditorSubmission");

        String ID = doc.getRootElement().getAttributeValue("id", "0");
        int id = Integer.parseInt(ID);

        if (id > 0) // Existing entry changed
        {
            if (AccessControl.currentUserIsAdmin()) {
                DozBibManager.instance().saveEntry(doc);
                LOGGER.info("UBO saved entry with ID " + ID);
                res.sendRedirect(MCRServlet.getServletBaseURL() + "DozBibEntryServlet?mode=show&id=" + ID);
            } else {
                res.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
        } else
        // New entry submitted
        {
            int newID = DozBibManager.instance().saveEntry(doc);
            LOGGER.info("UBO saved entry with ID " + newID);

            if (AccessControl.currentUserIsAdmin()) {
                res.sendRedirect(MCRServlet.getServletBaseURL() + "DozBibEntryServlet?mode=show&id=" + newID);

            } else {
                // Notify library staff via e-mail
                sendNotificationMail(doc);
                res.sendRedirect(MCRServlet.getServletBaseURL() + "DozBibEntryServlet?mode=show&XSL.step=confirm.submitted&id=" + newID);
            }
        }
    }
}
