package org.mycore.ubo.wos;

import java.io.InputStream;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.jdom2.transform.JDOMSource;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRStreamContent;
import org.mycore.ubo.importer.JSON2XMLTransformer;

/**
 * URI Resolver to get publication data from Web of Science.
 * 
 * To retrieve via Web of Science ID, call wos:[ID]
 * To retrieve via a user query, append query, e.g. wos:DO=10.1039/c4tb01010h
 * 
 * UBO.WebOfScience.API.BaseURL
 * UBO.WebOfScience.API.Key
 * 
 * must be set.
 * 
 * @author Frank L\u00FCtzenkirchen
 */
public class WoSResolver implements URIResolver {

    private static final String ACCEPT_TYPE = "application/json";

    private static final String API_KEY_HEADER = "X-ApiKey";

    private static final String[] PARAMS = { "databaseId", "WOS", "count", "1", "firstRecord", "1",
        "optionView", "FR" };

    private static final String USR_QUERY = "usrQuery";

    private static final String WOS_ID_PREFIX = "WOS:";

    private String apiKey;

    private WebTarget baseTarget;

    public WoSResolver() {
        String prefix = "UBO.WebOfScience.API.";
        String baseURL = MCRConfiguration2.getStringOrThrow(prefix + "BaseURL");

        this.baseTarget = ClientBuilder.newClient().target(baseURL);
        this.apiKey = MCRConfiguration2.getStringOrThrow(prefix + "Key");
    }

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        String ref = href.substring(href.indexOf(":") + 1);

        WebTarget target = baseTarget;
        if (!ref.contains("=")) {
            String id = WOS_ID_PREFIX + ref;
            target = target.path("id").path(id);
        } else {
            target = target.queryParam(USR_QUERY, ref);
        }

        for (int i = 0; i < PARAMS.length;) {
            target = target.queryParam(PARAMS[i++], PARAMS[i++]);
        }

        Response r = target.request().accept(ACCEPT_TYPE).header(API_KEY_HEADER, apiKey).get();

        if (r.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
            throw new TransformerException(r.getStatus() + " " + r.getStatusInfo().getReasonPhrase());
        }

        try {
            MCRContent json = new MCRStreamContent(r.readEntity(InputStream.class));
            MCRContent xml = new JSON2XMLTransformer().transform(json);
            return new JDOMSource(xml.asXML());
        } catch (Exception ex) {
            throw new TransformerException(ex);
        }
    }
}
