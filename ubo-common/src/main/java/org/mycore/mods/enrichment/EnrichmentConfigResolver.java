package org.mycore.mods.enrichment;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.jdom2.Element;
import org.jdom2.transform.JDOMSource;
import org.mycore.common.config.MCRConfiguration2;

/** 
 * URI Resolver that returns the enrichment resolver configuration
 * defined in mycore.properties, as XML to be read in enrichmentDebugger.xed
 * 
 * @author Frank Lützenkirchen
 **/
public class EnrichmentConfigResolver implements URIResolver {

    private static final String CONFIG_PREFIX = "MCR.MODS.EnrichmentResolver.";

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        String selectedDefault = href.substring(href.indexOf(":") + 1);

        Element enrichmentDebugger = new Element("enrichmentDebugger");
        Element enrichers = new Element("enrichers").setAttribute("selected", selectedDefault);
        enrichmentDebugger.addContent(enrichers);

        Map<String, String> config = MCRConfiguration2.getSubPropertiesMap(CONFIG_PREFIX + "DataSources.");
        Map<String, Element> dataSourceMap = new HashMap<String, Element>();

        for (Entry<String, String> configLine : config.entrySet()) {
            String id = configLine.getKey();
            String dataSources = configLine.getValue();

            Element enricher = new Element("enricher");
            enricher.setAttribute("id", id).setText(dataSources);
            enrichers.addContent(enricher);

            for (String dataSourceID : dataSources.split("[\\s+\\(\\)]")) {
                if (!(dataSourceMap.containsKey(dataSourceID) || dataSourceID.isEmpty())) {
                    String configProperty = CONFIG_PREFIX + "DataSource." + dataSourceID + ".IdentifierTypes";
                    String identifiers = MCRConfiguration2.getStringOrThrow(configProperty);

                    Element dsXml = new Element("dataSource");
                    dsXml.setAttribute("id", dataSourceID);
                    dsXml.setText(identifiers);
                    dataSourceMap.put(dataSourceID, dsXml);
                }
            }
        }

        Element dataSources = new Element("dataSources");
        dataSourceMap.entrySet()
            .stream().sorted((ds1, ds2) -> ds1.getKey().compareTo(ds2.getKey()))
            .forEach(entry -> dataSources.addContent(entry.getValue()));
        enrichmentDebugger.addContent(dataSources);

        return new JDOMSource(enrichmentDebugger);
    }
}
