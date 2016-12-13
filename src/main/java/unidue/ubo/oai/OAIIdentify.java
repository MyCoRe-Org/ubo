/**
 * Copyright (c) 2016 Duisburg-Essen University Library
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package unidue.ubo.oai;

import java.util.Date;
import java.util.List;

import org.mycore.oai.MCROAIIdentify;
import org.mycore.oai.MCROAIUtils;
import org.mycore.oai.pmh.DateUtils;
import org.mycore.parsers.bool.MCRCondition;
import org.mycore.services.fieldquery.MCRHit;
import org.mycore.services.fieldquery.MCRQuery;
import org.mycore.services.fieldquery.MCRQueryManager;
import org.mycore.services.fieldquery.MCRResults;
import org.mycore.services.fieldquery.MCRSortBy;

public class OAIIdentify extends MCROAIIdentify {

    public OAIIdentify(String baseURL, String configPrefix) {
        super(baseURL, configPrefix);
    }

    private final static String defaultTimestamp = "1970-01-01";

    @Override
    protected Date calculateEarliestTimestamp() {
        try {
            MCRCondition condition = MCROAIUtils.getDefaultRestrictionCondition(getConfigPrefix());
            List<MCRSortBy> sortByList = MCROAIUtils.getSortByList(getConfigPrefix() + "EarliestDatestamp.SortBy", null);
            MCRQuery query = new MCRQuery(condition, sortByList, 1);
            MCRResults result = MCRQueryManager.search(query);
            if (result.getNumHits() > 0) {
                MCRHit hit = result.getHit(0);
                String value = hit.getSortData().get(0).getValue();
                return new Date(Long.parseLong(value));
            }
        } catch (Exception ex) {
            LOGGER.warn("Error occured while examining modified date of first modified object. Using default value.", ex);
        }
        return DateUtils.parseUTC(defaultTimestamp);
    }
}