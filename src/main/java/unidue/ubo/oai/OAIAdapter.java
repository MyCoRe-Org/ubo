/**
 * Copyright (c) 2016 Duisburg-Essen University Library
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package unidue.ubo.oai;

import org.mycore.common.config.MCRConfiguration;
import org.mycore.oai.MCROAIAdapter;
import org.mycore.oai.MCROAIIdentify;
import org.mycore.oai.MCROAIObjectManager;

public class OAIAdapter extends MCROAIAdapter {

    @Override
    public MCROAIObjectManager getObjectManager() {
        if (this.objectManager == null) {
            String clazz = getConfigPrefix() + "OAIObjectManager.class";
            String defaultClass = "unidue.ubo.oai.OAIObjectManager";
            this.objectManager = (MCROAIObjectManager) (MCRConfiguration.instance().getInstanceOf(clazz, defaultClass));
            String reposId = getIdentify().getIdentifierDescription().getRepositoryIdentifier();
            this.objectManager.init(getConfigPrefix(), reposId);
        }
        return this.objectManager; 
    }

    @Override
    public MCROAIIdentify getIdentify() {
        if (this.identify == null) {
            this.identify = new OAIIdentify(this.baseURL, getConfigPrefix());
        }
        return this.identify;
    }
}
