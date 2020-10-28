/**
 * Copyright (c) 2016 Duisburg-Essen University Library
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package unidue.ubo.importer.evaluna;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.mycore.common.config.MCRConfiguration2;
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
        API_URL = MCRConfiguration2.getString("UBO.Evaluna.URL").get();
        API_KEY = MCRConfiguration2.getString("UBO.Evaluna.APIKey").get();
        API_USER = MCRConfiguration2.getString("UBO.Evaluna.UserName").get();
        API_PASSWORD = MCRConfiguration2.getString("UBO.Evaluna.Password").get();
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

    public MCRContent getResponse() throws IOException, JDOMException, SAXException {
        Element root = new Element("interface").setAttribute("version", "1.0");
        root.addContent(buildAuthElement());
        addAllRequests(root);

        HttpPost post = postRequest(root);
        CloseableHttpClient client = HttpClients.createDefault();
        CloseableHttpResponse response = client.execute(post);
        HttpEntity entity = response.getEntity();

        MCRContent result = new MCRStreamContent(entity.getContent());
        result = new MCRJDOMContent(result.asXML());
        client.close();

        return result;
    }

    private void addAllRequests(Element root) {
        for (Element request : requests) {
            String id = String.valueOf(requests.indexOf(request) + 1);
            request.setAttribute("id", id);
            root.addContent(request);
        }
    }

    private HttpPost postRequest(Element root) throws IOException {
        HttpPost httpPost = new HttpPost(API_URL);
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("function", "api"));
        params.add(new BasicNameValuePair("job", "request"));
        params.add(new BasicNameValuePair("request", xml2string(root)));
        httpPost.setEntity(new UrlEncodedFormEntity(params));
        return httpPost;
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
