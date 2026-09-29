package org.mycore.ubo.mail;

import org.jdom2.Element;
import org.jdom2.transform.JDOMSource;
import org.mycore.common.MCRException;
import org.mycore.common.xml.MCRURIResolver;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

/**
 * URI-Resolver that needs to be used directly after the {@link org.mycore.solr.common.xml.MCRSolrQueryResolver}.
 * Checks if a solr-search yields any hits and throws an exception otherwise. Format is "solr-require-results:solr:..."
 */
public class SolrRequireResultsResolver implements URIResolver {
    @Override
    public Source resolve(String href, String base) throws TransformerException {
        final String subHref = href.substring(href.indexOf(":") + 1);

        Element solrResults = MCRURIResolver.instance().resolve(subHref);
        Element result = solrResults.getChild("result");
        if (result == null) {
            throw new MCRException("There were no search results for " + subHref);
        }
        String s = result.getAttributeValue("numFound");
        if (s == null || s.isBlank() || s.equals("0")) {
            throw new MCRException("There were no search results for " + subHref);
        }
        return new JDOMSource(solrResults);
    }
}
