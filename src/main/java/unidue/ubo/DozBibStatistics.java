/**
 * Copyright (c) 2016 Duisburg-Essen University Library
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package unidue.ubo;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.common.MCRConstants;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.xml.MCRURIResolver;
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryDAO;
import org.mycore.datamodel.classifications2.MCRCategoryDAOFactory;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.classifications2.MCRLabel;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.mods.MCRMODSWrapper;

/**
 * Collections statistics on bibliography entries and writes them to a file statistics.xml within the web application.
 * That file is rendered to HTML by ubostatistics.xsl.
 * 
 * @author Frank L\u00FCtzenkirchen
 */
public class DozBibStatistics {

    private static final Logger LOGGER = LogManager.getLogger(DozBibStatistics.class);

    private static final String INTEGER_PATTERN = "[0-9]+";

    private static int THIS_YEAR = Calendar.getInstance().get(Calendar.YEAR);
    private static int MAX_PUB_AGE_IN_YEARS = 5;
    private static int MIN_PUB_YEAR = THIS_YEAR - MAX_PUB_AGE_IN_YEARS;

    private static MCRCategoryDAO DAO;

    private static enum ChartTemplateName {
        Piechart, // http://www.highcharts.com/demo/pie-basic
        ColumnRotatedLabels, // http://www.highcharts.com/demo/column-rotated-labels
        SingleTimeSeries, // http://www.highcharts.com/demo/line-basic/grid,
        PublicationsByYear, TopList, // custom statistics, displayed via ubostatistics.xsl
        Matrix // custom statistics, displayed via ubostatistics.xsl
    }

    public static void collectStatistics(String base) throws Exception {
        DAO = MCRCategoryDAOFactory.getInstance();

        Table publicationsByType = new Table("Publikationen nach Typ", ChartTemplateName.Piechart,
            Row.COMPARE_BY_NUM_DESC);
        Table publicationsByYear = new Table("Publikationen nach Jahr", ChartTemplateName.PublicationsByYear,
            Row.COMPARE_BY_NUM_LABEL_DESC);
        Table publicationsByField = new Table("Publikationen nach Fachgebiet", ChartTemplateName.Piechart,
            Row.COMPARE_BY_NUM_DESC);
        Table publicationsByPID = new Table("Am h\u00E4ufigsten verzeichnete AutorInnen (Publikationsjahr >= " + MIN_PUB_YEAR + ")", ChartTemplateName.TopList,
            Row.COMPARE_BY_NUM_DESC);
        Table identifiers = new Table("In Publikationen verwendete Autoren-Identifikatoren", ChartTemplateName.Matrix,
            Row.COMPARE_BY_LABEL);
        int numPublications = 0;

        preparePublicationsByField(publicationsByField);

        LOGGER.info("Collecting statistics...");

        for (String id : MCRXMLMetadataManager.instance().listIDsOfType("mods")) {
            numPublications++;

            try {
                MCRObject obj = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(id));
                Element mods = new MCRMODSWrapper(obj).getMODS();
                countPublicationType(publicationsByType, mods);
                countPublicationYear(publicationsByYear, mods);
                countPublicationField(publicationsByField, mods);
                countNameIdentifiers(identifiers, mods);
                countPublicationsByLSFPID(publicationsByPID, mods);
            } catch (Exception e) {
                LOGGER.warn("Exception while processing entry #" + id, e);
            }
        }

        Element statistics = new Element("ubostatistics");
        statistics.setAttribute("total", String.valueOf(numPublications));
        statistics.setAttribute("minYear",String.valueOf(MIN_PUB_YEAR));
        statistics.addContent(publicationsByYear.toXML());
        statistics.addContent(publicationsByField.toXML());
        statistics.addContent(publicationsByType.toXML());
        statistics.addContent(identifiers.toXML());
        statistics.addContent(publicationsByPID.toXML());

        base = fixBaseDirectory(base);
        writeStatisticsFile(base, statistics);
    }

    private static void writeStatisticsFile(String base, Element statistics) throws IOException, URISyntaxException {
        String uri = base + "statistics.xml";
        LOGGER.info("Writing statistics to " + uri + " ...");
        new MCRJDOMContent(statistics).sendTo(new File(new URI(uri)));
    }

    private static void preparePublicationsByField(Table publicationsByField) {
        String uri = "resource:fachreferate.xml";
        Element fachreferate = MCRURIResolver.instance().resolve(uri);
        for (Element item : (List<Element>) (fachreferate.getChildren("item"))) {
            // bsp: <item value="sonst" label="Allgemeines, Sonstiges">
            String key = item.getAttributeValue("value");
            String label = item.getAttributeValue("label");
            publicationsByField.addRow(key, label);
        }
    }

    private static String fixBaseDirectory(String base) {
        base = base.replace(File.separatorChar, '/');
        if (!base.startsWith("/"))
            base = "/" + base;
        base += "/";
        base = "file://" + base.replace("//", "/");
        return base;
    }

    private static void countNameIdentifiers(Table identifiers, Element root) {
        for (Element name : getNodes(root, "//mods:name")) {
            for (Element nameIdentifier1 : getNodes(name, "mods:nameIdentifier")) {
                String type1 = nameIdentifier1.getAttributeValue("type");
                for (Element nameIdentifier2 : getNodes(name, "mods:nameIdentifier")) {
                    String type2 = nameIdentifier2.getAttributeValue("type");
                    {
                        String key = (type1 + "2" + type2).toUpperCase();
                        identifiers.increaseRowValueforKey(key, key);
                    }
                }
            }
        }
    }

    
    private static void countPublicationsByLSFPID(Table publicationsByPID, Element root) {
        if( getPublicationYear(root) < MIN_PUB_YEAR ) return;
        
        Set<String> occurringPIDs = new HashSet<String>(); // Count each PID only once per publication
        for (Element name : getNodes(root, "//mods:name")) {
            for (Element nameIdentifier : getNodes(name, "mods:nameIdentifier[@type='lsf']")) {
                String pid = nameIdentifier.getTextTrim();
                String completeName = getCompleteName(name);

                if (!(pid.isEmpty() || occurringPIDs.contains(pid))) {
                    occurringPIDs.add(pid);
                    publicationsByPID.increaseRowValueforKey(pid, completeName);
                }
            }
        }
    }

    private static String getCompleteName(Element name) {
        List<Element> familyNames = getNodes(name, "mods:namePart[@type='family']");
        String familyName = familyNames.isEmpty() ? "" : familyNames.get(0).getTextTrim();
        List<Element> givenNames = getNodes(name, "mods:namePart[@type='given']");
        String givenName = givenNames.isEmpty() ? "" : givenNames.get(0).getTextTrim();
        return familyName + ", " + givenName;
    }

    private static void countPublicationField(Table publicationsByField, Element root) {
        for (Element classification : getNodes(root,
            "//mods:mods/mods:classification[contains(@authorityURI,'fachreferate')]")) {
            String subjectID = classification.getAttributeValue("valueURI").split("#")[1];
            publicationsByField.increaseRowValueforKey(subjectID, null);
        }
    }

    private static void countPublicationType(Table publicationsByType, Element root) {
        String type = getNodes(root, "//mods:mods/mods:genre[@type='intern']").get(0).getTextTrim();
        String label = getGenreLabel(type);
        publicationsByType.increaseRowValueforKey(type, label);
    }

    private static String getGenreLabel(String genreID) {
        MCRCategoryID originID = new MCRCategoryID("ubogenre", genreID);
        MCRCategory category = DAO.getCategory(originID, 0);
        Optional<MCRLabel> label = category.getLabel("de");
        return label.get().getText();
    }

    private static void countPublicationYear(Table publicationsByYear, Element root) {
        int year = getPublicationYear(root);
        if (year > 1950) {
            String yearText = String.valueOf(year);
            publicationsByYear.increaseRowValueforKey(yearText, yearText);
        }
    }

    private static int getPublicationYear(Element root) {
        List<Element> datesIssued = getNodes(root, "//mods:originInfo/mods:dateIssued");
        if (datesIssued.isEmpty())
            return 0;

        String year = datesIssued.get(0).getTextTrim();
        if ((year == null) || year.isEmpty())
            return 0;

        if (year.matches(INTEGER_PATTERN))
            return Integer.valueOf(year);
        else
            return 0;
    }

    private static List<Element> getNodes(Element context, String xPath) {
        List<Namespace> namespaces = new ArrayList<Namespace>();
        namespaces.add(MCRConstants.MODS_NAMESPACE);
        XPathExpression<Element> xPathExpr = XPathFactory.instance().compile(xPath, Filters.element(), null,
            namespaces);
        return xPathExpr.evaluate(context);
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    static class Table {

        String label;

        ChartTemplateName type;

        Comparator<Row> sortBy;

        Map<String, Row> rows = new HashMap<String, Row>();

        public Table(String label, ChartTemplateName type, Comparator<Row> sortBy) {
            this.label = label;
            this.type = type;
            this.sortBy = sortBy;
        }

        public void increaseRowValueforKey(String rowKey, String subjectLabel) {
            Row row = rows.get(rowKey);
            if (row == null)
                row = addRow(rowKey, subjectLabel);

            row.num += 1;
        }

        Row addRow(String key, String label) {
            Row row = new Row(key, label);
            rows.put(key, row);
            return row;
        }

        static int id = 1;

        Element toXML() {
            Element table = new Element("table");
            table.setAttribute("name", label);
            table.setAttribute("charttype", type.toString());
            table.setAttribute("id", Integer.toString(id++));

            Collection<Row> rows = this.rows.values();
            List<Row> list = new ArrayList<Row>(rows);
            Collections.sort(list, sortBy);

            for (Row row : list) {
                table.addContent(row.toXML());
            }

            return table;
        }
    }

    static class Row {

        int num;

        String key;

        String label;

        Row(String key, String label) {
            this.key = key;
            this.label = label;
        }

        static Comparator<Row> COMPARE_BY_NUM_DESC = new Comparator<Row>() {
            public int compare(Row a, Row b) {
                return b.num - a.num;
            }
        };

        static Comparator<Row> COMPARE_BY_LABEL = new Comparator<Row>() {
            public int compare(Row a, Row b) {
                return a.label.compareTo(b.label);
            }
        };

        static Comparator<Row> COMPARE_BY_NUM_LABEL_DESC = new Comparator<Row>() {
            public int compare(Row a, Row b) {
                return Integer.parseInt(a.label) - Integer.parseInt(b.label);
            }
        };

        Element toXML() {
            Element row = new Element("row");
            row.setAttribute("num", Integer.toString(num));
            row.setAttribute("key", key);
            row.setAttribute("label", label);
            return row;
        }
    }

}
