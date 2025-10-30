package org.mycore.ubo.modsperson;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.events.MCREvent;
import org.mycore.common.events.MCREventHandlerBase;
import org.mycore.datamodel.common.MCRLinkTableManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUserManager;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Handles updates and removals of modsperson-objects. Changes in modsperson objects need to be reflected
 * in the {@link MODSPersonLookup MODSPersonLookup-Cache}.
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

        String modspersonId = obj.getId().toString();
        Set<MCRUser> connectedUsers = MCRUserManager.getUsers("id_modsperson", modspersonId)
            .collect(Collectors.toSet());
        for (MCRUser user : connectedUsers) {
            user.getAttributes().removeIf(ua -> (ua.getName().equals("id_modsperson") && ua.getValue().equals(modspersonId)));
            MCRUserManager.updateUser(user);
        }

        MODSPersonLookup.remove(obj);
        LOGGER.info("Deleted modsperson " + obj.getId().toString() + " from cache");
    }

}

