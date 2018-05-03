package unidue.ubo.importer.scopus;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
import org.mycore.common.config.MCRConfiguration;
import org.mycore.common.xml.MCRURIResolver;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.mods.MCRMODSWrapper;
import org.mycore.solr.MCRSolrClientFactory;
import org.mycore.solr.MCRSolrUtils;

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
        MCRConfiguration config = MCRConfiguration.instance();

        String prefix = "UBO.Scopus.Importer.";
        FIELD_TO_QUERY_ID = config.getString(prefix + "Field2QueryID");
        IMPORT_URI = config.getString(prefix + "ImportURI");
        PROJECT_ID = config.getString(prefix + "ProjectID");
        STATUS = config.getString(prefix + "Status");

        prefix += "Mail.";
        MAIL_TO = config.getString(prefix + "To");
        MAIL_PARAM = config.getString(prefix + "Param");
        MAIL_XSL = config.getString(prefix + "XSL");
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
        SolrClient solrClient = MCRSolrClientFactory.getSolrClient();
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
        String uri = MessageFormat.format(IMPORT_URI, externalID);
        return MCRURIResolver.instance().resolve(uri);
    }

    /** If mods:genre was not mapped by conversion/import function, ignore this publication and do not import */
    private static boolean shouldIgnore(Element publication) {
        return !publication.getDescendants(new ElementFilter("genre", MCRConstants.MODS_NAMESPACE)).hasNext();
    }

    private MCRObject buildMCRObject(Element publicationXML) {
        MCRObject obj = new MCRObject(new Document(publicationXML));
        MCRMODSWrapper wrapper = new MCRMODSWrapper(obj);
        wrapper.setServiceFlag("status", STATUS);
        MCRObjectID oid = MCRObjectID.getNextFreeId(PROJECT_ID, "mods");
        obj.setId(oid);
        return obj;
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
