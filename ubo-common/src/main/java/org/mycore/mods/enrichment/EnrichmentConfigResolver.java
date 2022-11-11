package org.mycore.mods.enrichment;

import java.util.Map;

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

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        String selectedDefault = href.substring(href.indexOf(":") + 1);

        Element enrichmentDebugger = new Element("enrichmentDebugger");
        Element enrichers = new Element("enrichers").setAttribute("selected", selectedDefault);
        enrichmentDebugger.addContent(enrichers);

        Map<String, String> config = MCRConfiguration2.getSubPropertiesMap("MCR.MODS.EnrichmentResolver.DataSources.");
        config.entrySet().forEach(cfgLine -> enrichers.addContent(new Element("enricher")
            .setAttribute("id", cfgLine.getKey()).setText(cfgLine.getValue())));

        return new JDOMSource(enrichmentDebugger);
    }
}
