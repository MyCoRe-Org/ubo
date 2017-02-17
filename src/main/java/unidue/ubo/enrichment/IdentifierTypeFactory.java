/**
 * Copyright (c) 2016 Duisburg-Essen University Library
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package unidue.ubo.enrichment;

import java.text.MessageFormat;

import org.mycore.common.MCRCache;
import org.mycore.common.config.MCRConfiguration;

class IdentifierTypeFactory {

    private static String DEFAULT_XPATH = "mods:identifier[@type=\"{0}\"]";

    private static IdentifierTypeFactory INSTANCE = new IdentifierTypeFactory();

    public static IdentifierTypeFactory instance() {
        return INSTANCE;
    }

    private MCRCache<String, IdentifierType> id2type = new MCRCache<String, IdentifierType>(30, "identifier types");

    private IdentifierTypeFactory() {
    }

    private IdentifierType buildIdentifierType(String typeID) {
        MCRConfiguration config = MCRConfiguration.instance();
        String defaultXPath = MessageFormat.format(DEFAULT_XPATH, typeID);
        String xPath = config.getString("UBO.EnrichmentResolver.IdentifierType." + typeID, defaultXPath);
        return new IdentifierType(typeID, xPath);
    }

    public IdentifierType getType(String typeID) {
        IdentifierType type = id2type.get(typeID);
        if (type == null) {
            type = buildIdentifierType(typeID);
            id2type.put(typeID, type);
        }
        return type;
    }
}