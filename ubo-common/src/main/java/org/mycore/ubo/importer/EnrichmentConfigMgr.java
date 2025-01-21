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
     * If the value of the DataSource element is a valid enrichment config id that id is returned. Otherwise,
     * it assumed a list of enrichment sources e.g. <em>GBV Unpaywall ...</em> is provided. In that case a new
     * configuration with id <code>custom</code> is created and the returned id will be <code>custom</code>.
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

        if (dataSource.isEmpty()) {
            return null;
        }

        String dataSrcTxt = dataSource.get().getText();
        if (MCRConfiguration2.getString("MCR.MODS.EnrichmentResolver.DataSources." + dataSrcTxt).isPresent()) {
            return dataSrcTxt;
        } else {
            String property = "MCR.MODS.EnrichmentResolver.DataSources." + DEFAULT_CONFIG_ID;
            MCRConfiguration2.set(property, dataSrcTxt);
            return DEFAULT_CONFIG_ID;
        }
    }
}
