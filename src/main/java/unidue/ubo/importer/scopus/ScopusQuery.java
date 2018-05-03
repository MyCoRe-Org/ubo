package unidue.ubo.importer.scopus;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.common.content.MCRURLContent;
import org.xml.sax.SAXException;

class ScopusQuery {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String QUERY_PATTERN = "?query=%1$s&apikey=%2$s&httpAccept=application/xml&count=%3$s";

    private static final Namespace NS_ATOM = Namespace.getNamespace("http://www.w3.org/2005/Atom");

    private static final String EID_PREFIX = "2-s2.0-";

    private static String API_KEY;

    private static String API_URL;

    private URL queryURL;

    static {
        MCRConfiguration config = MCRConfiguration.instance();

        String prefix = "UBO.Scopus.API.";
        API_KEY = config.getString(prefix + "Key");
        API_URL = config.getString(prefix + "URL") + "search/scopus";
    }

    public ScopusQuery(String query, int count) throws UnsupportedEncodingException, MalformedURLException {
        this.queryURL = buildQueryURL(query, count);
        LOGGER.info("Querying " + queryURL);
    }

    public List<String> execute() throws JDOMException, IOException, SAXException {
        Document response = new MCRURLContent(queryURL).asXML();
        return getScopusIDs(response);
    }

    private URL buildQueryURL(String query, int count) throws UnsupportedEncodingException, MalformedURLException {
        String encodedQuery = URLEncoder.encode(query, "UTF-8");
        String queryString = String.format(QUERY_PATTERN, encodedQuery, API_KEY, count);
        return new URL(API_URL + queryString);
    }

    private List<String> getScopusIDs(Document response) {
        List<String> scopusIDs = new ArrayList<String>();
        Element root = response.getRootElement();

        for (Element entry : root.getChildren("entry", NS_ATOM)) {
            String eid = entry.getChildTextTrim("eid", NS_ATOM);
            if (eid != null && !eid.isEmpty() && eid.startsWith(EID_PREFIX)) {
                String scopusID = eid.replace(EID_PREFIX, "");
                scopusIDs.add(scopusID);
            }
        }

        return scopusIDs;
    }
}
