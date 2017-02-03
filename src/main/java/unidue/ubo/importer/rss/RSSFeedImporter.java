/**
 * Copyright (c) 2016 Duisburg-Essen University Library
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package unidue.ubo.importer.rss;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocumentList;
import org.jdom2.Document;
import org.jdom2.Element;
import org.mycore.common.MCRMailer;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.common.xml.MCRURIResolver;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.mods.MCRMODSWrapper;
import org.mycore.solr.MCRSolrClientFactory;
import org.mycore.solr.MCRSolrUtils;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

/**
 * Reads an RSS feed referencing new publications and imports those publications that are not stored yet. 
 * 
 * @author Frank L\u00FCtzenkirchen
 */
public class RSSFeedImporter {

    private final static Logger LOGGER = LogManager.getLogger(RSSFeedImporter.class);

    public static void importFromFeed(String sourceSystemID) throws Exception {
        RSSFeedImporter importer = new RSSFeedImporter(sourceSystemID);
        importer.importPublications();
    }

    private String sourceSystemID;

    private String feedURL;

    private String importURI;

    private Pattern pattern2findID;

    private String field2queryID;

    public RSSFeedImporter(String sourceSystemID) {
        this.sourceSystemID = sourceSystemID;

        String prefix = "UBO.RSSFeedImporter." + sourceSystemID + ".";
        MCRConfiguration config = MCRConfiguration.instance();
        feedURL = config.getString(prefix + "FeedURL");
        importURI = config.getString(prefix + "PublicationURI");
        pattern2findID = Pattern.compile(config.getString(prefix + "Pattern2FindID"));
        field2queryID = config.getString(prefix + "Field2QueryID");
    }

    public void importPublications() throws Exception {
        LOGGER.info("Getting new publications from " + sourceSystemID + " RSS feed...");
        Element bibentries = new Element("bibentries");
        SyndFeed feed = retrieveFeed();

        for (SyndEntry entry : feed.getEntries()) {
            String link = entry.getLink();
            Matcher m = pattern2findID.matcher(link);
            if (m.matches()) {
                String externalID = m.group(1);
                try {
                    handlePublication(bibentries, externalID);
                } catch (Exception ex) {
                    LOGGER.warn("Exception while importing publication from " + sourceSystemID, ex);
                }
            } else
                LOGGER.warn("no publication ID found in link " + link);
        }
        int numPublicationsImported = bibentries.getChildren().size();

        LOGGER.info("imported " + numPublicationsImported + " publications.");
        if (numPublicationsImported > 0) {
            HashMap<String, String> parameters = new HashMap<String, String>();
            parameters.put("MCR.Mail.Address", MCRConfiguration.instance().getString("MCR.Mail.Address"));
            parameters.put("RSS.SourceSystem", sourceSystemID);
            MCRMailer.sendMail(new Document(bibentries), "rss-import-e-mail", parameters);
        }
    }

    private void handlePublication(Element bibentries, String externalID) throws Exception {
        if (isAlreadyStored(externalID)) {
            LOGGER.info("publication with ID " + externalID + " already existing, will not import.");
            return;
        }

        LOGGER.info("publication with ID " + externalID + " does not exist yet, retrieving data...");
        Element publication = retrieveAndConvertPublication(externalID);

        MCRObject obj = new MCRObject(new Document(publication));
        MCRMODSWrapper wrapper = new MCRMODSWrapper(obj);
        wrapper.setServiceFlag("status", "imported");

        if (shouldIgnore(wrapper)) {
            LOGGER.info("publication will be ignored, do not store.");
            return;

        }

        MCRObjectID oid = MCRObjectID.getNextFreeId("ubo_mods");
        obj.setId(oid);

        MCRMetadataManager.create(obj);
        bibentries.addContent(obj.createXML().detachRootElement());
    }

    /** If mods:genre was set to "ignore" by conversion/import function, ignore this publication and do not import */
    private static boolean shouldIgnore(MCRMODSWrapper wrapper) {
        for (Element genre : wrapper.getElements("mods:genre"))
            if (genre.getTextTrim().contains("ignore"))
                return true;
        return false;
    }

    private SyndFeed retrieveFeed() throws IOException, MalformedURLException, FeedException {
        XmlReader feedReader = new XmlReader(new URL(feedURL));
        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = input.build(feedReader);
        return feed;
    }

    private boolean isAlreadyStored(String externalID) throws SolrServerException, IOException {
        SolrClient solrClient = MCRSolrClientFactory.getSolrClient();
        SolrQuery query = new SolrQuery();
        query.setQuery(field2queryID + ":" + MCRSolrUtils.escapeSearchValue(externalID));
        query.setRows(0);
        SolrDocumentList results = solrClient.query(query).getResults();
        return (results.getNumFound() > 0);
    }

    private Element retrieveAndConvertPublication(String externalID) {
        String uri = MessageFormat.format(importURI, externalID);
        return MCRURIResolver.instance().resolve(uri);
    }
}
