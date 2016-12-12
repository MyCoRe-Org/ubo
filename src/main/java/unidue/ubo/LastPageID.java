/**
 * Copyright (c) 2016 Duisburg-Essen University Library
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
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
