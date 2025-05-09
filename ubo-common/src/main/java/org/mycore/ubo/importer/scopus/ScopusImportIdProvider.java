package org.mycore.ubo.importer.scopus;

import org.mycore.common.MCRSessionMgr;
import org.mycore.ubo.importer.ImportIdProvider;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ScopusImportIdProvider implements ImportIdProvider {
    protected final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT);

    @Override
    public String getImportId() {
        return "SCOPUS-" + dateFormat.format(new Date(MCRSessionMgr.getCurrentSession().getLoginTime()));
    }
}
