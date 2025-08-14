package org.mycore.ubo.modsperson;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRConstants;
import org.mycore.common.MCRPersistenceException;
import org.mycore.common.events.MCREvent;
import org.mycore.common.events.MCREventHandlerBase;
import org.mycore.datamodel.common.MCRLinkTableManager;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.mods.MCRMODSWrapper;

import java.util.List;

/**
 * Handles updates and removals of modsperson-objects. When deleting a modsperson-object, references to this object
 * need to also be deleted from mods-objects. This happens in
 * {@link MODSPersonEventHandler#removeReferencesInMods(String)}.
 */
public class MODSPersonEventHandler extends MCREventHandlerBase {

    private final static Logger LOGGER = LogManager.getLogger(MODSPersonEventHandler.class);

    @Override
    protected void handleObjectUpdated(MCREvent evt, MCRObject obj) {
        if (!"modsperson".equals(obj.getId().getTypeId())) {
            return;
        }
        MODSPersonLookup.update(obj);
        LOGGER.info("Updated modsperson " + obj.getId().toString() + " in cache");
    }

    @Override
    protected void handleObjectDeleted(MCREvent evt, MCRObject obj) {
        if (!"modsperson".equals(obj.getId().getTypeId())) {
            return;
        }
        MODSPersonLookup.remove(obj);

        int removed = removeReferencesInMods(obj.getId().toString());
        LOGGER.debug("Removed " + removed + " references in mods objects.");
        LOGGER.info("Deleted modsperson " + obj.getId().toString() + " from cache and in mods references");
    }

    /**
     * For a given modsperson-ID, remove references to this ID in all mods-Documents.
     * @param modspersonId ID of modsperson that was deleted
     * @return number of references in mods-documents found and deleted
     */
    private int removeReferencesInMods(String modspersonId) {
            MCRLinkTableManager linkTableManager = MCRLinkTableManager.getInstance();
            MCRObjectID modspersonObjectId = MCRObjectID.getInstance(modspersonId);
            List<String> modsReferenceIds = (List) linkTableManager.getSourceOf(modspersonObjectId);
            for (String modsReferenceId : modsReferenceIds) {
            try {
                MCRObjectID modsId = MCRObjectID.getInstance(modsReferenceId);
                MCRObject obj = MCRMetadataManager.retrieveMCRObject(modsId);
                MCRMODSWrapper wrapper = new MCRMODSWrapper(obj);
                wrapper.getElements("mods:name[@type='personal']").forEach(modsName -> {
                    if (modsName.getAttribute("href", MCRConstants.XLINK_NAMESPACE) != null &&
                        modspersonId.equals(modsName.getAttributeValue("href", MCRConstants.XLINK_NAMESPACE))) {
                        modsName.removeAttribute("href", MCRConstants.XLINK_NAMESPACE);
                        try {
                            MCRMetadataManager.update(obj);
                        } catch (MCRPersistenceException | MCRAccessException e) {
                            LOGGER.warn("Error while updating object " + modsId.toString(), e);
                        }
                    }
                });
            } catch (MCRPersistenceException e) {
                LOGGER.warn("Error while trying to access object " + modsReferenceId, e);
            }
        }

            linkTableManager.deleteReferenceLink(modspersonObjectId);
            return modsReferenceIds.size();
    }

}

