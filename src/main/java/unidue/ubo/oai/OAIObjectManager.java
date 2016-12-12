/**
 * Copyright (c) 2016 Duisburg-Essen University Library
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package unidue.ubo.oai;

import org.mycore.oai.MCROAIObjectManager;

import unidue.ubo.DozBibManager;

/**
 * @author Frank L\u00FCtzenkirchen
 */
public class OAIObjectManager extends MCROAIObjectManager {

    @Override
    protected boolean exists(String oaiId) {
        String uboID = getMyCoReId(oaiId);
        int ID = 0;
        try {
            ID = Integer.parseInt(uboID);
        } catch (Exception ex) {
            return false;
        }

        if (ID < 1)
            return false;

        try {
            return exists(ID);
        } catch (Exception ex) {
            String msg = "Exception while checking existence of object " + ID;
            LOGGER.warn(msg, ex);
            return false;
        }
    }

    private boolean exists(int id) throws Exception {
        return DozBibManager.instance().exists(id);
    }

    @Override
    protected String formatURI(String uri, String uboID, String metadataPrefix) {
        if (uboID.startsWith("ubo:"))
            uboID = uboID.split(":")[1];
        return uri.replace("{id}", uboID).replace("{format}", metadataPrefix);
    }
}
