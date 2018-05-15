package unidue.ubo.basket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRConstants;
import org.mycore.common.MCRPersistenceException;
import org.mycore.common.content.MCRJDOMContent;
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
import org.xml.sax.SAXException;

import unidue.ubo.AccessControl;
import unidue.ubo.DozBibEntryServlet;

/**
 * Merges publications in basket.
 *
 * With parameter "commit=true",
 *   will actually change publications in store, otherwise preview only
 * With parameter "target=publications",
 *   merges the publications in basket into a single one, assuming all are dupulicates.
 * With parameter "target=hosts",
 *   extracts the host items of all publications in basket and merges into a single host,
 *   assuming all publications in basket have the same host but maybe with duplicate/unseparated host entries.
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
        String target = job.getRequest().getParameter("target");
        boolean commit = "true".equals(job.getRequest().getParameter("commit"));

        MCRObject master = null;

        if ("publications".equals(target)) {
            master = mergePublications(basket, commit);
        } else if ("hosts".equals(target)) {
            master = mergeHosts(basket, commit);
        }

        if (commit) {
            res.sendRedirect("DozBibEntryServlet?XSL.step=merged." + target + "&id=" + master.getId());
        } else {
            show(job, master, target);
        }
    }

    private MCRObject mergeHosts(MCRBasket basket, boolean commit) throws MCRAccessException, MCRActiveLinkException {
        List<MCRObject> children = basket2objects(basket);
        List<MCRObject> parents = collectParents(children);
        MCRObject master = merge(parents);
        if (commit) {
            assignValidID(master);
            MCRMetadataManager.update(master);
            adoptChildrenThenKillFathers(parents, master);
            linkUnlinkedHostItems(master, children);
            updateBasket(basket);
        }
        return master;
    }

    private void assignValidID(MCRObject master) {
        if (master.getId().getNumberAsInteger() == 0) {
            String base = master.getId().getBase();
            MCRObjectID oid = MCRObjectID.getNextFreeId(base);
            master.setId(oid);
        }
    }

    private MCRObject mergePublications(MCRBasket basket, boolean commit)
        throws MCRAccessException, MCRActiveLinkException {
        List<MCRObject> duplicates = basket2objects(basket);
        MCRObject master = merge(duplicates);
        if (commit) {
            MCRMetadataManager.update(master);
            adoptChildrenThenKillFathers(duplicates, master);
            basket.clear();
        }
        return master;
    }

    private void updateBasket(MCRBasket basket) {
        for (MCRBasketEntry entry : basket) {
            entry.resolveContent();
        }
    }

    private void linkUnlinkedHostItems(MCRObject master, List<MCRObject> children) throws MCRAccessException {
        String masterID = master.getId().toString();

        for (MCRObject child : children) {
            Element hostItem = getHostItem(child);
            if (hostItem == null) {
                continue;
            }

            Attribute href = hostItem.getAttribute("href", MCRConstants.XLINK_NAMESPACE);
            if ((href == null) || (href.getValue().isEmpty())) {
                hostItem.setAttribute("href", masterID, MCRConstants.XLINK_NAMESPACE);
                child.getStructure().setParent(masterID);
                MCRMetadataManager.update(child);
            }
        }
    }

    private List<MCRObject> collectParents(List<MCRObject> children) {
        List<MCRObject> parents = new ArrayList<MCRObject>();
        Set<String> parentIDs = new HashSet<String>();

        for (MCRObject child : children) {
            Element hostItem = getHostItem(child);
            if (hostItem != null) {
                String parentID = hostItem.getAttributeValue("href", MCRConstants.XLINK_NAMESPACE);
                if (parentID != null) {
                    if (!parentIDs.contains(parentID)) {
                        parents.add(MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(parentID)));
                    }
                } else {
                    parents.add(buildFromRelatedItem(hostItem, child.getId().getBase()));
                }
            }
        }
        return parents;
    }

    private MCRObject buildFromRelatedItem(Element relatedItem, String base) {
        Element mods = relatedItem.clone().setName("mods");
        mods.removeAttribute("type");
        mods.removeChildren("part", MCRConstants.MODS_NAMESPACE);

        MCRMODSWrapper wrapper = new MCRMODSWrapper();
        wrapper.setMODS(mods);
        MCRObject object = wrapper.getMCRObject();
        String nullID = MCRObjectID.formatID(base, 0);
        object.setId(MCRObjectID.getInstance(nullID));
        return object;
    }

    private List<MCRObject> basket2objects(MCRBasket basket) {
        List<MCRObject> objects = new ArrayList<MCRObject>();
        for (MCRBasketEntry entry : basket) {
            MCRObjectID id = MCRObjectID.getInstance(entry.getID());
            MCRObject obj = MCRMetadataManager.retrieveMCRObject(id);
            objects.add(obj);
        }
        return objects;
    }

    private MCRObject merge(List<MCRObject> duplicates) {
        MCRObject master = duplicates.remove(0);
        Element modsMaster = new MCRMODSWrapper(master).getMODS();

        for (MCRObject duplicate : duplicates) {
            LOGGER.info("merging " + master.getId() + " with " + duplicate.getId() + "...");
            Element modsOther = new MCRMODSWrapper(duplicate).getMODS();
            MCRMergeTool.merge(modsMaster, modsOther);
        }

        return master;
    }

    private void show(MCRServletJob job, MCRObject object, String target)
        throws IOException, TransformerException, SAXException {
        Document xml = object.createXML();
        job.getRequest().setAttribute("XSL.step", "ask." + target);
        getLayoutService().doLayout(job.getRequest(), job.getResponse(), new MCRJDOMContent(xml));
    }

    private void adoptChildrenThenKillFathers(List<MCRObject> duplicates, MCRObject master)
        throws MCRAccessException, MCRActiveLinkException {
        for (MCRObject duplicate : duplicates) {
            if (duplicate.getId().getNumberAsInteger() > 0) {
                adoptChildrenThenKillFather(master, duplicate);
            }
        }
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
