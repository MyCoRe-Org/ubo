package org.mycore.ubo.importer;

import java.util.Date;

public class ListImportIdprovider implements ImportIdProvider {

    @Override
    public String getImportId() {
        return DEFAULT_DATE_FORMAT.format(new Date());
    }
}
