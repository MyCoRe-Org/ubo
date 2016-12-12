/**
 * Copyright (c) 2016 Duisburg-Essen University Library
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package unidue.ubo.importer.scopus;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.mycore.common.MCRConstants;
import org.mycore.common.MCRMailer;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.common.xml.MCRURIResolver;
import org.mycore.frontend.cli.MCRAbstractCommands;
import org.mycore.frontend.cli.annotation.MCRCommand;
import org.mycore.frontend.cli.annotation.MCRCommandGroup;
import org.mycore.services.fieldquery.MCRQuery;
import org.mycore.services.fieldquery.MCRQueryCondition;
import org.mycore.services.fieldquery.MCRQueryManager;
import org.mycore.services.fieldquery.MCRResults;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import unidue.ubo.DozBibManager;

/**
 * Reads an RSS feed from Scopus referencing new publications and imports those publications that are not stored yet. 
 * 
 * @author Frank L\u00FCtzenkirchen
 */
@MCRCommandGroup(name = "Universitätsbibliographie")
public class ScopusFeedImporter extends MCRAbstractCommands {

    private final static Logger LOGGER = Logger.getLogger(ScopusFeedImporter.class);

    private final static String API_KEY;

    private final static String API_URL;

    private final static String FEED_URL;

    private final static String importURI = "xslStyle:scopus2mods,mods2bibentry:{0}abstract/scopus_id/{1}?apikey={2}";

    private final static Pattern scopusIDFinder = Pattern.compile(".+eid%3D2-s2\\.0-(\\d+)%26.+");

    static {
        API_KEY = MCRConfiguration.instance().getString("UBO.Scopus.API.Key");
        API_URL = MCRConfiguration.instance().getString("UBO.Scopus.API.URL");
        FEED_URL = MCRConfiguration.instance().getString("UBO.Scopus.RSSFeedURL");
    }

    @MCRCommand(syntax = "ubo import scopus publications from RSS feed", help = "Reads an RSS feed from Scopus referencing new publications and imports those publications that are not stored yet.")
    public static void importPublications() throws Exception {
        LOGGER.info("Getting new publications from Scopus RSS feed...");
        Element bibentries = new Element("bibentries");
        SyndFeed feed = retrieveFeed();

        for (SyndEntry entry : feed.getEntries()) {
            String link = entry.getLink();
            Matcher m = scopusIDFinder.matcher(link);
            if (m.matches()) {
                String scopusID = m.group(1);
                try {
                    handlePublication(bibentries, scopusID);
                } catch (Exception ex) {
                    LOGGER.warn("Exception while importing publication from Scopus ID", ex);
                }
            } else
                LOGGER.warn("no Scopus ID found in link " + link);
        }
        int numPublicationsImported = bibentries.getChildren().size();

        LOGGER.info("imported " + numPublicationsImported + " publications.");
        if (numPublicationsImported > 0)
            MCRMailer.sendMail(new Document(bibentries), "scopus-rss-import-e-mail");
    }

    private static void handlePublication(Element bibentries, String scopusID) throws IOException, JDOMException {
        if (isAlreadyStored(scopusID)) {
            LOGGER.info("publication with ID " + scopusID + " already existing, will not import.");
            return;
        }

        LOGGER.info("publication with ID " + scopusID + " does not exist yet, retrieving data...");
        Element bibentry = buildEntryFromScopus(scopusID);

        if (shouldIgnore(bibentry)) {
            LOGGER.info("publication will be ignored, do not store.");
            return;

        }
        bibentry.setAttribute("status", "imported");
        DozBibManager.instance().saveEntry(new Document(bibentry));
        bibentries.addContent(bibentry.detach());
    }

    /** If mods:genre was set to "ignore" by conversion/import function, ignore this publication and do not import */
    private static boolean shouldIgnore(Element bibentry) {
        Element mods = bibentry.getChild("mods", MCRConstants.MODS_NAMESPACE);
        String genre = mods.getChildTextTrim("genre", MCRConstants.MODS_NAMESPACE);
        return "ignore".equals(genre);
    }

    private static SyndFeed retrieveFeed() throws IOException, MalformedURLException, FeedException {
        XmlReader feedReader = new XmlReader(new URL(FEED_URL));
        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = input.build(feedReader);
        return feed;
    }

    private static boolean isAlreadyStored(String scopusID) {
        MCRQueryCondition condition = new MCRQueryCondition("ubo_scopus", "=", scopusID);
        MCRQuery query = new MCRQuery(condition);
        MCRResults results = MCRQueryManager.search(query);
        results.fetchAllHits();
        return (results.getNumHits() > 0);
    }

    private static Element buildEntryFromScopus(String scopusID) {
        String uri = MessageFormat.format(importURI, API_URL, scopusID, API_KEY);
        return MCRURIResolver.instance().resolve(uri);
    }
}
