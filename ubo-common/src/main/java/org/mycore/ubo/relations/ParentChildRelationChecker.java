package org.mycore.ubo.relations;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.mycore.common.MCRConstants;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.metadata.MCRMetaLinkID;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.metadata.MCRObjectStructure;
import org.mycore.frontend.cli.annotation.MCRCommand;
import org.mycore.frontend.cli.annotation.MCRCommandGroup;
import org.mycore.mods.MCRMODSWrapper;

/**
 * Checks the parent and child relations for the given object.
 * Checks structure part with links down to children and up to parent,
 * ensuring they are present in both directrions.
 * Checks relatedItem[@type='host']/@xlink:href to be same as the parent link.
 *
 * Call new ParentChildRelationChecker(...).check() or use the CLI commands.
 * 
 * @author Frank L\U00FCtzenkirchen
 **/
@MCRCommandGroup(name = "UBO relation commands")
public class ParentChildRelationChecker {

    private final static Logger LOGGER = LogManager.getLogger();

    @MCRCommand(
        syntax = "ubo check all relations repair {0}",
        help = "Checks parent-child relations in both structure and relatedItem[@type='host'], repair=[true|false]",
        order = 1)
    public static void checkAllRelations(String sRepair) {
        for (String id : MCRXMLMetadataManager.instance().listIDs()) {
            checkRelations(id, sRepair);
        }
    }

    @MCRCommand(
        syntax = "ubo check relations of {0} repair {1}",
        help = "Checks parent-child relations in both structure and relatedItem[@type='host'], repair = [true|false]",
        order = 2)
    public static void checkRelations(String id, String sRepair) {
        MCRObjectID oid = MCRObjectID.getInstance(id);

        try {
            new ParentChildRelationChecker(oid, Boolean.valueOf(sRepair)).check();
        } catch (StructureException canIgnoreBecauseAlreadyLogged) {
        } catch (Exception ex) {
            LOGGER.warn(ex.getClass().getName() + ": " + ex.getMessage());
        }
    }

    private MCRObject obj;

    private MCRObjectID oid;

    private MCRObjectStructure structure;

    private MCRObjectID parentID;

    private List<MCRMetaLinkID> childLinks;

    private List<Element> hosts;

    private boolean shouldRepair = false;

    public ParentChildRelationChecker(MCRObjectID oid, boolean shouldRepair) {
        this.oid = oid;
        this.obj = MCRMetadataManager.retrieveMCRObject(oid);
        this.structure = obj.getStructure();
        this.parentID = obj.getParent();
        this.childLinks = structure.getChildren();
        this.hosts = getHostRelations(obj);
        this.shouldRepair = shouldRepair;
    }

    public void check() throws StructureException {
        checkHasBothChildrenAndParent();

        if (parentID != null) {
            checkForNonExistingParent();
            checkForParentMissingChild();
        }

        checkChildren();

        checkNumberOfHosts();
        checkHostEqualsParent();
    }

    private void problemFound(String msg, boolean repaired) {
        msg = oid + " " + msg;
        if (repaired) {
            LOGGER.info("REPAIRED: " + msg);
        } else {
            LOGGER.warn("CAN'T REPAIR: " + msg);
            throw new StructureException(msg);
        }
    }

    private void checkHostEqualsParent() {
        if (hosts.isEmpty()) {
            return;
        }

        MCRObjectID hostID = getHostID(hosts);

        if (hostID == null) {
            return;
        }

        if (parentID == null) {
            problemFound("has host " + hostID + ", but no parent set", false);
        }

        if (!hostID.equals(parentID)) {
            problemFound("has parent " + parentID + ", but host " + hostID, false);
        }
    }

    private void checkNumberOfHosts() {
        if (hosts.size() > 1) {
            problemFound("has more than one host relation in MODS", false);
        }
    }

    private void checkChildren() {
        for (MCRMetaLinkID childLink : new ArrayList<MCRMetaLinkID>(childLinks)) {
            checkForNonExistingChild(childLink);
            checkForChildWithoutLinkToParent(childLink);
        }
    }

    private void checkForChildWithoutLinkToParent(MCRMetaLinkID childLink) {
        MCRObjectID childID = childLink.getXLinkHrefID();
        if (!oid.equals(MCRMetadataManager.retrieveMCRObject(childID).getParent())) {
            problemFound("has link to child " + childID + ", but child has no link up", false);
        }
    }

    private void checkForNonExistingChild(MCRMetaLinkID childLink) {
        MCRObjectID childID = childLink.getXLinkHrefID();
        if (!MCRMetadataManager.exists(childID)) {
            if (shouldRepair) {
                childLinks.remove(childLink);
                MCRMetadataManager.fireUpdateEvent(obj);
            }
            problemFound("has link to non-existing child " + childID, shouldRepair);
        }
    }

    private void checkForParentMissingChild() {
        MCRObject parent = MCRMetadataManager.retrieveMCRObject(parentID);
        MCRObjectStructure parentStructure = parent.getStructure();

        if (!parentStructure.getChildren().stream().anyMatch(c -> c.getXLinkHrefID().equals(oid))) {
            if (shouldRepair) {
                parentStructure.addChild(new MCRMetaLinkID("child", oid, null, null));
                MCRMetadataManager.fireUpdateEvent(parent);
            }
            problemFound("has link to parent " + parentID + ", but parent has no link down", shouldRepair);
        }
    }

    private void checkHasBothChildrenAndParent() {
        if ((parentID != null) && (!childLinks.isEmpty())) {
            LOGGER.warn("{} has both parent and children", oid);
        }
    }

    private void checkForNonExistingParent() {
        if (!MCRMetadataManager.exists(parentID)) {
            problemFound("has link to non-existing parent " + parentID, false);
        }
    }

    private MCRObjectID getHostID(List<Element> hosts) {
        Element hostElement = hosts.get(0);
        String href = hostElement.getAttributeValue("href", MCRConstants.XLINK_NAMESPACE);
        return href == null ? null : MCRObjectID.getInstance(href);
    }

    private List<Element> getHostRelations(MCRObject obj) {
        Element mods = new MCRMODSWrapper(obj).getMODS();
        List<Element> relations = mods.getChildren("relatedItem", MCRConstants.MODS_NAMESPACE);
        List<Element> hosts = relations.stream()
            .filter(e -> "host".equals(e.getAttributeValue("type")))
            .collect(Collectors.toList());
        return hosts;
    }
}

class StructureException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    StructureException(String message) {
        super(message + "!");
    }
}
