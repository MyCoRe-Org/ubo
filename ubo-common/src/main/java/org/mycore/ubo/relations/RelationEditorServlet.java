package org.mycore.ubo.relations;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Content;
import org.jdom2.Element;
import org.mycore.common.MCRConstants;
import org.mycore.datamodel.metadata.MCRMetaLinkID;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;
import org.mycore.mods.MCRMODSWrapper;
import org.mycore.ubo.AccessControl;
import org.mycore.ubo.DozBibEntryServlet;

public class RelationEditorServlet extends MCRServlet {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LogManager.getLogger(RelationEditorServlet.class);

    @Override
    public void doGetPost(MCRServletJob job) throws Exception {
        HttpServletRequest req = job.getRequest();
        HttpServletResponse res = job.getResponse();

        if (AccessControl.systemInReadOnlyMode()) {
            DozBibEntryServlet.sendReadOnlyError(res);
            return;
        }

        if (!AccessControl.currentUserIsAdmin()) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String id = req.getParameter("id");
        if (!DozBibEntryServlet.isValidID(id)) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        MCRObjectID oid = MCRObjectID.getInstance(id);
        new ParentChildRelationChecker(oid, false).check();

        MCRObject obj = MCRMetadataManager.retrieveMCRObject(oid);

        String action = req.getParameter("action");
        if ("delete".equals(action)) {
            deleteEntry(obj, res);
            oid = MCRObjectID.getInstance(req.getParameter("from"));
        } else {
            if ("extractHost".equals(action)) {
                extractHostFrom(obj);
            } else if ("linkHost".equals(action)) {
                MCRObjectID parentID = MCRObjectID.getInstance(req.getParameter("host"));
                new ParentChildRelationChecker(parentID, false).check();
                linkHost(obj, parentID);
            } else if ("unlinkHost".equals(action)) {
                unlinkHostIn(obj);
                new ParentChildRelationChecker(oid, false).check();
                oid = MCRObjectID.getInstance(req.getParameter("from"));
            } else {
                res.sendError(HttpServletResponse.SC_BAD_REQUEST, "missing 'action' parameter");
                return;
            }

            MCRMetadataManager.update(obj);
            new ParentChildRelationChecker(oid, false).check();
        }

        res.sendRedirect(MCRServlet.getServletBaseURL() + "DozBibEntryServlet?id=" + oid + "&XSL.Style=structure");
    }

    private void deleteEntry(MCRObject obj, HttpServletResponse res) throws Exception {
        LOGGER.info("UBO delete entry " + obj.getId());

        // do not delete entries that have linked children, otherwise the children would be deleted too
        List<MCRMetaLinkID> children = obj.getStructure().getChildren();
        if (!children.isEmpty()) {
            res.sendError(HttpServletResponse.SC_CONFLICT, "entry has " + children.size() + " child(ren)");
        }
        MCRMetadataManager.deleteMCRObject(obj.getId());
    }

    /** Extract mods:relatedItem[@type='host'] to a new separate entry and link it via @xlink:href */
    private void extractHostFrom(MCRObject obj)
        throws Exception {
        LOGGER.info("UBO extract host in entry " + obj.getId());

        MCRMODSWrapper wrapper = new MCRMODSWrapper(obj);

        // Just add an empty dummy ID as xlink:href attribute, this triggers
        // MCRExtractRelatedItemsEventHandler which will do the actual work afterwards
        String nullID = MCRObjectID.formatID(obj.getId().getBase(), 0);

        Element hostItem = wrapper.getElement("mods:relatedItem[@type='host']");
        hostItem.setAttribute("href", nullID, MCRConstants.XLINK_NAMESPACE);
    }

    /** Link host in mods:relatedItem[@type='host']/@xlink:href and parent */
    private void linkHost(MCRObject obj, MCRObjectID parentID)
        throws Exception {
        LOGGER.info("UBO link entry " + obj.getId() + " to host " + parentID);

        obj.getStructure().setParent(parentID);

        MCRObject parent = MCRMetadataManager.retrieveMCRObject(parentID);
        MCRMODSWrapper parentWrapper = new MCRMODSWrapper(parent);
        List<Content> parentContent = parentWrapper.getMODS().removeContent();

        MCRMODSWrapper childWrapper = new MCRMODSWrapper(obj);
        Element relatedItem = childWrapper.getElement("mods:relatedItem[@type='host']");
        relatedItem.setAttribute("href", parentID.toString(), MCRConstants.XLINK_NAMESPACE);
        Element part = relatedItem.getChild("part", MCRConstants.MODS_NAMESPACE).detach();
        relatedItem.setContent(parentContent);
        relatedItem.addContent(part);
    }

    /** Unlink host in mods:relatedItem[@type='host']/@xlink:href and parent */
    private void unlinkHostIn(MCRObject obj)
        throws Exception {
        LOGGER.info("UBO unlink host in entry " + obj.getId());

        obj.getStructure().removeParent();

        MCRMODSWrapper wrapper = new MCRMODSWrapper(obj);
        Element host = wrapper.getElement("mods:relatedItem[@type='host']");
        host.getAttribute("href", MCRConstants.XLINK_NAMESPACE).detach();
    }
}
