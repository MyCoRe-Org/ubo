package org.mycore.ubo.importer;

import org.jdom2.Element;
import org.mycore.common.config.MCRConfiguration2;

import java.util.Optional;

/**
 * Class retrieves the enricher id from the form input of import-list.xed.
 *
 * @author shermann (Silvio Hermann)
 */
public class EnrichmentConfigMgr {
    static final String DEFAULT_CONFIG_ID = "custom";

    private EnrichmentConfigMgr() {
    }

    /**
     * Retrieves the enricher id from the import list form element.
     *
     * @param formInput the form input (usually provided by import-list.xed)
     *
     * @return the enricher id or <code>null</code>
     */
    public static String getEnricherId(Element formInput) {
        Optional<Element> dataSource = formInput.getChildren("DataSources")
            .stream()
            .filter(element -> !element.getText().isEmpty())
            .findFirst();

        String enricherId = dataSource.isPresent() ? dataSource.get().getText() : null;
        if (enricherId != null) {
            if (MCRConfiguration2.getString("MCR.MODS.EnrichmentResolver.DataSources." + enricherId).isPresent()) {
                return enricherId;
            } else {
                String property = "MCR.MODS.EnrichmentResolver.DataSources." + DEFAULT_CONFIG_ID;
                MCRConfiguration2.set(property, dataSource.get().getText());
                return DEFAULT_CONFIG_ID;
            }
        }
        return null;
    }
}
