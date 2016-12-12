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

public class AccessControl {

    public static boolean currentUserIsAdmin() {
        return MCRSessionMgr.getCurrentSession().getUserInformation().isUserInRole("admin");
    }
}
