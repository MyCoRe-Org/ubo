/**
 * Copyright (c) 2016 Duisburg-Essen University Library
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package unidue.ubo.enrichment;

import org.mycore.common.MCRCache;
import org.mycore.common.config.MCRConfiguration;

class DataSourceFactory {

    private static DataSourceFactory INSTANCE = new DataSourceFactory();

    public static DataSourceFactory instance() {
        return INSTANCE;
    }

    private MCRCache<String, DataSource> dataSources = new MCRCache<String, DataSource>(30, "data sources");

    private DataSourceFactory() {
    }

    private DataSource buildDataSource(String sourceID) {
        MCRConfiguration config = MCRConfiguration.instance();
        DataSource dataSource = new DataSource(sourceID);

        String[] identifierTypes = config.getString("UBO.EnrichmentResolver.DataSource." + sourceID + ".IdentifierTypes")
            .split("\\s");
        for (String typeID : identifierTypes) {
            String prefix = "UBO.EnrichmentResolver.DataSource." + sourceID + "." + typeID + ".";
            String uri = config.getString(prefix + "URI");

            IdentifierType idType = IdentifierTypeFactory.instance().getType(typeID);
            IdentifierResolver resolver = new IdentifierResolver(idType, uri);
            dataSource.addResolver(resolver);
        }
        return dataSource;
    }

    public DataSource getDataSource(String sourceID) {
        DataSource dataSource = dataSources.get(sourceID);
        if (dataSource == null) {
            dataSource = buildDataSource(sourceID);
            dataSources.put(sourceID, dataSource);
        }
        return dataSource;
    }
}