package unidue.ubo.importer.scopus;

import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.stream.Collectors;

public class ScopusUpdater {

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
            .map(a -> String.format("AF-ID(%1s)", a.trim()))
            .collect(Collectors.joining(" OR ", "(", ")"));
    }

    private static String buildDateCondition(int daysOffset) {
        GregorianCalendar day = new GregorianCalendar();
        day.add(Calendar.DATE, -daysOffset);
        return String.format("orig-load-date aft %1$tY%1$tm%1$td", day);
    }
}
