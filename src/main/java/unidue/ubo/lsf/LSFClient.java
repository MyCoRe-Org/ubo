/**
 * $Revision$ 
 * $Date$
 *
 * This file is part of the MILESS repository software.
 * Copyright (C) 2011 MILESS/MyCoRe developer team
 * See http://duepublico.uni-duisburg-essen.de/ and http://www.mycore.de/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **/
package unidue.ubo.lsf;

import java.io.StringReader;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.rpc.ServiceException;

import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.mycore.common.MCRCache;
import org.mycore.common.MCRException;
import org.mycore.common.config.MCRConfiguration;

import unidue.ubo.lsf.SOAPSearch;
import unidue.ubo.lsf.SOAPSearchServiceLocator;
import unidue.ubo.dedup.HyphenNormalizer;

/**
 * Implements a Web Services client for HIS LSF.
 * Allows searching for person data and retrieving details.
 * For documentation, see http://wiki.uni-due.de/lsf/index.php/SOAP
 * 
 * @author Frank L\u00fctzenkirchen
 */
public class LSFClient {

    /** Encoding of XML Strings */
    protected final static String encoding = "ISO-8859-15";

    /** The singleton instance */
    protected static LSFClient singleton;

    /** The client instance of LSF SOAPSearch web service */
    protected SOAPSearch soapsearch;

    /** The client instance of LSF DBInterface web service */
    protected DBInterface dbinterface;

    /** The Log4J Logger */
    protected final static Logger LOGGER = Logger.getLogger(LSFClient.class);

    protected MCRCache<String, Element> cache;

    protected long maxAge;

    private LSFClient() {
        try {
            maxAge = MCRConfiguration.instance().getLong("UBO.LSFClient.Cache.MaxAge");
            int capacity = MCRConfiguration.instance().getInt("UBO.LSFClient.Cache.Capacity");
            cache = new MCRCache<String, Element>(capacity, "LSF Client");

            soapsearch = new SOAPSearchServiceLocator().getsoapsearch();
            dbinterface = new DBInterfaceServiceLocator().getdbinterface();
        } catch (ServiceException ex) {
            String msg = "Could not locate HIS LSF WebService";
            throw new MCRException(msg, ex);
        }
    }

    /** Returns the LSF client instance */
    public static synchronized LSFClient instance() {
        if (singleton == null)
            singleton = new LSFClient();
        return singleton;
    }

    /** 
     * Returns an XML element containing detailed data of the given person.
     * 
     * @param pid the HIS LSF person ID
     */
    public Element getPersonDetails(String pid) {
        long time = System.currentTimeMillis() - maxAge;
        Element person = cache.getIfUpToDate(pid, time);

        if (person == null) {
            person = new Element("Person");
            try {
                String response = dbinterface.getData("PersonDetail", pid);
                Element xml = new SAXBuilder().build(new StringReader(response)).getRootElement();
                Element p = xml.getChild("Person");
                if (p != null) {
                    person = p;
                    cache.put(pid, person);
                }
            } catch (Exception ex) {
                LOGGER.error("Error while getting HIS LSF person details", ex);
            }
        }

        return person.clone();
    }

    /**
     * Returns an XML element containing a list of all person data found in HIS LSF for the given name.
     * The method searches in the field personal.nachname.
     * 
     * @param lastName The last name (or part of it) of the person to search for.
     * @param firstName The first name of the person to search for.
     */
    public Element searchPerson(String lastName, String firstName) {
        long time = System.currentTimeMillis() - maxAge;
        Element results = cache.getIfUpToDate(lastName, time);

        if (results == null) {
            results = new Element("results");

            Map<String, Element> found = new HashMap<String, Element>();
            lookup(lastName, found);

            String variantName = lastName.replace("ue", "\u00fc").replace("oe", "\u00f6").replace("ae", "\u00e4")
                .replace("ss", "\u00df");
            if (!variantName.equals(lastName))
                lookup(variantName, found);

            variantName = Normalizer.normalize(lastName, Form.NFD).replaceAll("\\p{M}", "");
            if (!variantName.equals(lastName))
                lookup(variantName, found);

            results.addContent(found.values());
            cache.put(lastName, results);

            LOGGER
                .info("LSF search for person with name = '" + lastName + "': " + results.getContentSize() + " found.");
        }

        results = results.clone();

        removeThoseWithNonMatchingFirstNames(firstName, results);

        return results;
    }

    private void lookup(String lastName, Map<String, Element> results) {
        String request = buildRequest(lastName);

        try {
            String response = soapsearch.search(request);
            LOGGER.debug("Response: " + response);

            Element xml = new SAXBuilder().build(new StringReader(response)).getRootElement();
            Element list = xml.getChild("success").getChild("list");

            for (Element object : list.getChildren("object")) {
                Element person = new Element("person");

                List<Element> attributes = object.getChildren("attribute");
                for (Element attribute : attributes) {
                    String name = attribute.getAttributeValue("name");
                    String value = attribute.getAttributeValue("value");
                    if (value == null || value.trim().length() == 0)
                        continue;

                    person.addContent(new Element(name.toLowerCase()).setText(value.trim()));
                }

                if (person.getContentSize() > 0) {
                    String id = person.getChildTextTrim("id");
                    if (!results.containsKey(id))
                        results.put(id, person);
                }
            }
        } catch (Exception ex) {
            LOGGER.error("Error while searching HIS LSF for person name", ex);
        }
    }

    private void removeThoseWithNonMatchingFirstNames(String firstName, Element results) {
        String[] firstNameParts = getNormalizedNameParts(firstName);

        for (Iterator<Element> iterator = results.getChildren().iterator(); iterator.hasNext();) {
            Element person = iterator.next();
            String vorname = person.getChildTextTrim("vorname");
            if (vorname == null)
                continue;

            String[] candidateNameParts = getNormalizedNameParts(vorname);
            if (!matches(firstNameParts, candidateNameParts))
                iterator.remove();
        }
    }

    private boolean matches(String[] firstNameParts, String[] candidateNameParts) {
        for (String a : firstNameParts)
            for (String b : candidateNameParts)
                if (b.startsWith(a))
                    return true;

        return false;
    }

    private String[] getNormalizedNameParts(String text) {
        text = text.toLowerCase();
        text = new HyphenNormalizer().normalize(text).replace("-", " ");
        text = Normalizer.normalize(text, Form.NFD).replaceAll("\\p{M}", ""); // canonical decomposition, then remove accents
        text = text.replace("ue", "u").replace("oe", "o").replace("ae", "a").replace("ß", "s").replace("ss", "s");
        text = text.replaceAll("[^a-z0-9]\\s]", ""); //remove all non-alphabetic characters
        // text = text.replaceAll("\\b.{1,3}\\b", " ").trim(); // remove all words with fewer than four characters
        text = text.replaceAll("\\p{Punct}", " ").trim(); // remove all punctuation
        text = text.replaceAll("\\s+", " "); // normalize whitespace
        return text.split("\\s");
    }

    private String buildRequest(String lastName) {
        Element xml = new Element("search");
        xml.addContent(new Element("object").setText("person"));
        Element expression = new Element("expression");
        xml.addContent(expression);

        Element column = new Element("column");
        expression.addContent(column);
        column.setAttribute("name", "personal.nachname");
        column.setAttribute("value", lastName);

        XMLOutputter xout = new XMLOutputter();
        xout.setFormat(Format.getCompactFormat().setEncoding(encoding));
        String request = xout.outputString(new Document(xml));

        LOGGER.debug("Request: " + request);
        return request;
    }

    /**
     * A small test application searching for a person and showing person details.
     */
    public static void main(String[] args) throws Exception {
        XMLOutputter xout = new XMLOutputter();
        xout.setFormat(Format.getPrettyFormat().setEncoding(encoding));

        Element results = LSFClient.instance().searchPerson("L\u00fctzenkirchen", "Frank");
        xout.output(results, System.out);

        Element person = LSFClient.instance().getPersonDetails("11775");
        xout.output(person, System.out);
    }
}
