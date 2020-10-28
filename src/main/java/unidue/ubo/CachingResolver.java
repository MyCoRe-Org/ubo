/**
 * Copyright (c) 2016 Duisburg-Essen University Library
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package unidue.ubo;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.transform.JDOMSource;
import org.mycore.common.MCRCache;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.common.xml.MCRURIResolver;

/**
 * Resolves XML content from a given URI and caches it for re-use.
 * If the URI was already resolved within the last
 * UBO.CachingResolver.MaxAge milliseconds, the cached version is returned.
 * The cache capacity is configured via UBO.CachingResolver.Capacity
 *
 * @author Frank L\u00FCtzenkirchen
 */
public class CachingResolver implements URIResolver {

    private final static Logger LOGGER = LogManager.getLogger(CachingResolver.class);

    private final static long DEFAULT_MAX_AGE = 24 * 60 * 60 * 1000;

    private long maxAge;

    private MCRCache<String, Element> cache;

    public CachingResolver() {
        int capacity = MCRConfiguration2.getInt("UBO.CachingResolver.Capacity").orElse(100);
        maxAge = MCRConfiguration2.getLong("UBO.CachingResolver.MaxAge").orElse(DEFAULT_MAX_AGE);
        cache = new MCRCache<String, Element>(capacity, "Caching Resolver");
    }

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        href = href.substring(href.indexOf(":") + 1);
        LOGGER.debug("resolving " + href);

        Element resolvedXML = cache.getIfUpToDate(href, System.currentTimeMillis() - maxAge);

        if (resolvedXML == null) {
            LOGGER.debug(href + " not in cache, must resolve");
            resolvedXML = MCRURIResolver.instance().resolve(href);
            cache.put(href, resolvedXML);
        } else
            LOGGER.debug(href + " already in cache");

        return new JDOMSource(resolvedXML);
    }
}
