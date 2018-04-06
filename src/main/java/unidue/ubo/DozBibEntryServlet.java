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
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.metadata.MCRMetaLinkID;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.common.MCRConstants;
import org.mycore.common.MCRMailer;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;
import org.mycore.mods.MCRMODSWrapper;

public class DozBibEntryServlet extends MCRServlet {

    private final static Logger LOGGER = LogManager.getLogger(DozBibEntryServlet.class);

    public void doGetPost(MCRServletJob job) throws Exception {
        HttpServletRequest req = job.getRequest();
        HttpServletResponse res = job.getResponse();

        String mode = req.getParameter("mode");
        if ((mode == null) || mode.isEmpty() || "show".equals(mode)) {
            showEntry(req, res);
        } else if (AccessControl.systemInReadOnlyMode()) {
            sendReadOnlyError(res);
        } else if ("delete".equals(mode)) {
            deleteEntry(req, res);
        } else if ("save".equals(mode)) {
            saveEntry(req, res);
        } else if ("xhost".equals(mode)) {
            extractHostEntry(req, res);
        }
    }

    public static void sendReadOnlyError(HttpServletResponse res) throws IOException {
        String msg = MCRConfiguration.instance().getString("UBO.System.ReadOnly.Message");
        res.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, msg);
        LOGGER.info("rejected editing because system is in read-only mode");
    }

    private void showEntry(HttpServletRequest req, HttpServletResponse res) throws Exception {
        String ID = req.getParameter("id");
        if (isValidID(ID)) {
            LOGGER.info("UBO show entry " + ID);
            MCRObjectID oid = MCRObjectID.getInstance(ID);
            Document xml = MCRMetadataManager.retrieveMCRObject(oid).createXML();
            getLayoutService().doLayout(req, res, new MCRJDOMContent(xml));
        } else if (isLegacyPublicationID(ID)) {
            redirectToCurrentPublicationID(res, ID);
        } else {
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void redirectToCurrentPublicationID(HttpServletResponse res, String ID) throws IOException {
        int id = Integer.parseInt(ID);
        MCRObjectID oid = MCRObjectID.getInstance(MCRObjectID.formatID("ubo_mods", id));
        String url = "DozBibEntryServlet?id=" + oid.toString();
        res.sendRedirect(url);
    }

    private boolean isLegacyPublicationID(String ID) {
        return (ID != null) && ID.matches("\\d+");
    }

    private void deleteEntry(HttpServletRequest req, HttpServletResponse res) throws Exception {
        if (!AccessControl.currentUserIsAdmin()) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String ID = req.getParameter("id");
        if (!isValidID(ID)) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        LOGGER.info("UBO delete entry " + ID);

        MCRObjectID oid = MCRObjectID.getInstance(ID);
        MCRObject obj = MCRMetadataManager.retrieveMCRObject(oid);

        // do not delete entries that have linked children, otherwise the children would be deleted too
        List<MCRMetaLinkID> children = obj.getStructure().getChildren();
        if (!children.isEmpty()) {
            res.sendError(HttpServletResponse.SC_CONFLICT, "entry has " + children.size() + " child(ren)");
            return;
        }

        new MCRMODSWrapper(obj).setServiceFlag("status", "deleted");
        Document xml = obj.createXML();

        MCRMetadataManager.deleteMCRObject(oid);
        req.setAttribute("XSL.step", "confirm.deleted");
        getLayoutService().doLayout(req, res, new MCRJDOMContent(xml));
    }

    private boolean isValidID(String id) throws IOException {
        return id != null && MCRObjectID.isValid(id)
            && MCRXMLMetadataManager.instance().exists(MCRObjectID.getInstance(id));
    }

    private void sendNotificationMail(Document doc) throws Exception {
        LOGGER.info("UBO sending notification e-mail");

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("WebApplicationBaseURL", MCRFrontendUtil.getBaseURL());
        parameters.put("MCR.Mail.Address", MCRConfiguration.instance().getString("MCR.Mail.Address"));
        MCRMailer.sendMail(doc, "mycoreobject-e-mail", parameters);
    }

    private void saveEntry(HttpServletRequest req, HttpServletResponse res) throws Exception {
        Document doc = (Document) req.getAttribute("MCRXEditorSubmission");

        String id = doc.getRootElement().getAttributeValue("ID");
        MCRObjectID oid = MCRObjectID.getInstance(id);
        MCRObject obj = new MCRObject(doc);

        if (MCRXMLMetadataManager.instance().exists(oid)) {
            if (AccessControl.currentUserIsAdmin()) {
                MCRMetadataManager.update(obj);
                LOGGER.info("UBO saved entry with ID " + oid);
                res.sendRedirect(MCRServlet.getServletBaseURL() + "DozBibEntryServlet?id=" + id);
            } else {
                res.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
        } else
        // New entry submitted
        {
            oid = MCRObjectID.getNextFreeId(oid.getBase());
            obj.setId(oid);
            MCRMetadataManager.create(obj);
            LOGGER.info("UBO saved entry with ID " + oid);

            if (AccessControl.currentUserIsAdmin()) {
                res.sendRedirect(MCRServlet.getServletBaseURL() + "DozBibEntryServlet?id=" + oid.toString());

            } else {
                // Notify library staff via e-mail
                sendNotificationMail(obj.createXML());
                res.sendRedirect(MCRServlet.getServletBaseURL()
                    + "DozBibEntryServlet?XSL.step=confirm.submitted&id=" + oid.toString());
            }
        }
    }

    /** Extract mods:relatedItem[@type='host'] to a new separate entry and link it via xlink:href */
    private void extractHostEntry(HttpServletRequest req, HttpServletResponse res) throws Exception {
        if (!AccessControl.currentUserIsAdmin()) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String ID = req.getParameter("id");
        if (!isValidID(ID)) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        MCRObjectID childID = MCRObjectID.getInstance(ID);
        MCRObject obj = MCRMetadataManager.retrieveMCRObject(childID);

        Element mods = new MCRMODSWrapper(obj).getMODS();
        for (Element relatedItem : mods.getChildren("relatedItem", MCRConstants.MODS_NAMESPACE)) {
            String type = relatedItem.getAttributeValue("type");
            if (!"host".equals(type)) {
                continue;
            }

            String href = relatedItem.getAttributeValue("href", MCRConstants.XLINK_NAMESPACE);
            if ((href != null) && !href.isEmpty()) {
                continue;
            }

            LOGGER.info("UBO extract host in entry " + ID);

            // Just add an empty dummy ID as xlink:href attribute, this triggers
            // MCRExctractRelatedItemsEventHandler which will do the actual work afterwards
            String nullID = MCRObjectID.formatID(childID.getBase(), 0);
            relatedItem.setAttribute("href", nullID, MCRConstants.XLINK_NAMESPACE);
            MCRMetadataManager.update(obj);
            break;
        }

        res.sendRedirect(MCRServlet.getServletBaseURL() + "DozBibEntryServlet?id=" + childID);
    }
}
