package org.mycore.ubo.importer.scopus;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.mycore.common.content.MCRURLContent;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

public class ScopusQuery extends AbstractScopusQuery{

    private static final String QUERY_PATTERN = "/search/scopus?query=%1$s&apikey=%2$s&insttoken=%3$s&httpAccept=application/xml&count=%4$s";

    private  String query;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    private  int count;

    public ScopusQuery(String query, int count) throws MalformedURLException {
        setQuery(query);
        setCount(count);
    }

    private URL buildQueryURL() throws MalformedURLException, URISyntaxException {
        String encodedQuery = URLEncoder.encode(getQuery(), StandardCharsets.UTF_8);
        String queryString = String.format(Locale.ROOT, QUERY_PATTERN, encodedQuery, API_KEY, INST_TOKEN, getCount());
        return new URI(API_URL + queryString).toURL();
    }

    public List<String> resolveIDs() throws JDOMException, IOException, SAXException, URISyntaxException {
        Document response = new MCRURLContent(buildQueryURL()).asXML();
        return getEntryScopusIDs(response);
    }
}
