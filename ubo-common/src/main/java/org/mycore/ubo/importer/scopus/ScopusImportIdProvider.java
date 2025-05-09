package org.mycore.ubo.importer.scopus;

import org.mycore.common.MCRSessionMgr;
import org.mycore.ubo.importer.ImportIdProvider;

import java.util.Date;

public class ScopusImportIdProvider implements ImportIdProvider {
    @Override
    public String getImportId() {
        return "SCOPUS-" + DEFAULT_DATE_FORMAT.format(new Date(MCRSessionMgr.getCurrentSession().getLoginTime()));
    }
}
