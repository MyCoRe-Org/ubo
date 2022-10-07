/*
 * This file is part of ***  M y C o R e  ***
 * See http://www.mycore.de/ for details.
 *
 * MyCoRe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyCoRe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MyCoRe.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mycore.ubo.importer.scopus;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.mycore.common.content.MCRURLContent;
import org.xml.sax.SAXException;

public class PaginatedScopusQuery extends ScopusQuery {

    private static final String QUERY_PATTERN = "/search/scopus?query=%1$s&apikey=%2$s&insttoken=%3$s&httpAccept" +
        "=application/xml&count=%4$s&start=%5$s";

    private Namespace OPEN_SEARCH = Namespace.getNamespace("opensearch", "http://a9.com/-/spec/opensearch/1.1/");
    private int start;

    public PaginatedScopusQuery(String query, int count, int start) throws MalformedURLException {
        super(query, count);
        setStart(start);
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    private URL buildQueryURL() throws MalformedURLException {
        return buildQueryURL(getStart());
    }

    private URL buildQueryURL(int start) throws MalformedURLException {
        String encodedQuery = URLEncoder.encode(getQuery(), StandardCharsets.UTF_8);
        String queryString
            = String.format(Locale.ROOT, QUERY_PATTERN, encodedQuery, API_KEY, INST_TOKEN, getCount(), start);
        return new URL(API_URL + queryString);
    }

    public List<String> resolveIDs() throws JDOMException, IOException, SAXException {
        List<String> ids = new ArrayList<>();
        int totalResults, startIndex, itemsPerPage, curStart;

        curStart = getStart();
        do {
            Document response = new MCRURLContent(buildQueryURL(curStart)).asXML();
            Element root = response.getRootElement();
            totalResults = Integer.parseInt(root.getChild("totalResults", OPEN_SEARCH).getTextTrim());
            startIndex = Integer.parseInt(root.getChild("startIndex", OPEN_SEARCH).getTextTrim());
            itemsPerPage = Integer.parseInt(root.getChild("itemsPerPage", OPEN_SEARCH).getTextTrim());
            ids.addAll(getEntryScopusIDs(response));
            curStart = startIndex + itemsPerPage;
        } while (startIndex + itemsPerPage < totalResults);

        return ids;
    }

}
