/**
 * Copyright (c) 2016 Duisburg-Essen University Library
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package unidue.ubo.enrichment;

import java.util.List;
import java.util.StringTokenizer;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.transform.JDOMSource;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.common.MCRConstants;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.common.xml.MCRURIResolver;
import org.mycore.common.xml.MCRXPathBuilder;

import unidue.ubo.merger.Merger;
import unidue.ubo.merger.MergerFactory;

public class EnrichmentResolver implements URIResolver {

    private static final Logger LOGGER = LogManager.getLogger(EnrichmentResolver.class);

    private static final XPathExpression<Element> xPath2RelatedItems = XPathFactory.instance().compile(
        "mods:relatedItem[@type='host' or @type='series']", Filters.element(), null,
        MCRConstants.getStandardNamespaces());

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        href = href.substring(href.indexOf(":") + 1);
        String configID = href.substring(0, href.indexOf(':'));

        href = href.substring(href.indexOf(":") + 1);
        Element mods = MCRURIResolver.instance().resolve(href);

        enrichPublication(mods, configID);

        return new JDOMSource(mods);
    }

    public void enrichPublication(Element mods, String configID) {
        enrichPublicationLevel(mods, configID);
        List<Element> relatedItems = xPath2RelatedItems.evaluate(mods);
        for (Element relatedItem : relatedItems)
            enrichPublicationLevel(relatedItem, configID);

        debug(mods, "complete publication");
    }

    private void enrichPublicationLevel(Element mods, String configID) {
        LOGGER.debug("resolving via config " + configID + " : " + MCRXPathBuilder.buildXPath(mods));

        boolean withinGroup = false;
        boolean dataSourceCompleted = false;
        String dsConfig = MCRConfiguration.instance().getString("UBO.EnrichmentResolver.DataSources." + configID);

        for (StringTokenizer st = new StringTokenizer(dsConfig, " ()", true); st.hasMoreTokens();) {
            String token = st.nextToken();
            if (token.equals(" "))
                continue;
            else if (token.equals("(")) {
                withinGroup = true;
                dataSourceCompleted = false;
            } else if (token.equals(")")) {
                withinGroup = false;
                dataSourceCompleted = false;
            } else if (withinGroup && dataSourceCompleted) {
                LOGGER.debug("Skipping data source " + token);
                continue;
            } else {
                String dataSourceID = token;
                DataSource dataSource = DataSourceFactory.instance().getDataSource(dataSourceID);

                dataSourceLoop: for (IdentifierResolver resolver : dataSource.getResolvers()) {
                    IdentifierType idType = resolver.getType();
                    List<Element> identifiersFound = idType.findIdentifiers(mods);
                    for (Element identifierElement : identifiersFound) {
                        String identifier = identifierElement.getTextTrim();

                        LOGGER.debug("resolving " + idType + " " + identifier + " from " + dataSource + "...");
                        Element resolved = resolver.resolve(identifier);

                        if (resolved == null) {
                            LOGGER.debug("no data returned from " + dataSource);
                        } else {
                            mergeResolvedIntoExistingData(mods, resolved);
                            dataSourceCompleted = true;
                            break dataSourceLoop;
                        }
                    }
                }
            }
        }
    }

    private void mergeResolvedIntoExistingData(Element mods, Element resolved) {
        LOGGER.debug("resolved publication data, merging into existing data...");
        debug(resolved, "resolved publication");

        if (mods.getName().equals("relatedItem")) {
            // resolved is always mods:mods, transform to mods:relatedItem to be mergeable
            resolved.setName("relatedItem");
            resolved.setAttribute(mods.getAttribute("type").clone());
        }

        Merger a = MergerFactory.buildFrom(mods);
        Merger b = MergerFactory.buildFrom(resolved);
        a.mergeFrom(b);

        MODSSorter.sort(mods);
        debug(mods, "merged publication");
    }

    private void debug(Element mods, String headline) {
        if (LOGGER.isDebugEnabled()) {
            mods.removeChildren("extension", MCRConstants.MODS_NAMESPACE);
            try {
                LOGGER.debug("\n-------------------- " + headline + ": --------------------\n");
                XMLOutputter xout = new XMLOutputter(Format.getPrettyFormat());
                LOGGER.debug(xout.outputString(mods));
                LOGGER.debug("\n");
            } catch (Exception ignored) {
            }
        }
    }
}