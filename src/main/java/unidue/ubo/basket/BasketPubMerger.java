package unidue.ubo.basket;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRConstants;
import org.mycore.common.MCRPersistenceException;
import org.mycore.datamodel.common.MCRActiveLinkException;
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

/**
 * Merges publications in basket.
 * By default, merges the publications in basket into a single one, assuming all are dupulicates.
 * With parameter "target=hosts", extracts the host items of all publications in basket and merges into a single host,
 * assuming all publications in basket have the same host but maybe with duplicate/unseparated host entries.
 *
 * @author Frank L\u00FCtzenkirchen
 */
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

        if ("hosts".equals(job.getRequest().getParameter("target"))) {
            mergeHosts(basket);
        } else {
            List<MCRObject> duplicates = basket2objects(basket);
            mergeDuplicates(basket, duplicates);
        }
        res.sendRedirect(getServletBaseURL() + "MCRBasketServlet?type=bibentries&action=show");
    }

    private void mergeHosts(MCRBasket basket) throws MCRAccessException, MCRActiveLinkException {
        List<MCRObject> children = basket2objects(basket);
        Set<MCRObjectID> parentIDs = new LinkedHashSet<MCRObjectID>();

        for (MCRObject child : children) {
            Element hostItem = getHostItem(child);
            if (hostItem == null) {
                continue;
            }

            Attribute href = hostItem.getAttribute("href", MCRConstants.XLINK_NAMESPACE);
            if ((href == null) || (href.getValue().isEmpty())) {
                LOGGER.info("extract host inside " + child.getId() + "...");
                String nullID = MCRObjectID.formatID(child.getId().getBase(), 0);
                hostItem.setAttribute("href", nullID, MCRConstants.XLINK_NAMESPACE);
                MCRMetadataManager.update(child);
                updateInBasket(basket, child);
            }

            MCRObjectID parentID = child.getParent();
            LOGGER.info("found host to merge: " + parentID);
            parentIDs.add(parentID);
        }

        List<MCRObject> duplicates = parentIDs.stream().map(id -> MCRMetadataManager.retrieveMCRObject(id))
            .collect(Collectors.toList());

        mergeDuplicates(basket, duplicates);
    }

    private void mergeDuplicates(MCRBasket basket, List<MCRObject> duplicates)
        throws MCRAccessException, MCRActiveLinkException {
        if (duplicates.size() < 2) {
            return;
        }

        MCRObject master = mergeMODS(duplicates);

        LOGGER.info("saving merged master " + master.getId() + "...");
        MCRMetadataManager.update(master);
        updateInBasket(basket, master);

        for (MCRObject duplicate : duplicates) {
            adoptChildrenThenKillFather(master, duplicate);
            removeFromBasket(basket, duplicate.getId());
        }
    }

    private void updateInBasket(MCRBasket basket, MCRObject obj) {
        MCRBasketEntry entry = basket.get(obj.getId().toString());
        if (entry != null) {
            entry.setContent(obj.createXML().clone().detachRootElement());
        }
    }

    private void removeFromBasket(MCRBasket basket, MCRObjectID oid) {
        MCRBasketEntry entry = basket.get(oid.toString());
        if (entry != null) {
            basket.remove(entry);
        }
    }

    private List<MCRObject> basket2objects(MCRBasket basket) {
        List<MCRObject> duplicates = basket.stream()
            .map(e -> new MCRObject(new Document(e.getContent().clone()))).collect(Collectors.toList());
        return duplicates;
    }

    private MCRObject mergeMODS(List<MCRObject> duplicates) {
        MCRObject master = duplicates.remove(0);
        Element modsMaster = new MCRMODSWrapper(master).getMODS();

        for (MCRObject duplicate : duplicates) {
            LOGGER.info("merging " + master.getId() + " with " + duplicate.getId() + "...");
            Element modsOther = new MCRMODSWrapper(duplicate).getMODS();
            MCRMergeTool.merge(modsMaster, modsOther);
        }

        return master;
    }

    private void adoptChildrenThenKillFather(MCRObject newParent, MCRObject oldParent)
        throws MCRAccessException, MCRPersistenceException, MCRActiveLinkException {
        List<MCRMetaLinkID> childLinks = oldParent.getStructure().getChildren();
        for (MCRMetaLinkID childLink : childLinks) {
            MCRObjectID childID = childLink.getXLinkHrefID();

            LOGGER.info("child " + childID + " of " + oldParent.getId() + " adopted by " + newParent.getId() + "...");
            MCRObject childObject = MCRMetadataManager.retrieveMCRObject(childID);
            adoptChild(newParent, childObject);
        }

        LOGGER.info("deleting merged entry " + oldParent.getId() + "...");
        // delete using ID, otherwise structure part of old parent is not up-to-date and will delete adopted children!
        MCRMetadataManager.deleteMCRObject(oldParent.getId());
    }

    private void adoptChild(MCRObject parent, MCRObject child) throws MCRAccessException {
        child.getStructure().setParent(parent.getId());
        setRelatedItemHostLink(parent, child);
        MCRMetadataManager.update(child);
    }

    private void setRelatedItemHostLink(MCRObject parent, MCRObject child) {
        Element hostItem = getHostItem(child);
        Attribute href = hostItem.getAttribute("href", MCRConstants.XLINK_NAMESPACE);
        href.setValue(parent.getId().toString());
    }

    private Element getHostItem(MCRObject obj) {
        Element mods = new MCRMODSWrapper(obj).getMODS();
        for (Element relatedItem : mods.getChildren("relatedItem", MCRConstants.MODS_NAMESPACE)) {
            if ("host".equals(relatedItem.getAttributeValue("type"))) {
                return relatedItem;
            }
        }
        return null;
    }
}
