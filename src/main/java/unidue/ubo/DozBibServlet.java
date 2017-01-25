/**
 * Copyright (c) 2016 Duisburg-Essen University Library
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package unidue.ubo;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;
import org.mycore.parsers.bool.MCRAndCondition;
import org.mycore.parsers.bool.MCRCondition;
import org.mycore.parsers.bool.MCROrCondition;
import org.mycore.services.fieldquery.MCRCachedQueryData;
import org.mycore.services.fieldquery.MCRFieldDef;
import org.mycore.services.fieldquery.MCRFieldType;
import org.mycore.services.fieldquery.MCRQuery;
import org.mycore.services.fieldquery.MCRQueryCondition;
import org.mycore.services.fieldquery.MCRQueryParser;
import org.mycore.services.fieldquery.MCRResults;

public class DozBibServlet extends MCRServlet {

    public final static Logger LOGGER = LogManager.getLogger(DozBibServlet.class);

    public void doGetPost(MCRServletJob job) throws Exception {
        HttpServletRequest req = job.getRequest();

        MCRAndCondition cond = new MCRAndCondition();
        cond.addChild(new MCRQueryCondition("ubo_status", "=", "confirmed")); // Only find "status=confirmed" publications

        {
            if (req.getParameter("query") != null)
                cond.addChild(new MCRQueryParser().parse(req.getParameter("query")));
            else
                for (String name : Collections.list(req.getParameterNames()))
                    if (isConditionParameter(name))
                        cond.addChild(buildFieldCondition(req, name));

            if (cond.getChildren().size() < 2) {
                job.getResponse().sendError(HttpServletResponse.SC_BAD_REQUEST, "Request contains no query.");
                return;
            }
        }

        if (LOGGER.isDebugEnabled())
            LOGGER.debug("UBO search: " + cond.toString());

        Element query = new Element("query");
        query.setAttribute("mask", "ubo");
        query.setAttribute("maxResults", getReqParameter(req, "maxResults", "0"));
        query.setAttribute("numPerPage", getReqParameter(req, "numPerPage", ""));

        Element conditions = new Element("conditions");
        conditions.setAttribute("format", "xml");
        conditions.setContent(cond.toXML());
        query.addContent(conditions);
        query.addContent(buildSortBy(req));

        Document doc = new Document(query);
        MCRQuery q = MCRQuery.parseXML(doc);
        MCRCachedQueryData qd = MCRCachedQueryData.cache(q, doc);
        MCRResults results = qd.getResults();

        redirectToResults(job, doc, results);
    }

    private MCRCondition buildFieldCondition(HttpServletRequest req, String name) {
        String fieldName = (name.startsWith("ubo_") ? name : "ubo_" + name);
        MCRFieldDef field = MCRFieldDef.getDef(fieldName);

        String operator = getReqParameter(req, name + ".operator",
            MCRFieldType.getDefaultOperator(field.getDataType()));

        String[] values = req.getParameterValues(name);
        if (values.length == 1)
            return new MCRQueryCondition(fieldName, operator, values[0].trim());
        else { // Multiple fields with same name, combine with OR
            MCROrCondition oc = new MCROrCondition();
            for (String value : values)
                oc.addChild(new MCRQueryCondition(fieldName, operator, value.trim()));
            return oc;
        }
    }

    private boolean isConditionParameter(String name) {
        if (name.endsWith(".operator") || name.contains(".sortField") || name.startsWith("XSL."))
            return false;
        if (Arrays.asList(new String[] { "maxResults", "numPerPage", "mode", "format", "lang", "css" }).contains(name))
            return false;
        return true;
    }

    private Element buildSortBy(HttpServletRequest req) {
        List<String> sortFieldParameters = Collections.list(req.getParameterNames());
        sortFieldParameters.removeIf(p -> ! p.contains(".sortField"));

        Element sortBy = new Element("sortBy");
        if (!sortFieldParameters.isEmpty()) {
            sortSortFieldParameters(sortFieldParameters);

            for (String parameterName : sortFieldParameters) {
                String order = getReqParameter(req, parameterName, "ascending");
                String name = getSortFieldName(parameterName);

                sortBy.addContent(buildSortFieldElement(name, order));
            }
        } else
            sortBy.addContent(buildSortFieldElement("ubo_year", "descending"));

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

        // Fix legacy sort fields, field names have changed:
        if ("ubo_title".equals(name))
            name = "ubo_sortby_title";
        if ("ubo_author".equals(name))
            name = "ubo_sortby_name";

        return name;
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

    private void redirectToResults(MCRServletJob job, Document doc, MCRResults results) throws IOException {
        StringBuilder url = new StringBuilder(MCRServlet.getServletBaseURL());
        url.append("MCRSearchServlet?mode=results");
        url.append("&id=").append(results.getID());

        String format = job.getRequest().getParameter("format");
        if ((format == null) || (format.equals("pdf") && (results.getNumHits() == 0))) {
            url.append("&numPerPage=").append(getNumPerPage(doc, results));
        } else {
            url.append("&numPerPage=").append(results.getNumHits());
            url.append("&XSL.Transformer=").append(format);
            String css = job.getRequest().getParameter("css");
            if (css != null)
                url.append("&XSL.css=").append(css);
        }
        job.getResponse().sendRedirect(url.toString());
    }

    private String getNumPerPage(Document doc, MCRResults results) {
        String npp = doc.getRootElement().getAttributeValue("numPerPage");
        if ((npp == null) || npp.isEmpty() || (Integer.parseInt(npp) > results.getNumHits()))
            npp = String.valueOf(results.getNumHits());
        return npp;
    }

    private String getReqParameter(HttpServletRequest req, String name, String defaultValue) {
        String value = req.getParameter(name);
        if ((value == null) || (value.trim().length() == 0))
            return defaultValue;
        else
            return value.trim();
    }
}