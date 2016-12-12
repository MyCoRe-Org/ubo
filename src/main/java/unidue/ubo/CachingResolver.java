/**
 * $Revision$ 
 * $Date$
 *
 * This file is part of the MILESS repository software.
 * Copyright (C) 2011 MILESS/MyCoRe developer team
 * See http://duepublico.uni-duisburg-essen.de/ and http://www.mycore.de/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **/

package unidue.ubo;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.apache.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.transform.JDOMSource;
import org.mycore.common.MCRCache;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.common.xml.MCRURIResolver;

/**
 * Resolves XML content from a given URI and caches it for re-use.
 * If the URI was already resolved within the last
 * MIL.CachingResolver.MaxAge milliseconds, the cached version is returned.
 * The cache capacity is configured via MIL.CachingResolver.Capacity
 * 
 * @author Frank L\u00FCtzenkirchen
 */
public class CachingResolver implements URIResolver {

    private final static Logger LOGGER = Logger.getLogger(CachingResolver.class);

    private final static long DEFAULT_MAX_AGE = 24 * 60 * 60 * 1000;

    private long maxAge;

    private MCRCache<String, Element> cache;

    public CachingResolver() {
        int capacity = MCRConfiguration.instance().getInt("MIL.CachingResolver.Capacity", 100);
        maxAge = MCRConfiguration.instance().getLong("MIL.CachingResolver.MaxAge", DEFAULT_MAX_AGE);
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
