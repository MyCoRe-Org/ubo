package org.mycore.ubo.dedup;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.mycore.common.events.MCREvent;
import org.mycore.common.events.MCREventHandlerBase;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.mods.MCRMODSWrapper;
import org.mycore.ubo.dedup.jpa.DeduplicationKeyManager;

public class DeDupEventHandler extends MCREventHandlerBase {

    private final static Logger LOGGER = LogManager.getLogger();

    @Override
    protected void handleObjectCreated(MCREvent evt, MCRObject obj) {
        updateDeDupCriteria(obj);
    }

    @Override
    protected void handleObjectUpdated(MCREvent evt, MCRObject obj) {
        updateDeDupCriteria(obj);
    }

    @Override
    protected void handleObjectRepaired(MCREvent evt, MCRObject obj) {
        updateDeDupCriteria(obj);
    }

    @Override
    protected void handleObjectDeleted(MCREvent evt, MCRObject obj) {
        LOGGER.info("Clearing deduplication keys and no duplicates for object {}", obj.getId());
        DeduplicationKeyManager.obtainInstance().clearDeduplicationKeys(obj.getId().toString());
        DeduplicationKeyManager.obtainInstance().clearNoDuplicates(obj.getId().toString());
    }

    private void updateDeDupCriteria(MCRObject obj) {
        LOGGER.info("Updating deduplication keys for object {}", obj.getId());
        Element mods = new MCRMODSWrapper(obj).getMODS();
        new DeDupCriteriaBuilder().updateDeDupCriteria(mods, obj.getId());
    }
}
