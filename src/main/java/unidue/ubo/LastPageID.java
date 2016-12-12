/**
 * $Revision: 23133 $ 
 * $Date: 2012-01-16 11:49:18 +0100 (Mo, 16 Jan 2012) $
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

package unidue.ubo;

import org.mycore.common.MCRSessionMgr;

/**
 * Helper class used in XSL to store the ID of the last page displayed, see layout.xsl  
 *
 * @author Frank L\u00fctzenkirchen
 * @version $Revision: 23133 $ $Date: 2012-01-16 11:49:18 +0100 (Mo, 16 Jan 2012) $
 */
public abstract class LastPageID {

    public static String getLastPageID() {
        String lastPageID = (String) (MCRSessionMgr.getCurrentSession().get("LastPageID"));
        return (lastPageID == null ? "" : lastPageID);
    }

    public static String setLastPageID(String lastPageID) {
        MCRSessionMgr.getCurrentSession().put("LastPageID", lastPageID);
        return lastPageID;
    }
}
