/**
 * Copyright (c) 2016 Duisburg-Essen University Library
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package unidue.ubo.enrichment;

import java.net.URLEncoder;
import java.text.MessageFormat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.mycore.common.xml.MCRURIResolver;

class IdentifierResolver {

    private static final Logger LOGGER = LogManager.getLogger(IdentifierResolver.class);

    private IdentifierType idType;

    private String uriPattern;

    public IdentifierResolver(IdentifierType idType, String uriPattern) {
        this.idType = idType;
        this.uriPattern = uriPattern;
    }

    public IdentifierType getType() {
        return idType;
    }

    public Element resolve(String identifier) {
        Element resolved = null;
        try {
            String uri = MessageFormat.format(uriPattern, identifier, URLEncoder.encode(identifier, "UTF-8"));
            resolved = MCRURIResolver.instance().resolve(uri);
        } catch (Exception ex) {
            LOGGER.warn("Exception resolving " + identifier + ": " + ex.getClass().getName() + " " + ex.getMessage());
        }

        // Normalize various error/not found cases:
        if (resolved == null)
            return null;
        else if (!"mods".equals(resolved.getName()))
            return null;
        else if (resolved.getChildren().isEmpty())
            return null;
        else
            return resolved;
    }
}