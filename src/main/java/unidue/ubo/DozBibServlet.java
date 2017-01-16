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
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.ElementFilter;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.xml.MCRURIResolver;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.basket.MCRBasket;
import org.mycore.frontend.basket.MCRBasketEntry;
import org.mycore.frontend.basket.MCRBasketManager;
import org.mycore.frontend.editor.MCREditorSubmission;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;
import org.mycore.parsers.bool.MCRAndCondition;
import org.mycore.parsers.bool.MCRCondition;
import org.mycore.parsers.bool.MCROrCondition;
import org.mycore.services.fieldquery.MCRFieldDef;
import org.mycore.services.fieldquery.MCRFieldType;
import org.mycore.services.fieldquery.MCRHit;
import org.mycore.services.fieldquery.MCRQuery;
import org.mycore.services.fieldquery.MCRQueryCondition;
import org.mycore.services.fieldquery.MCRQueryManager;
import org.mycore.services.fieldquery.MCRQueryParser;
import org.mycore.services.fieldquery.MCRResults;

public class DozBibServlet extends MCRServlet {
    
    public final static Logger LOGGER = LogManager.getLogger(DozBibServlet.class);

    public void doGetPost(MCRServletJob job) throws Exception {
        HttpServletRequest req = job.getRequest();
        HttpServletResponse res = job.getResponse();

        String mode = req.getParameter("mode");

        if ("list".equals(mode))
            showResultList(job);
        else if ("export".equals(mode))
            exportResultList(job);
        else if ("allToBasket".equals(mode))
            allToBasket(req, res);
        else
            searchForEntries(job);
    }

    private void allToBasket(HttpServletRequest req, HttpServletResponse res) throws Exception {
        String key = req.getParameter("listKey");
        if ((key == null) || (key.trim().length() == 0)) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Parameter 'key' is missing.");
            return;
        }

        LOGGER.info("UBO add results to basket " + key);
        MCRResults results = (MCRResults) (req.getSession(true).getAttribute(key));
        if (results == null) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Result list does not exist in session.");
            return;
        }

        for (int i = 0; i < results.getNumHits(); i++) {
            String oid = results.getHit(i).getID();
            String id = String.valueOf(Integer.parseInt(oid.split("_")[2]));
            String uri = "ubo:" + id;
            MCRBasketEntry entry = new MCRBasketEntry(id, uri);
            entry.resolveContent();
            MCRBasketManager.getOrCreateBasketInSession("bibentries").add(entry);
        }

        res.sendRedirect(getServletBaseURL() + "MCRBasketServlet?type=bibentries&action=show");
    }

    private MCRResults getResults(MCRServletJob job) throws Exception {
        String key = job.getRequest().getParameter("listKey");
        if ((key == null) || (key.trim().length() == 0)) {
            job.getResponse().sendError(HttpServletResponse.SC_BAD_REQUEST, "Parameter 'key' is missing.");
            return null;
        }

        MCRResults results = (MCRResults) (job.getRequest().getSession(true).getAttribute(key));
        if (results == null) {
            job.getResponse().sendError(HttpServletResponse.SC_BAD_REQUEST, "Result list does not exist in session.");
            return null;
        }

        return results;
    }

    private void exportResultList(MCRServletJob job) throws Exception {
        MCRResults results = getResults(job);
        if (results != null)
            exportResults(job, results);
    }

    private void showResultList(MCRServletJob job) throws Exception {
        MCRResults results = getResults(job);
        if (results == null)
            return;

        HttpServletRequest req = job.getRequest();
        HttpServletResponse res = job.getResponse();

        String key = job.getRequest().getParameter("listKey");

        String snpp = req.getParameter("numPerPage");
        if ((snpp == null) || (snpp.trim().length() == 0))
            snpp = "10";
        int npp = Integer.parseInt(snpp);

        String spage = req.getParameter("page");
        if ((spage == null) || (spage.trim().length() == 0))
            spage = "1";
        int page = Integer.parseInt(spage);

        Document xml = buildResultsPage(npp, page, results, key);
        getLayoutService().doLayout(req, res, new MCRJDOMContent(xml));
    }

    private void searchForEntries(MCRServletJob job) throws Exception {
        HttpServletRequest req = job.getRequest();
        HttpServletResponse res = job.getResponse();

        Document doc = (Document) req.getAttribute("MCRXEditorSubmission"); // Query as XML document
        if (req.getAttribute("MCREditorSubmission") != null) // Query from legacy search mask
            doc = ((MCREditorSubmission) (req.getAttribute("MCREditorSubmission"))).getXML();
        
        MCRCondition cond = null; // Parsed query condition

        if (doc != null) // Query from search mask
        {
            Element conditions = doc.getRootElement().getChild("conditions");

            if (conditions.getAttributeValue("format").equals("text")) {
                // Expertensuche, Suchausdruck in MCRQL
                cond = new MCRQueryParser().parse(conditions.getTextTrim());
            } else {
                // Suchmaske mit einzelnen Feldern
                prepareQuery(doc);
                cond = new MCRQueryParser().parse((Element) (conditions.getChildren().get(0)));
            }
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

        MCRResults results = MCRQueryManager.search(MCRQuery.parseXML(doc), true);
        String key = storeResultList(req.getSession(true), results);

        String npp = doc.getRootElement().getAttributeValue("numPerPage");
        if ((npp == null) || npp.equals("") || (Integer.parseInt(npp) > results.getNumHits()))
            npp = String.valueOf(results.getNumHits());

        String format = req.getParameter("format");
        if ((format == null) || (format.equals("pdf") && (results.getNumHits() == 0))) {
            String url = MCRServlet.getServletBaseURL() + "DozBibServlet?mode=list&page=1&numPerPage=" + npp + "&listKey=" + key;
            res.sendRedirect(url);
        } else
            exportResults(job, results);
    }

    private void exportResults(MCRServletJob job, MCRResults results) throws Exception, IOException, ServletException {
        String basketID = "bibresults-" + results.getID();
        MCRBasket basket = MCRBasketManager.getOrCreateBasketInSession(basketID);
        
        if (basket.size() != results.getNumHits()) {
            basket.clear();

            for (Iterator<MCRHit> hits = results.iterator(); hits.hasNext();) {
                String oid = hits.next().getID();
                String id = String.valueOf(MCRObjectID.getInstance(oid).getNumberAsInteger());
                String uri = "ubo:" + id;
                basket.add(new MCRBasketEntry(id, uri));
            }
        }

        String format = job.getRequest().getParameter("format");
        String url = MCRServlet.getServletBaseURL() + "MCRExportServlet/export." + format + "?basket=" + basketID + "&root=bibentries&transformer=" + format;
        String css = job.getRequest().getParameter("css");
        if( css != null ) url += "&XSL.css=" + css;
        
        job.getResponse().sendRedirect(url);
    }

    private String getReqParameter(HttpServletRequest req, String name, String defaultValue) {
        String value = req.getParameter(name);
        if ((value == null) || (value.trim().length() == 0))
            return defaultValue;
        else
            return value.trim();
    }

    private Document buildResultsPage(int npp, int page, MCRResults results, String key) throws Exception {
        int numHits = results.getNumHits();
        int first = npp * (page - 1);
        int last = Math.min(first + npp, numHits);
        int numPages = (int) (Math.ceil((double) numHits / (double) npp));

        Element root = new Element("bibentries");
        root.setAttribute("numHits", String.valueOf(numHits));
        root.setAttribute("page", String.valueOf(page));
        root.setAttribute("numPerPage", String.valueOf(npp));
        root.setAttribute("numPages", String.valueOf(numPages));
        root.setAttribute("listKey", key);

        for (int i = first; i < last; i++) {
            String oid = results.getHit(i).getID();
            String uri = "mcrobject:" + oid;
            Element entry = MCRURIResolver.instance().resolve(uri);
            root.addContent(entry);
        }

        return new Document(root);
    }

    private String storeResultList(HttpSession session, MCRResults results) {
        String key = Long.toString(System.currentTimeMillis(), 36);
        session.setAttribute(key, results);
        return key;
    }

    private void prepareQuery(Document query) throws Exception {
        // Rename condition elements from search mask: condition1 -> condition
        // Transform condition with multiple values into OR condition
        Element root = query.getRootElement();
        List<Element> contentToRemove = new ArrayList<Element>();
        for (Element elem : root.getDescendants(new ElementFilter())) {
            
            if ((!elem.getName().equals("conditions")) && elem.getName().startsWith("condition"))
                elem.setName("condition");
            else if (elem.getName().equals("value")) {
                Element parent = elem.getParentElement();
                parent.removeAttribute("value");
                parent.setAttribute("children", "true");
                
                elem.setName("condition");
                elem.setAttribute("field", parent.getAttributeValue("field"));
                elem.setAttribute("operator", parent.getAttributeValue("operator"));
                elem.setAttribute("value", elem.getText());
                contentToRemove.add(elem);
            }
        }
        for (Element element : contentToRemove) {
            element.removeContent();
        }

        // Find condition fields without values
        Vector<Element> help = new Vector<Element>();
        for (Element condition : root.getDescendants(new ElementFilter("condition"))) {
            if (condition.getAttribute("children") != null) {
                // Transform into OR condition
                condition.setName("boolean");
                condition.setAttribute("operator", "or");
                condition.removeAttribute("children");
            } else if (condition.getAttribute("value") == null) {
                help.add(condition);
            }
        }

        // Remove found conditions without values
        for (int i = help.size() - 1; i >= 0; i--)
            help.get(i).detach();
    }
}
