package org.mycore.ubo.importer.scopus;

import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.stream.Collectors;

public class ScopusUpdater {

    public static void update(String strQuery) throws Exception {
        String encodedQueryStr = strQuery;
        AbstractScopusQuery query = new PaginatedScopusQuery(encodedQueryStr, 20, 0);
        ScopusImporter importer = new ScopusImporter();

        List<String> scopusIDs = query.resolveIDs();

        for (String scopusID : scopusIDs) {
            importer.doImport(scopusID);
        }
        importer.sendNotification();
    }

    public static void update(String affiliationIDs, int daysOffset, int count)
        throws Exception {
        String affiliationCondition = buildAffiliationCondition(affiliationIDs);
        String dateCondition = buildDateCondition(daysOffset);
        String queryString = affiliationCondition + " AND " + dateCondition;

        AbstractScopusQuery query = new ScopusQuery(queryString, count);
        ScopusImporter importer = new ScopusImporter();

        List<String> scopusIDs = query.resolveIDs();

        for (String scopusID : scopusIDs) {
            importer.doImport(scopusID);
        }
        importer.sendNotification();
    }

    private static String buildAffiliationCondition(String affiliationIDs) {
        return Arrays.stream(affiliationIDs.split(","))
            .map(a -> String.format(Locale.ROOT, "AF-ID(%1s)", a.trim()))
            .collect(Collectors.joining(" OR ", "(", ")"));
    }

    private static String buildDateCondition(int daysOffset) {
        GregorianCalendar day = new GregorianCalendar(TimeZone.getDefault(), Locale.ROOT);
        day.add(Calendar.DATE, -daysOffset);
        return String.format(Locale.ROOT, "orig-load-date aft %1$tY%1$tm%1$td", day);
    }
}
