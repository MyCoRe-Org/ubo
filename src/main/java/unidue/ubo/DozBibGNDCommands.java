/**
 * $Revision: 27403 $ 
 * $Date: 2013-07-12 12:39:15 +0200 (Fr, 12 Jul 2013) $
 *
 * This file is part of the MILESS repository software.
 * Copyright (C) 2011 MILESS/MyCoRe developer team
 * See http://duepublico.uni-duisburg-essen.de/ and http://www.mycore.de/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **/

package unidue.ubo;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.common.MCRConstants;
import org.mycore.common.xml.MCRURIResolver;
import org.xml.sax.SAXException;

public class DozBibGNDCommands {

    private static final Logger LOGGER = Logger.getLogger(DozBibGNDCommands.class);

    private static final String XSERVER_BASE = "https://alephprod.ub.uni-due.de/X?op=";

    private static final String GND_PREFIX = "(DE-588)";

    private static final int MAX_DIFFERENCE_PERCENT = 10;

    private static final int MILLIS_TO_WAIT_BETWEEN_REQUESTS = 400;

    private List<Element> bibEntries = new ArrayList<Element>();

    private Map<String, String> id2id = new HashMap<String, String>();

    private Set<String> pids = new HashSet<String>();

    private Set<String> gnds = new HashSet<String>();

    private int numBooks;

    private int numISBNs;

    private int numShelfmarks;

    private int numFound;

    public static void findGNDs() throws Exception {
        new DozBibGNDCommands();
    }

    private DozBibGNDCommands() throws IOException, JDOMException, SAXException {
        readBibEntries();
        addGNDsFromAleph();
        buildIDMapping();
        addMissingIDs("pid", "gnd");
        addMissingIDs("gnd", "pid");
        showStatistics();
    }

    private void buildIDMapping() {
        for (Element bibEntry : bibEntries) {
            for (Element contributor : getNodes(bibEntry, "mods:mods/mods:name[@type='personal']")) {
                String gnd = getID(contributor, "gnd");
                if (gnd != null)
                    gnds.add(gnd);

                String pid = getID(contributor, "pid");
                if (pid != null)
                    pids.add(pid);

                if ((gnd != null) && (pid != null)) {
                    id2id.put(gnd, pid);
                    id2id.put(pid, gnd);
                }
            }
        }
    }

    private void addMissingIDs(String fromIDType, String toIDType) {
        for (Element bibEntry : bibEntries) {
            for (Element contributor : bibEntry.getChildren("contributor")) {
                String toID = getID(contributor, toIDType);
                if (toID != null)
                    continue;

                String fromID = getID(contributor, fromIDType);
                if (fromID == null)
                    continue;

                toID = id2id.get(fromID);
                contributor.addContent(new Element(toIDType).setText(toID));
            }
        }
    }

    private String getID(Element contributor, String type) {
        List<Element> ids = getNodes(contributor, "mods:nameIdentifier[@type='" + type + "']");
        if (ids.isEmpty())
            return null;
        String id = ids.get(0).getTextTrim();
        return id.isEmpty() ? null : id;
    }

    private static List<Element> getNodes(Element context, String xPath) {
        List<Namespace> namespaces = new ArrayList<Namespace>();
        namespaces.add(MCRConstants.MODS_NAMESPACE);
        XPathExpression<Element> xPathExpr = XPathFactory.instance().compile(xPath, Filters.element(), null,
            namespaces);
        return xPathExpr.evaluate(context);
    }

    private void addGNDsFromAleph() {
        for (Element bibEntry : bibEntries) {
            String type = getNodes(bibEntry, "mods:mods/mods:genre[@type='intern']").get(0).getTextTrim();
            if (!"dissertation collection proceedings book festschrift lexicon".contains(type))
                continue;

            numBooks++;

            Element alephRecord = getAlephRecord(bibEntry);
            if (alephRecord != null) {
                if (hasMoreOrLessTheSameTitle(bibEntry, alephRecord)) {
                    numFound++;
                    Map<String, String> name2gnd = getGNDs(alephRecord);
                    addGNDs(bibEntry, name2gnd);
                }
            }
        }
    }

    private boolean hasMoreOrLessTheSameTitle(Element bibEntry, Element alephRecord) {
        String titleFromAleph = getTitleFromAleph(alephRecord);
        String titleFromBibEntry = getTitleFromBibEntry(bibEntry);

        int minLength = Math.min(titleFromAleph.length(), titleFromBibEntry.length());
        int maxDistance = minLength * MAX_DIFFERENCE_PERCENT / 100;

        int distance = StringUtils.getLevenshteinDistance(normalizeTitle(titleFromAleph),
            normalizeTitle(titleFromBibEntry));
        if (distance > maxDistance) {
            String id = bibEntry.getAttributeValue("id");
            LOGGER.warn("Skipping entry " + id + ", Levenshtein Distance = " + distance);
            LOGGER.warn(titleFromBibEntry);
            LOGGER.warn(titleFromAleph);
        }
        return true; // (distance <= maxDistance);
    }

    private void readBibEntries() throws IOException, JDOMException, SAXException {
        LOGGER.info("Reading in all bibentries...");

        for (Iterator<Integer> ids = DozBibManager.instance().iterateStoredIDs(); ids.hasNext();) {
            int id = ids.next();
            Element bibEntry = DozBibManager.instance().getEntry(id).getRootElement();
            bibEntries.add(bibEntry);
        }
    }

    private void showStatistics() {
        int numEntries = bibEntries.size();
        LOGGER.info("      Anzahl Einträge : " + numEntries);
        LOGGER.info("         davon Bücher : " + numBooks + " = "
            + Math.round(((double) numBooks / (double) numEntries) * 100) + "  %");
        LOGGER.info("       davon mit ISBN : " + numISBNs + " = "
            + Math.round(((double) numISBNs / (double) numBooks) * 100) + "  %");
        LOGGER.info("    oder mit Signatur : " + numShelfmarks + " = "
            + Math.round(((double) numShelfmarks / (double) numBooks) * 100) + "  %");
        LOGGER.info("      Bücher in Aleph : " + numFound + " = "
            + Math.round(((double) numFound / (double) numBooks) * 100) + "  %");
        LOGGER.info("    verschiedene PIDs : " + pids.size());
        LOGGER.info("    verschiedene GNDs : " + gnds.size());
        LOGGER.info("  Mapping PID <-> GND : " + id2id.size() / 2);
    }

    private Element getAlephRecord(Element bibEntry) {
        Element alephRecord = null;

        String isbn = getISBN(bibEntry);
        String shelfmark = getShelfmark(bibEntry);

        if (isbn != null)
            alephRecord = getAlephRecord("ibn=" + isbn);

        if ((shelfmark != null) && (alephRecord == null))
            try {
                alephRecord = getAlephRecord("psg=" + URLEncoder.encode(shelfmark, "UTF-8"));
            } catch (UnsupportedEncodingException ex) {
            }

        return alephRecord;
    }

    private String getISBN(Element bibEntry) {
        List<Element> identifiers = getNodes(bibEntry, "mods:mods/mods:identifier[@type='isbn']");
        if (identifiers.isEmpty())
            return null;

        String isbn = identifiers.get(0).getTextTrim();
        if (isbn.isEmpty())
            return null;

        isbn = isbn.replace("-", "").replace(".", "");
        if (!((isbn.length() == 10) || (isbn.length() == 13)))
            return null;

        numISBNs++;
        return isbn;
    }

    private String getShelfmark(Element bibEntry) {
        List<Element> shelfmarks = getNodes(bibEntry, "mods:mods/mods:location/mods:shelfLocator");
        if (shelfmarks.isEmpty())
            return null;

        String shelfmark = shelfmarks.get(0).getTextTrim();
        if (shelfmark.isEmpty())
            return null;

        numShelfmarks++;
        return shelfmark;
    }

    private Element getAlephRecord(String query) {
        String opWithParameters = "find&base=edu01&request=" + query;
        String setNumber = executeAlephQuery(opWithParameters);
        return getAlephResultRecord(setNumber);
    }

    private String executeAlephQuery(String opWithParameters) {
        Element response = callXServer(opWithParameters);

        if (!"find".equals(response.getName()))
            return null;

        String noRecords = response.getChildTextTrim("no_records");
        if ((noRecords == null) || noRecords.isEmpty() || (Integer.parseInt(noRecords) == 0))
            return null;

        return response.getChildTextTrim("set_number");
    }

    private Element getAlephResultRecord(String setNumber) {
        Element response;
        response = callXServer("present&set_entry=000000001&base=edu01&set_number=" + setNumber);

        Element record = response.getChild("record");
        if (record == null)
            return null;

        Element metadata = record.getChild("metadata");
        if (metadata == null)
            return null;

        return metadata.getChild("oai_marc");
    }

    private Map<String, String> getGNDs(Element record) {
        Map<String, String> name2gnd = new HashMap<String, String>();
        for (Element varfield : record.getChildren("varfield")) {
            if (!isPersonField(varfield))
                continue;

            String name = getName(varfield);
            String gnd = getGND(varfield);

            if ((name == null) || (gnd == null))
                continue;

            name = normalizeName(name);
            LOGGER.debug(gnd + " " + name);
            name2gnd.put(name, gnd);
        }
        return name2gnd;
    }

    private String getTitleFromAleph(Element record) {
        String title = "";
        for (Element varfield : record.getChildren("varfield")) {
            String id = varfield.getAttributeValue("id");
            if ("331".equals(id) || "335".equals(id))
                for (Element subfield : varfield.getChildren("subfield"))
                    if ("a".equals(subfield.getAttributeValue("label")))
                        title += " " + subfield.getTextTrim();
        }
        return title.trim();
    }

    private String getTitleFromBibEntry(Element bibEntry) {
        String title = "";

        Element titleInfo = getNodes(bibEntry, "mods:mods/mods:titleInfo").get(0);
        for (Element part : titleInfo.getChildren()) {
            title += " " + part.getTextTrim();
        }
        return title.trim();
    }

    private Element callXServer(String opWithParameters) {
        try {
            Thread.sleep(MILLIS_TO_WAIT_BETWEEN_REQUESTS);
        } catch (InterruptedException ignored) {
        }

        String url = XSERVER_BASE + opWithParameters;
        LOGGER.debug(url);
        return MCRURIResolver.instance().resolve(url);
    }

    private boolean isPersonField(Element varfield) {
        Attribute idAttribute = varfield.getAttribute("id");
        int id = 0;
        try {
            id = idAttribute.getIntValue();
        } catch (Exception DataConversionException) {
            return false;
        }
        return (id >= 100) && (id <= 196) && ((id % 4) == 0);
    }

    private String getName(Element varfield) {
        for (Element subfield : varfield.getChildren("subfield"))
            if ("p".equals(subfield.getAttributeValue("label")))
                return subfield.getTextTrim();
        return null;
    }

    private String normalizeName(String name) {
        if (!name.contains(","))
            return name.trim();
        String lastName = name.split(",")[0].trim();
        String firstName = name.split(",")[1].trim();
        return lastName + ", " + firstName.charAt(0);
    }

    private String getGND(Element varfield) {
        for (Element subfield : varfield.getChildren("subfield"))
            if ("9".equals(subfield.getAttributeValue("label")))
                if (subfield.getTextTrim().startsWith(GND_PREFIX))
                    return subfield.getTextTrim().substring(GND_PREFIX.length());
        return null;
    }

    private void addGNDs(Element bibEntry, Map<String, String> name2gnd) {
        for (Element contributor : getNodes(bibEntry, "mods:mods/mods:name[@type='personal']")) {
            if (getID(contributor, "gnd") == null) {
                String name = getContributorName(contributor);
                String gnd = name2gnd.get(name);
                if (gnd != null) {
                    contributor.addContent(new Element("nameIdentifier", MCRConstants.MODS_NAMESPACE).setText(gnd));
                    LOGGER.info("New GND added in bibentry " + bibEntry.getAttributeValue("id") + ": " + name
                        + " has GND " + gnd);
                }
            }
        }
    }

    private String getContributorName(Element contributor) {
        String lastName = getNodes(contributor, "mods:namePart[@type='family']").get(0).getTextTrim();
        List<Element> firstNames = getNodes(contributor, "mods:namePart[@type='given']");
        if (firstNames.isEmpty())
            return lastName;

        String firstName = firstNames.get(0).getTextTrim();
        if (firstName.isEmpty())
            return lastName;
        else
            return lastName + ", " + firstName.charAt(0);
    }

    private String normalizeTitle(String title) {
        return title.toLowerCase().replaceAll("[^a-z]+", "");
    }
}
