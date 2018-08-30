/**
 * Copyright (c) 2016 Duisburg-Essen University Library
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package unidue.ubo;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocumentList;
import org.jdom2.Document;
import org.jdom2.Element;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;
import org.mycore.parsers.bool.MCRAndCondition;
import org.mycore.parsers.bool.MCRCondition;
import org.mycore.parsers.bool.MCRNotCondition;
import org.mycore.parsers.bool.MCROrCondition;
import org.mycore.parsers.bool.MCRSetCondition;
import org.mycore.services.fieldquery.MCRQuery;
import org.mycore.services.fieldquery.MCRQueryCondition;
import org.mycore.services.fieldquery.MCRQueryParser;
import org.mycore.solr.MCRSolrClientFactory;
import org.mycore.solr.search.MCRSolrSearchUtils;

public class DozBibServlet extends MCRServlet {

    public final static Logger LOGGER = LogManager.getLogger(DozBibServlet.class);

    public void doGetPost(MCRServletJob job) throws Exception {
        HttpServletRequest req = job.getRequest();
        HttpServletResponse res = job.getResponse();

        MCRAndCondition cond = new MCRAndCondition();
        cond.addChild(new MCRQueryCondition("status", "=", "confirmed")); // Only find "status=confirmed" publications

        if (req.getParameter("query") != null) {
            String query = req.getParameter("query");
            cond.addChild(new MCRQueryParser().parse(query));
        } else {
            for (String name : Collections.list(req.getParameterNames())) {
                if (isConditionParameter(name)) {
                    cond.addChild(buildFieldCondition(req, name));
                }
            }
        }

        if (cond.getChildren().size() < 2) {
            job.getResponse().sendError(HttpServletResponse.SC_BAD_REQUEST, "Request contains no query.");
            return;
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("UBO search incoming: " + cond.toString());
        }

        convertLegacyFieldNames(cond);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("UBO search MCRQL: " + cond.toString());
        }

        Element query = new Element("query");
        query.setAttribute("mask", "ubo");
        query.setAttribute("numPerPage", "0");

        Element conditions = new Element("conditions");
        conditions.setAttribute("format", "xml");
        conditions.setContent(cond.toXML());
        query.addContent(conditions);
        query.addContent(buildSortBy(req));

        Document doc = new Document(query);
        MCRQuery q = MCRQuery.parseXML(doc);

        SolrQuery solrQuery = MCRSolrSearchUtils.getSolrQuery(q, doc, req);
        solrQuery.setRows(0);
        SolrClient solrClient = MCRSolrClientFactory.getMainSolrClient();
        SolrDocumentList results = solrClient.query(solrQuery).getResults();
        long numFound = results.getNumFound();

        String format = job.getRequest().getParameter("format");
        boolean export = (format != null) && ((numFound > 0) || !"pdf".equals(format));

        String numPerPage = getReqParameter(req, "numPerPage", "10");
        String maxResults = getReqParameter(req, "maxResults", "0");
        if (!"0".equals(maxResults)) {
            numPerPage = maxResults;
        } else if (export) {
            numPerPage = String.valueOf(numFound);
        }

        query.setAttribute("numPerPage", numPerPage);

        solrQuery = MCRSolrSearchUtils.getSolrQuery(q, doc, req);
        StringBuffer url = new StringBuffer(MCRServlet.getServletBaseURL());
        url.append("SolrSelectProxy");
        url.append(solrQuery.toQueryString());

        if (export) {
            url.append("&XSL.Transformer=").append(format);
            String css = job.getRequest().getParameter("css");
            if (css != null) {
                url.append("&XSL.css=").append(css);
            }
        }

        res.sendRedirect(res.encodeRedirectURL(url.toString()));
    }

    private MCRCondition buildFieldCondition(HttpServletRequest req, String name) {
        String defaultOperator = getDefaultOperator(name);
        String operator = getReqParameter(req, name + ".operator", defaultOperator);

        String[] values = req.getParameterValues(name);
        if (values.length == 1) {
            return new MCRQueryCondition(name, operator, values[0].trim());
        } else { // Multiple fields with same name, combine with OR
            MCROrCondition oc = new MCROrCondition();
            for (String value : values) {
                oc.addChild(new MCRQueryCondition(name, operator, value.trim()));
            }
            return oc;
        }
    }

    private String getDefaultOperator(String fieldName) {
        String key = "UBO.LegacySearch.DefaultOperator." + fieldName;
        return MCRConfiguration.instance().getString(key, "=");
    }

    private boolean isConditionParameter(String name) {
        if (name.endsWith(".operator") || name.contains(".sortField") || name.startsWith("XSL.")) {
            return false;
        }
        if (Arrays.asList(new String[] { "maxResults", "numPerPage", "mode", "format", "lang", "css" }).contains(name)) {
            return false;
        }
        return true;
    }

    private Element buildSortBy(HttpServletRequest req) {
        List<String> sortFieldParameters = Collections.list(req.getParameterNames());
        sortFieldParameters.removeIf(p -> !p.contains(".sortField"));

        Element sortBy = new Element("sortBy");
        if (sortFieldParameters.isEmpty()) {
            sortBy.addContent(buildSortFieldElement("year", "descending"));
        } else {
            sortSortFieldParameters(sortFieldParameters);

            for (String parameterName : sortFieldParameters) {
                String order = getReqParameter(req, parameterName, "ascending");
                String name = getSortFieldName(parameterName);
                sortBy.addContent(buildSortFieldElement(name, order));
            }
        }

        return sortBy;
    }

    private Element buildSortFieldElement(String name, String order) {
        Element sortField = new Element("field");
        sortField.setAttribute("name", name);
        sortField.setAttribute("order", order);
        return sortField;
    }

    private String getSortFieldName(String parameterName) {
        String name = parameterName.substring(0, parameterName.indexOf(".sortField"));
        return convertLegacyFieldName("Sort", name);
    }

    private void sortSortFieldParameters(List<String> sortFieldParameters) {
        Collections.sort(sortFieldParameters, new Comparator<String>() {
            public int compare(String s0, String s1) {
                s0 = s0.substring(s0.indexOf(".sortField"));
                s1 = s1.substring(s1.indexOf(".sortField"));
                return s0.compareTo(s1);
            }
        });
    }

    private String getReqParameter(HttpServletRequest req, String name, String defaultValue) {
        String value = req.getParameter(name);
        if ((value == null) || (value.trim().length() == 0)) {
            return defaultValue;
        } else {
            return value.trim();
        }
    }

    private void convertLegacyFieldNames(MCRCondition cond) {
        if (cond instanceof MCRQueryCondition) {
            MCRQueryCondition qc = ((MCRQueryCondition) cond);
            String nameOld = qc.getFieldName();
            String nameNew = convertLegacyFieldName("Field", nameOld);
            qc.setFieldName(nameNew);
        } else if (cond instanceof MCRSetCondition) {
            MCRSetCondition sc = (MCRSetCondition) cond;
            for (MCRCondition childCond : (List<MCRCondition>) (sc.getChildren())) {
                convertLegacyFieldNames(childCond);
            }
        } else if (cond instanceof MCRNotCondition) {
            MCRNotCondition nc = (MCRNotCondition) cond;
            convertLegacyFieldNames(nc.getChild());
        }
    }

    private String convertLegacyFieldName(String type, String nameOld) {
        String nameKey = "UBO.LegacySearch." + type + "." + nameOld;
        return MCRConfiguration.instance().getString(nameKey, nameOld);
    }
}
