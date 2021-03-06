/**
 * Copyright (c) 2016 Duisburg-Essen University Library
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package org.mycore.ubo;

import org.mycore.common.MCRSessionMgr;
import org.mycore.common.config.MCRConfiguration2;

public class AccessControl {

    public static boolean currentUserIsAdmin() {
        return MCRSessionMgr.getCurrentSession().getUserInformation().isUserInRole("admin");
    }

    public static boolean systemInReadOnlyMode() {
        return MCRConfiguration2.getBoolean("UBO.System.ReadOnly").orElse(false);
    }
}
