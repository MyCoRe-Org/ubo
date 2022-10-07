package org.mycore.ubo.importer.scopus;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocumentList;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.ElementFilter;
import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRConstants;
import org.mycore.common.MCRException;
import org.mycore.common.MCRMailer;
import org.mycore.common.MCRPersistenceException;
import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.common.xml.MCRURIResolver;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.mods.MCRMODSWrapper;
import org.mycore.solr.MCRSolrClientFactory;
import org.mycore.solr.MCRSolrUtils;
import org.mycore.user2.MCRUser;

class ScopusImporter {

    private static final Logger LOGGER = LogManager.getLogger();

    private static String IMPORT_URI;

    private static String FIELD_TO_QUERY_ID;

    private static String PROJECT_ID;

    private static String STATUS;

    private static String MAIL_TO;

    private static String MAIL_PARAM;

    private static String MAIL_XSL;

    private List<MCRObject> importedObjects = new ArrayList<>();

    static {
        String prefix = "UBO.Scopus.Importer.";
        FIELD_TO_QUERY_ID = MCRConfiguration2.getString(prefix + "Field2QueryID").get();
        IMPORT_URI = MCRConfiguration2.getString(prefix + "ImportURI").get();
        PROJECT_ID = MCRConfiguration2.getString(prefix + "ProjectID").get();
        STATUS = MCRConfiguration2.getString(prefix + "Status").get();

        prefix += "Mail.";
        MAIL_TO = MCRConfiguration2.getString(prefix + "To").get();
        MAIL_PARAM = MCRConfiguration2.getString(prefix + "Param").get();
        MAIL_XSL = MCRConfiguration2.getString(prefix + "XSL").get();
    }

    public MCRObject doImport(String scopusID) throws MCRPersistenceException, MCRAccessException {
        if (isAlreadyStored(scopusID)) {
            LOGGER.info("publication with ID {} already existing, will not import.", scopusID);
            return null;
        }

        LOGGER.info("publication with ID {} does not exist yet, retrieving data...", scopusID);
        Element publicationXML = retrieveAndConvertPublication(scopusID);
        if (shouldIgnore(publicationXML)) {
            LOGGER.info("publication will be ignored, do not store.");
            return null;
        }

        MCRObject obj = buildMCRObject(publicationXML);
        MCRMetadataManager.create(obj);
        importedObjects.add(obj);
        return obj;
    }

    private boolean isAlreadyStored(String scopusID) {
        SolrClient solrClient = MCRSolrClientFactory.getMainSolrClient();
        SolrQuery query = new SolrQuery();
        query.setQuery(FIELD_TO_QUERY_ID + ":" + MCRSolrUtils.escapeSearchValue(scopusID));
        query.setRows(0);
        SolrDocumentList results;
        try {
            results = solrClient.query(query).getResults();
            return (results.getNumFound() > 0);
        } catch (Exception ex) {
            throw new MCRException(ex);
        }
    }

    private Element retrieveAndConvertPublication(String externalID) {
        String uri = new MessageFormat(IMPORT_URI, Locale.ROOT).format(externalID);
        return MCRURIResolver.instance().resolve(uri);
    }

    /** If mods:genre was not mapped by conversion/import function, ignore this publication and do not import */
    private static boolean shouldIgnore(Element publication) {
        return !publication.getDescendants(new ElementFilter("genre", MCRConstants.MODS_NAMESPACE)).hasNext();
    }

    private final static SimpleDateFormat ID_BUILDER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT);

    private MCRObject buildMCRObject(Element publicationXML) {
        MCRObject obj = new MCRObject(new Document(publicationXML));
        MCRMODSWrapper wrapper = new MCRMODSWrapper(obj);
        wrapper.setServiceFlag("status", STATUS);
        wrapper.setServiceFlag("importID","SCOPUS-" + getImportID());
        MCRObjectID oid = MCRObjectID.getNextFreeId(PROJECT_ID, "mods");
        obj.setId(oid);
        return obj;
    }

    private String getImportID() {
        return ID_BUILDER.format(new Date(MCRSessionMgr.getCurrentSession().getLoginTime()));
    }

    public void sendNotification() throws Exception {
        int numPublicationsImported = importedObjects.size();
        LOGGER.info("imported {} publications.", numPublicationsImported);

        if ((numPublicationsImported > 0) && (MAIL_XSL != null)) {
            Element xml = new Element(STATUS).setAttribute("source", "SCOPUS");
            for (MCRObject obj : importedObjects) {
                xml.addContent(obj.createXML().detachRootElement());
            }

            HashMap<String, String> parameters = new HashMap<>();
            parameters.put(MAIL_PARAM, MAIL_TO);
            MCRMailer.sendMail(new Document(xml), MAIL_XSL, parameters);
        }
    }
}
