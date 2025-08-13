package org.mycore.ubo.importer;

import java.util.Date;

public class ListImportIdprovider implements ImportIdProvider {
    private Date dateTimeOfInit;

    public ListImportIdprovider() {
        dateTimeOfInit = new Date();
    }

    @Override
    public String getImportId() {
        return DEFAULT_DATE_FORMAT.format(this.dateTimeOfInit);
    }
}
