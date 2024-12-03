package org.mycore.ubo.importer;

import org.jdom2.Element;
import org.mycore.common.config.MCRConfiguration2;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

/**
 * Class handles dynamically created enrichment resolver configurations.
 *
 * @author shermann (Silvio Hermann)
 */
public class EnrichmentConfigMgr {

    private final HashMap<String, String> dynamicEnrichmentConfigIds = new HashMap<>();

    private static EnrichmentConfigMgr INSTANCE;

    private EnrichmentConfigMgr() {
    }

    /**
     * Creates and returns a singleton instance of this class.
     *
     * @return the instance
     */
    public static EnrichmentConfigMgr instance() {
        if (INSTANCE != null) {
            return INSTANCE;
        }
        INSTANCE = new EnrichmentConfigMgr();
        return INSTANCE;
    }

    /**
     * Creates and registers an enrichment configuration id.
     *
     * @param dataSources the data source
     *
     * @return the enrichment configuration id for the given data source
     * */
    public String getOrCreateEnrichmentConfig(String dataSources) {
        if (dynamicEnrichmentConfigIds.containsKey(dataSources)) {
            return dynamicEnrichmentConfigIds.get(dataSources);
        }

        String id = UUID.nameUUIDFromBytes(dataSources.getBytes(StandardCharsets.UTF_8)).toString();
        String property = "MCR.MODS.EnrichmentResolver.DataSources." + id;
        MCRConfiguration2.set(property, dataSources);
        dynamicEnrichmentConfigIds.put(dataSources, id);
        return id;
    }

    /**
     * Retrieves the first DataSource text content from the import list form element.
     *
     * @param formInput the form input (usually provided by import-list.xed)
     *
     * @return the first DataSource text or <code>null</code>
     */
    public String getDataSource(Element formInput) {
        Optional<Element> dataSource = formInput.getChildren("DataSources")
            .stream()
            .filter(element -> !element.getText().isEmpty())
            .findFirst();
        return dataSource.isPresent() ? dataSource.get().getText() : null;
    }
}
