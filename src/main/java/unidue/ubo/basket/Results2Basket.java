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
import org.mycore.frontend.basket.MCRBasket;
import org.mycore.frontend.basket.MCRBasketEntry;
import org.mycore.frontend.basket.MCRBasketManager;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;
import org.mycore.services.fieldquery.MCRCachedQueryData;
import org.mycore.services.fieldquery.MCRResults;

/**
 * Put all hits from current result list into basket
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
        String basketID = req.getParameter("basket");
        LOGGER.info("add results " + resultsID + " to basket " + basketID);

        MCRCachedQueryData qd = MCRCachedQueryData.getData(resultsID);
        MCRBasket basket = MCRBasketManager.getOrCreateBasketInSession(basketID);
        MCRResults results = qd.getResults();
        results.fetchAllHits();

        for (int i = 0; i < results.getNumHits(); i++) {
            String oid = results.getHit(i).getID();
            String uri = "mcrobject:" + oid;
            MCRBasketEntry entry = new MCRBasketEntry(oid, uri);
            entry.resolveContent();
            basket.add(entry);
        }

        res.sendRedirect(getServletBaseURL() + "MCRBasketServlet?action=show&type=" + basketID);
    }
}
