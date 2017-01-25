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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
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
        HttpServletResponse res = job.getResponse();

        Document doc = (Document) req.getAttribute("MCRXEditorSubmission"); // Query as XML document

        MCRCondition cond = null; // Parsed query condition

        if (doc != null) // Expertensuche, Suchausdruck in MCRQL
        {
            Element conditions = doc.getRootElement().getChild("conditions");
            cond = new MCRQueryParser().parse(conditions.getTextTrim());
        } else
        // Query as a link
        {
            boolean requestContainsQueryCondition = false;

            Element query = new Element("query");
            query.setAttribute("maxResults", getReqParameter(req, "maxResults", "0"));
            query.setAttribute("numPerPage", getReqParameter(req, "numPerPage", ""));
            doc = new Document(query);

            Element conditions = new Element("conditions");
            query.addContent(conditions);

            Element sortBy = new Element("sortBy");
            query.addContent(sortBy);

            List<String> sortFields = new ArrayList<String>();
            for (Enumeration<String> names = req.getParameterNames(); names.hasMoreElements();) {
                String name = names.nextElement();
                if (name.contains(".sortField"))
                    sortFields.add(name);
            }

            if (sortFields.size() > 0) {
                Collections.sort(sortFields, new Comparator<String>() {
                    public int compare(String s0, String s1) {
                        s0 = s0.substring(s0.indexOf(".sortField"));
                        s1 = s1.substring(s1.indexOf(".sortField"));
                        return s0.compareTo(s1);
                    }
                });

                for (String name : sortFields) {
                    String sOrder = getReqParameter(req, name, "ascending");
                    name = name.substring(0, name.indexOf(".sortField"));

                    // Fix legacy sort fields, field names have changed:
                    if ("ubo_title".equals(name))
                        name = "ubo_sortby_title";
                    if ("ubo_author".equals(name))
                        name = "ubo_sortby_name";

                    Element sField = new Element("field");
                    sField.setAttribute("name", name);
                    sField.setAttribute("order", sOrder);
                    sortBy.addContent(sField);
                }
            } else {
                Element sField = new Element("field");
                sField.setAttribute("name", "ubo_year");
                sField.setAttribute("order", "descending");
                sortBy.addContent(sField);
            }

            if (req.getParameter("query") != null) {
                requestContainsQueryCondition = true;
                // Search for a complex query expression
                String expr = req.getParameter("query");
                cond = new MCRQueryParser().parse(expr);
            } else {
                // Search for name-operator-value conditions given as request parameters

                conditions.setAttribute("format", "xml");
                MCRAndCondition ac = new MCRAndCondition();
                cond = ac;

                Enumeration<String> names = req.getParameterNames();
                while (names.hasMoreElements()) {
                    String name = names.nextElement();
                    if (name.endsWith(".operator") || name.contains(".sortField") || name.startsWith("XSL."))
                        continue;
                    if (" maxResults numPerPage mode format lang css ".contains(" " + name + " "))
                        continue;

                    requestContainsQueryCondition = true;

                    String fieldName = (name.startsWith("ubo_") ? name : "ubo_" + name);
                    MCRFieldDef field = MCRFieldDef.getDef(fieldName);

                    String operator = req.getParameter(name + ".operator");
                    if (operator == null)
                        operator = MCRFieldType.getDefaultOperator(field.getDataType());

                    String[] values = req.getParameterValues(name);
                    if (values.length == 1) {
                        ac.addChild(new MCRQueryCondition(fieldName, operator, values[0].trim()));
                    } else
                    // Multiple fields with same name, combine with OR
                    {
                        MCROrCondition oc = new MCROrCondition();
                        ac.addChild(oc);
                        for (int i = 0; i < values.length; i++)
                            oc.addChild(new MCRQueryCondition(fieldName, operator, values[i].trim()));
                    }
                }
            }

            if (!requestContainsQueryCondition) {
                job.getResponse().sendError(HttpServletResponse.SC_BAD_REQUEST, "Request contains no query.");
                return;
            }
        }

        // If current user is not admin, always only search for "status=confirmed" 

        if (!AccessControl.currentUserIsAdmin()) {
            MCRCondition extraCond = new MCRQueryCondition("ubo_status", "=", "confirmed");

            if (cond instanceof MCRAndCondition) {
                ((MCRAndCondition) cond).addChild(extraCond);
            } else {
                MCRAndCondition ac = new MCRAndCondition();
                if (cond != null)
                    ac.addChild(cond);
                ac.addChild(extraCond);
                cond = ac;
            }
        }

        Element conditions = doc.getRootElement().getChild("conditions");
        conditions.setAttribute("format", "xml");

        // Empty search mask, no condition at all? Then search for all entries with dummy condition:
        if (cond == null)
            cond = new MCRQueryCondition("ubo_status", "like", "*");

        conditions.setContent(cond.toXML());

        if (LOGGER.isDebugEnabled())
            LOGGER.debug("UBO search: " + cond.toString());

        doc.getRootElement().setAttribute("mask", "ubo");
        MCRQuery query = MCRQuery.parseXML(doc);
        MCRCachedQueryData qd = MCRCachedQueryData.cache(query, doc);

        MCRResults results = qd.getResults();

        redirectToResults(job, doc, results);
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