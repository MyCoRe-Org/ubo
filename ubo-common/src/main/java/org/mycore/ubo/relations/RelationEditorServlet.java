package org.mycore.ubo.relations;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRConstants;
import org.mycore.datamodel.metadata.MCRMetaLinkID;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;
import org.mycore.mods.MCRMODSWrapper;
import org.mycore.mods.merger.MCRMergeTool;
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
        } else if (!AccessControl.currentUserIsAdmin()) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN);
        } else {
            String action = req.getParameter("action");

            if ("delete".equals(action)) {
                deleteEntry(req, res);
            } else if ("extractHost".equals(action)) {
                extractHost(req, res);
            } else if ("linkHost".equals(action)) {
                linkHost(req, res);
            } else if ("unlinkHost".equals(action)) {
                unlinkHost(req, res);
            } else if ("merge".equals(action)) {
                merge(req, res);
            }

            if (!res.isCommitted()) {
                String base = req.getParameter("base");
                res.sendRedirect("DozBibEntryServlet?id=" + base + "&XSL.Style=structure");
            }
        }
    }

    private MCRObjectID getAndCheck(HttpServletRequest req, HttpServletResponse res, String parameterName)
        throws IOException {
        String id = req.getParameter(parameterName);
        if ((id == null) || id.isBlank()) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameter '" + parameterName + "'");
            return null;
        }

        if (!MCRObjectID.isValid(id)) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid object ID '" + id + "'");
            return null;
        }

        MCRObjectID oid = MCRObjectID.getInstance(id);
        if (!MCRMetadataManager.exists(oid)) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, "No object with ID '" + id + "'");
            return null;
        }

        new ParentChildRelationChecker(oid, false).check();

        return oid;
    }

    private void deleteEntry(HttpServletRequest req, HttpServletResponse res) throws Exception {
        MCRObjectID oid = getAndCheck(req, res, "id");
        deleteIfNoChildren(res, oid);
    }

    // do not delete entries that have linked children, otherwise the children would be deleted too
    private void deleteIfNoChildren(HttpServletResponse res, MCRObjectID oid)
        throws Exception {
        
        LOGGER.info("UBO delete entry " + oid);
        MCRObject obj = MCRMetadataManager.retrieveMCRObject(oid);

        if (!obj.getStructure().getChildren().isEmpty()) {
            res.sendError(HttpServletResponse.SC_CONFLICT, "entry has child(ren), wont delete");
        } else {
            new ParentChildRelationChecker(oid, false).check();
            MCRMetadataManager.deleteMCRObject(obj.getId());
        }
    }

    /** Extract mods:relatedItem[@type='host'] to a new separate entry and link it via @xlink:href */
    private void extractHost(HttpServletRequest req, HttpServletResponse res) throws Exception {
        MCRObjectID oid = getAndCheck(req, res, "id");
        LOGGER.info("UBO extract host in entry " + oid);

        MCRObject obj = MCRMetadataManager.retrieveMCRObject(oid);
        MCRMODSWrapper wrapper = new MCRMODSWrapper(obj);

        // Just add an empty dummy ID as xlink:href attribute, this triggers
        // MCRExtractRelatedItemsEventHandler which will do the actual work afterwards
        String nullID = MCRObjectID.formatID(oid.getBase(), 0);

        Element hostItem = wrapper.getElement("mods:relatedItem[@type='host']");
        hostItem.setAttribute("href", nullID, MCRConstants.XLINK_NAMESPACE);

        MCRMetadataManager.update(obj);
        new ParentChildRelationChecker(oid, false).check();
    }

    /** Link host in mods:relatedItem[@type='host']/@xlink:href and parent */
    private void linkHost(HttpServletRequest req, HttpServletResponse res) throws Exception {
        MCRObjectID childID = getAndCheck(req, res, "child");
        MCRObjectID parentID = getAndCheck(req, res, "parent");

        LOGGER.info("UBO link entry " + childID + " to host " + parentID);
        setParent(childID, parentID);

        new ParentChildRelationChecker(childID, false).check();
    }

    private void setParent(MCRObjectID childID, MCRObjectID parentID) throws MCRAccessException {
        LOGGER.info("UBO set parent of " + childID + " to " + parentID);

        MCRObject child = MCRMetadataManager.retrieveMCRObject(childID);

        child.getStructure().setParent(parentID);

        MCRMODSWrapper childWrapper = new MCRMODSWrapper(child);
        Element relatedItem = childWrapper.getElement("mods:relatedItem[@type='host']");
        relatedItem.setAttribute("href", parentID.toString(), MCRConstants.XLINK_NAMESPACE);

        MCRMetadataManager.update(child);
    }

    /** Unlink host in mods:relatedItem[@type='host']/@xlink:href and parent */
    private void unlinkHost(HttpServletRequest req, HttpServletResponse res) throws Exception {
        MCRObjectID childID = getAndCheck(req, res, "child");
        LOGGER.info("UBO unlink host in entry " + childID);

        MCRObject child = MCRMetadataManager.retrieveMCRObject(childID);
        child.getStructure().removeParent();

        MCRMODSWrapper wrapper = new MCRMODSWrapper(child);
        Element host = wrapper.getElement("mods:relatedItem[@type='host']");
        host.getAttribute("href", MCRConstants.XLINK_NAMESPACE).detach();

        MCRMetadataManager.update(child);
        new ParentChildRelationChecker(childID, false).check();
    }

    private void merge(HttpServletRequest req, HttpServletResponse res) throws Exception {
        MCRObjectID intoID = getAndCheck(req, res, "into");
        MCRObjectID fromID = getAndCheck(req, res, "from");
        LOGGER.info("UBO merge " + fromID + " into " + intoID);

        MCRObject objInto = MCRMetadataManager.retrieveMCRObject(intoID);
        MCRObject objFrom = MCRMetadataManager.retrieveMCRObject(fromID);

        Element modsToMergeInto = new MCRMODSWrapper(objInto).getMODS();
        Element modsToMergeFrom = new MCRMODSWrapper(objFrom).getMODS();
        MCRMergeTool.merge(modsToMergeInto, modsToMergeFrom);

        MCRMetadataManager.update(objInto);

        List<MCRMetaLinkID> childrenToAdopt = objFrom.getStructure().getChildren();
        while (!childrenToAdopt.isEmpty()) {
            MCRMetaLinkID childLink = childrenToAdopt.get(0);
            childrenToAdopt.remove(childLink);

            MCRObjectID childID = childLink.getXLinkHrefID();
            setParent(childID, intoID);
        }

        deleteIfNoChildren(res, fromID);
        new ParentChildRelationChecker(intoID, false).check();
    }
}
