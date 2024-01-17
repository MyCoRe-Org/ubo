package org.mycore.ubo.dedup;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.mycore.common.events.MCREvent;
import org.mycore.common.events.MCREventHandlerBase;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.mods.MCRMODSWrapper;

public class DeDupEventHandler extends MCREventHandlerBase {

    private final static Logger LOGGER = LogManager.getLogger(DeDupEventHandler.class);

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

    private void updateDeDupCriteria(MCRObject obj) {
        if (!"mods".equals(obj.getId().getTypeId())) {
            return;
        }

        LOGGER.info("updating deduplication keys for object " + obj.getId().toString());
        Element mods = new MCRMODSWrapper(obj).getMODS();
        new DeDupCriteriaBuilder().updateDeDupCriteria(mods);
    }
}
