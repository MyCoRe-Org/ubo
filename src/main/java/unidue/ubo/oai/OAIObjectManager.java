/**
 * $Revision$ 
 * $Date$
 *
 * This file is part of the MILESS repository software.
 * Copyright (C) 2011 MILESS/MyCoRe developer team
 * See http://duepublico.uni-duisburg-essen.de/ and http://www.mycore.de/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
