package unidue.ubo.importer.evaluna;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.content.MCRStreamContent;
import org.xml.sax.SAXException;

public class EvalunaConnection {

    private final static String API_URL;

    private final static String API_KEY;

    private final static String API_USER;

    private final static String API_PASSWORD;

    static {
        MCRConfiguration config = MCRConfiguration.instance();
        API_URL = config.getString("UBO.Evaluna.URL");
        API_KEY = config.getString("UBO.Evaluna.APIKey");
        API_USER = config.getString("UBO.Evaluna.UserName");
        API_PASSWORD = config.getString("UBO.Evaluna.Password");
    }

    private List<Element> requests = new ArrayList<Element>();

    public EvalunaConnection addInstitutionRequest() {
        requests.add(new Element("request").setAttribute("type", "institutions"));
        return this;
    }

    public EvalunaConnection addPublicationRequest(Element request) {
        request.setAttribute("type", "publications");
        requests.add(request);
        return this;
    }

    public MCRContent getResponse() throws HttpException, IOException, JDOMException, SAXException {
        Element root = new Element("interface").setAttribute("version", "1.0");
        root.addContent(buildAuthElement());
        addAllRequests(root);

        PostMethod connection = postRequest(root);
        InputStream response = connection.getResponseBodyAsStream();
        MCRContent result = new MCRStreamContent(response);
        result = new MCRJDOMContent( result.asXML() );
        connection.releaseConnection();
        
        return result;
    }

    private void addAllRequests(Element root) {
        for (Element request : requests) {
            String id = String.valueOf(requests.indexOf(request) + 1);
            request.setAttribute("id", id);
            root.addContent(request);
        }
    }

    private PostMethod postRequest(Element root) throws IOException, HttpException {
        HttpClient client = new HttpClient();
        PostMethod connection = new PostMethod(API_URL);
        connection.addParameter("function", "api");
        connection.addParameter("job", "request");
        connection.addParameter("request", xml2string(root));
        client.executeMethod(connection);
        return connection;
    }

    private String xml2string(Element root) {
        XMLOutputter xout = new XMLOutputter();
        xout.setFormat(Format.getPrettyFormat().setEncoding("ISO-8859-1"));
        String xml = xout.outputString(new Document(root));
        return xml;
    }

    private Element buildAuthElement() {
        Element auth = new Element("auth");
        auth.addContent(new Element("apikey").setText(API_KEY));
        auth.addContent(new Element("username").setText(API_USER));
        auth.addContent(new Element("password").setText(API_PASSWORD));
        return auth;
    }
}
