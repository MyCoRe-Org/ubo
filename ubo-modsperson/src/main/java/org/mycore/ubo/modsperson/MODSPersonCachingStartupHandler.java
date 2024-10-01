package org.mycore.ubo.modsperson;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.events.MCRStartupHandler;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.cli.MCRCommandUtils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Loads all mpdsperson data into {@link MODSPersonLookup} on startup.
 */
public class MODSPersonCachingStartupHandler implements MCRStartupHandler.AutoExecutable {

    private final static Logger LOGGER = LogManager.getLogger();

    @Override
    public String getName() {
        return "MODSPersonCachingStartupHandler";
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public void startUp(jakarta.servlet.ServletContext servletContext) {
        AtomicInteger counter = new AtomicInteger(0);
        MCRCommandUtils.getIdsForType("modsperson").forEach(idString -> {
            MCRObject obj = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(idString));
            MODSPersonLookup.add(obj);
            counter.incrementAndGet();
        });
        LOGGER.info("Loaded " + counter.get() + " modsperson-objects into the cache");
    }
}
