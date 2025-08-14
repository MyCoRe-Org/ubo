package org.mycore.ubo.modsperson;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRConstants;
import org.mycore.common.MCRPersistenceException;
import org.mycore.datamodel.common.MCRLinkTableManager;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.metadata.MCRPreDeleteAction;
import org.mycore.mods.MCRMODSWrapper;

import java.util.List;

public class MODSPersonReferenceDeletionAction implements MCRPreDeleteAction {

    private final static Logger LOGGER = LogManager.getLogger(MODSPersonReferenceDeletionAction.class);

    @Override
    public void execute(MCRObjectID id) {
        int deletedObjects = removeReferencesInMods(id.toString());
        LOGGER.info("Removed reference to MODS person {} from {} objects", id.toString(), deletedObjects);
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
