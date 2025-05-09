package org.mycore.ubo.importer;

import java.text.SimpleDateFormat;
import java.util.Locale;

public interface ImportIdProvider {
    SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT);

    String getImportId();
}
