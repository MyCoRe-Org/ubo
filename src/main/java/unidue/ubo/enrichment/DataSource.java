/**
 * Copyright (c) 2016 Duisburg-Essen University Library
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package unidue.ubo.enrichment;

import java.util.ArrayList;
import java.util.List;

class DataSource {

    private String sourceID;

    private List<IdentifierResolver> resolvers = new ArrayList<IdentifierResolver>();

    public DataSource(String sourceID) {
        this.sourceID = sourceID;
    }

    public void addResolver(IdentifierResolver resolver) {
        resolvers.add(resolver);
    }

    public List<IdentifierResolver> getResolvers() {
        return resolvers;
    }

    public String toString() {
        return "data source " + sourceID;
    }
}