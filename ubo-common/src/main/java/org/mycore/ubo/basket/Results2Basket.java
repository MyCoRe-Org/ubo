/**
 * Copyright (c) 2016 Duisburg-Essen University Library
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package org.mycore.ubo.basket;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.mycore.common.xml.MCRURIResolver;
import org.mycore.frontend.basket.MCRBasket;
import org.mycore.frontend.basket.MCRBasketEntry;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;

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

        String uri = "solr:" + req.getParameter("solr");
        LOGGER.info("add SOLR results to basket:" + uri);

        Element response = MCRURIResolver.instance().resolve(uri);

        for (Element result : response.getChildren("result")) {
            if ("response".equals(result.getAttributeValue("name"))) {
                for (Element doc : result.getChildren("doc")) {
                    for (Element str : doc.getChildren("str")) {
                        if ("id".equals(str.getAttributeValue("name")) && BasketUtils.hasSpace()) {
                            String oid = str.getText();
                            addtoBasket(oid);
                        }
                    }
                }
            }
        }

        res.sendRedirect(getServletBaseURL() + "MCRBasketServlet?action=show&type=objects");
    }

    private void addtoBasket(String oid) {
        MCRBasket basket = BasketUtils.getBasket();
        String uri = "mcrobject:" + oid;
        MCRBasketEntry entry = new MCRBasketEntry(oid, uri);
        entry.resolveContent();
        basket.add(entry);
    }
}
