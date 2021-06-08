/**
 * Copyright (c) 2016 Duisburg-Essen University Library
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package org.mycore.ubo.basket;

import org.mycore.frontend.basket.MCRBasket;
import org.mycore.frontend.basket.MCRBasketManager;

/**
 * Helper class to provide XSL with current number of entries in basket
 * 
 * @author Frank L\u00FCtzenkirchen
 */
public class BasketUtils {

    private final static String basketID = "objects";

    private final static int BASKET_LIMIT_NUMENTRIES = 500;
    
    public static MCRBasket getBasket() {
        return MCRBasketManager.getOrCreateBasketInSession(basketID);
    }

    public static int size() {
        return getBasket().size();
    }
    
    public static boolean hasSpace() {
        return size() < BASKET_LIMIT_NUMENTRIES;
    }

    public static boolean contains(String entryID) {
        return MCRBasketManager.contains(basketID, entryID);
    }
}
