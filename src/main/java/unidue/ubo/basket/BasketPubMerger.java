package unidue.ubo.basket;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRConstants;
import org.mycore.datamodel.metadata.MCRMetaLinkID;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.basket.MCRBasket;
import org.mycore.frontend.basket.MCRBasketEntry;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;
import org.mycore.mods.MCRMODSWrapper;
import org.mycore.mods.merger.MCRMergeTool;

import unidue.ubo.AccessControl;
import unidue.ubo.DozBibEntryServlet;

/** Merges all publications in basket into a single one */
public class BasketPubMerger extends MCRServlet {

    private final static Logger LOGGER = LogManager.getLogger(BasketPubMerger.class);

    public void doGetPost(MCRServletJob job) throws Exception {
        HttpServletResponse res = job.getResponse();

        if (!AccessControl.currentUserIsAdmin()) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        if (AccessControl.systemInReadOnlyMode()) {
            DozBibEntryServlet.sendReadOnlyError(res);
            return;
        }

        MCRBasket basket = BasketUtils.getBasket();
        MCRObject master = getObject(basket, 0);
        Element modsMaster = getMODS(master);

        for (int i = 1; i < basket.size(); i++) {
            MCRBasketEntry entry = basket.get(i);
            LOGGER.info("merging entry 0 = " + master.getId() + " with entry " + i + " = " + entry.getID() + "...");
            Element modsOther = getMODS(getObject(basket, 0));
            MCRMergeTool.merge(modsMaster, modsOther);
        }

        LOGGER.info("saving merged master entry " + basket.get(0).getID() + "...");
        MCRMetadataManager.update(master);
        basket.get(0).setContent(master.createXML().detachRootElement());

        for (int i = basket.size() - 1; i > 0; i--) {
            MCRObject obj = getObject(basket, i);

            adoptChildren(master, obj);

            MCRObjectID oid = obj.getId();
            LOGGER.info("deleting merged entry " + i + " = " + oid + "...");
            MCRMetadataManager.deleteMCRObject(oid);

            basket.remove(i);
        }

        res.sendRedirect(getServletBaseURL() + "MCRBasketServlet?type=bibentries&action=show");
    }

    private void adoptChildren(MCRObject newParent, MCRObject oldParent) throws MCRAccessException {
        List<MCRMetaLinkID> childLinks = oldParent.getStructure().getChildren();
        for (MCRMetaLinkID childLink : childLinks) {
            MCRObjectID childID = childLink.getXLinkHrefID();

            LOGGER.info("child " + childID + " of " + oldParent.getId() + " adopted by " + newParent.getId() + "...");
            MCRObject childObject = MCRMetadataManager.retrieveMCRObject(childID);
            adoptChild(newParent, childObject);
        }
    }

    private void adoptChild(MCRObject parent, MCRObject child) throws MCRAccessException {
        child.getStructure().setParent(parent.getId());
        setRelatedItemHostLink(parent, child);
        MCRMetadataManager.update(child);
    }

    private void setRelatedItemHostLink(MCRObject parent, MCRObject child) {
        Element mods = getMODS(child);
        for (Element relatedItem : mods.getChildren("relatedItem", MCRConstants.MODS_NAMESPACE)) {
            if ("host".equals(relatedItem.getAttributeValue("type"))) {
                Attribute href = relatedItem.getAttribute("href", MCRConstants.XLINK_NAMESPACE);
                href.setValue(parent.getId().toString());
                break;
            }
        }
    }

    private MCRObject getObject(MCRBasket basket, int index) {
        Element xml = basket.get(index).getContent().clone();
        return new MCRObject(new Document(xml));
    }

    private Element getMODS(MCRObject obj) {
        return new MCRMODSWrapper(obj).getMODS();
    }
}
