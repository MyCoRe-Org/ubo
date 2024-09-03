package org.mycore.mods.enrichment;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRException;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.cli.annotation.MCRCommand;
import org.mycore.frontend.cli.annotation.MCRCommandGroup;
import org.mycore.mods.MCRMODSWrapper;

/**
 * Enriches a MODS file by its ID and a given enrichment configuration, compare
 * <code>MCR.MODS.EnrichmentResolver.DataSources.&lt;configname&gt;</code>.
 */
@MCRCommandGroup(name = "Enrichment Commands")
public class EnrichmentCommands {

    private final static Logger LOGGER = LogManager.getLogger();

    @MCRCommand(syntax = "enrich {0} with config {1}",
        help = "Enriches existing MODS metadata {0} with a given enrichment configuration {1}", order = 40)
    public static void enrichMods(String modsId, String configID) {
        try {
            MCRObject obj = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(modsId));
            Element mods = new MCRMODSWrapper(obj).getMODS();
            MCREnricher enricher = new MCREnricher(configID);
            enricher.enrich(mods);
            MCRMetadataManager.update(obj);
        } catch (MCRException | MCRAccessException e) {
            LOGGER.error("Error while trying to enrich {} with configuration {}: ", modsId, configID, e);
        }
    }

}
