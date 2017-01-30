/**
 * Copyright (c) 2016 Duisburg-Essen University Library
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package unidue.ubo.basket;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.mycore.common.xml.MCRURIResolver;
import org.mycore.frontend.basket.MCRBasket;
import org.mycore.frontend.basket.MCRBasketEntry;
import org.mycore.frontend.basket.MCRBasketManager;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;
import org.mycore.services.fieldquery.MCRCachedQueryData;
import org.mycore.services.fieldquery.MCRResults;

/**
 * Put all hits from result list into basket
 * 
 * @author Frank L\u00FCtzenkirchen
 */
@SuppressWarnings("serial")
public class Results2Basket extends MCRServlet {

    private final static Logger LOGGER = LogManager.getLogger(Results2Basket.class);

    public void doGetPost(MCRServletJob job) throws Exception {
        HttpServletRequest req = job.getRequest();
        HttpServletResponse res = job.getResponse();

        String resultsID = req.getParameter("id");
        if (resultsID != null)
            addResultsToBasket(resultsID);
        else
            addSOLRResultsToBasket(req);

        res.sendRedirect(getServletBaseURL() + "MCRBasketServlet?action=show&type=bibentries");
    }

    private void addSOLRResultsToBasket(HttpServletRequest req) {
        LOGGER.info("add SOLR results to basket...");

        String url = req.getParameter("solr") + "%XSL.Style=xml";
        Element response = MCRURIResolver.instance().resolve(url);

        for (Element result : response.getChildren("result")) {
            if ("response".equals(result.getAttributeValue("name"))) {
                for (Element doc : result.getChildren("doc")) {
                    for (Element str : doc.getChildren("str")) {
                        if ("id".equals(str.getAttributeValue("name"))) {
                            String oid = str.getText();
                            addtoBasket(oid);
                        }
                    }
                }
            }
        }
    }

    private void addResultsToBasket(String resultsID) {
        LOGGER.info("add lucene results to basket...");

        MCRCachedQueryData qd = MCRCachedQueryData.getData(resultsID);
        MCRResults results = qd.getResults();
        results.fetchAllHits();

        for (int i = 0; i < results.getNumHits(); i++) {
            String oid = results.getHit(i).getID();
            addtoBasket(oid);
        }
    }

    private void addtoBasket(String oid) {
        MCRBasket basket = MCRBasketManager.getOrCreateBasketInSession("bibentries");
        String uri = "mcrobject:" + oid;
        MCRBasketEntry entry = new MCRBasketEntry(oid, uri);
        entry.resolveContent();
        basket.add(entry);
    }
}
