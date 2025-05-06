package org.mycore.ubo.modsperson;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRConstants;
import org.mycore.common.MCRException;
import org.mycore.common.MCRPersistenceException;
import org.mycore.common.events.MCREvent;
import org.mycore.common.events.MCREventHandlerBase;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.mods.MCRMODSWrapper;
import org.mycore.solr.MCRSolrClientFactory;

import java.io.IOException;

/**
 * Handles updates and removals of modsperson-objects. When deleting a modsperson-object, references to this object
 * need to also be deleted from mods-objects. This happens in
 * {@link MODSPersonEventHandler#removeReferencesInMods(String)}.
 * TODO: WIP
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
        try {
            int removed = removeReferencesInMods(obj.getId().toString());
            LOGGER.debug("Removed " + removed + " references in mods objects.");
        } catch (SolrServerException | IOException e) {
            throw new MCRException("There was an error while removing references for " + obj.getId().toString(), e);
        }
        LOGGER.info("Deleted modsperson " + obj.getId().toString() + " from cache");
    }

    /**
     * For a give modsperson-ID, remove references to this ID in all mods-Documents.
     * @param modspersonId ID of modsperson that was deleted
     * @return number of references in mods-documents found and deleted
     * @throws SolrServerException In case of an error inside the SOLR-Server
     * @throws IOException  If there is a low-level I/O error
     */
    private int removeReferencesInMods(String modspersonId) throws SolrServerException, IOException {
        final SolrQuery query = new SolrQuery("ref_person:" + modspersonId)
            .setFields("id").setRows(1000).setParam("wt", "json");

        QueryResponse queryResponse = MCRSolrClientFactory.getMainSolrClient().query(query);
        SolrDocumentList results = queryResponse.getResults();

        for (SolrDocument doc : results) {
            MCRObjectID modsId = MCRObjectID.getInstance((String) doc.getFieldValue("id"));
            try {
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
                LOGGER.warn("Error while trying to access object " + modsId.toString(), e);
            }
        }

        return (int) results.getNumFound();
    }

}

