package unidue.ubo.importer.scopus;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class ScopusUpdater {

    private static final String QUERY_PATTERN = "AF-ID(%1$s) AND orig-load-date aft %2$tY%2$tm%2$td";

    public static void update(String affiliationID, int daysOffset, int count)
        throws Exception {
        String queryString = buildQuery(affiliationID, daysOffset);
        AbstractScopusQuery query = new ScopusQuery(queryString, count);
        ScopusImporter importer = new ScopusImporter();

        List<String> scopusIDs = query.resolveIDs();

        for (String scopusID : scopusIDs) {
            importer.doImport(scopusID);
        }
        importer.sendNotification();
    }

    private static String buildQuery(String affiliationID, int daysOffset)
        throws UnsupportedEncodingException, MalformedURLException {
        GregorianCalendar day = new GregorianCalendar();
        day.add(Calendar.DATE, -daysOffset);
        return String.format(QUERY_PATTERN, affiliationID, day);
    }
}
