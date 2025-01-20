package org.mycore.ubo.modsperson;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.mycore.common.events.MCRStartupHandler;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.solr.MCRSolrClientFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Loads all modsperson data into {@link MODSPersonLookup} on startup.
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
        int offset = 0;
        int blockSize = 1000;

        final SolrQuery query = new SolrQuery("objectType:modsperson")
            .setFields("*").setRows(blockSize).setParam("wt", "json");

        try {
            int resultsFound = fetchAndProcessResults(query, offset, counter);
            if (resultsFound > blockSize) {
                for (int i = 0; i < resultsFound / blockSize; i++) {
                    offset += 1000;
                    fetchAndProcessResults(query, offset, counter);
                }
            }
        } catch (SolrServerException | HttpSolrClient.RemoteSolrException | IOException e) {
            LOGGER.error(e.getMessage());
        }
        LOGGER.info("Loaded " + counter.get() + " modsperson-objects into the cache");
    }

    private int fetchAndProcessResults(SolrQuery query, int offset, AtomicInteger counter)
        throws SolrServerException, IOException {
        query.setStart(offset);

        QueryResponse queryResponse = MCRSolrClientFactory.getMainSolrClient().query(query);
        SolrDocumentList results = queryResponse.getResults();

        for (SolrDocument doc : results) {
            addSolrDocToLookup(doc, counter);
        }

        return (int) results.getNumFound();
    }

    private void addSolrDocToLookup(SolrDocument doc, AtomicInteger counter) {
        try {
            MCRObjectID personmodsId = MCRObjectID.getInstance((String) doc.getFieldValue("id"));
            String familyName;
            String givenName;
            Set<String> keys = new HashSet<>();
            String personName = (String) doc.getFieldValue("name");
            if (personName != null && personName.contains(",")) {
                String[] parts = personName.split(",");
                familyName = parts[0].trim();
                givenName = parts[1].trim();
            }
            else {
                return;
            }
            for (String fieldName : doc.getFieldNames()) {
                if (fieldName.startsWith("nid_") && !fieldName.equals("nid_connection")) {
                    List<String> fieldValues = (List<String>) doc.getFieldValue(fieldName);
                    for (String fieldValue : fieldValues) {
                        String idType = fieldName.substring(4);
                        keys.add(String.join("|", idType.trim(), fieldValue.trim()));
                    }

                }
            }

            List<String> alternativeNamesString = (List<String>) doc.getFieldValue("alternative_name");
            if (alternativeNamesString != null) {
                Set<Map.Entry<String, String>> alternativeNames = alternativeNamesString.stream()
                    .map(name -> {
                        String[] parts = name.split(",\\s*");
                        return parts.length > 1 ? Map.entry(parts[0], parts[1]) : Map.entry(parts[0], "");
                    }).collect(Collectors.toSet());
                MODSPersonLookup.add(new MODSPersonLookup.PersonCache(personmodsId, familyName,
                    givenName, keys, alternativeNames));
            }
            else {
                MODSPersonLookup.add(new MODSPersonLookup.PersonCache(personmodsId, familyName,
                    givenName, keys, new HashSet<>()));
            }
            counter.incrementAndGet();

        } catch (ClassCastException | NullPointerException | IndexOutOfBoundsException ex) {
            // parsing a single faulty SolrDocument shouldn't interrupt the whole startup
            LOGGER.warn("Error while loading modsperson " + doc.get("id") + " into cache", ex);
        }
    }

}
