/**
 * Copyright (c) 2016 Duisburg-Essen University Library
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package org.mycore.ubo.lsf;

import java.io.StringReader;
import java.net.URL;
import java.net.URLDecoder;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.common.MCRException;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.mods.merger.MCRHyphenNormalizer;
import org.mycore.ubo.picker.IdentityService;
import org.mycore.ubo.picker.PersonSearchResult;

/**
 * Implements a Web Services client for HIS LSF.
 * Allows searching for person data and retrieving details.
 * For documentation, see http://wiki.uni-due.de/lsf/index.php/SOAP
 *
 * @author Frank L\u00fctzenkirchen
 */
public class LSFService implements IdentityService {

    /** Encoding of XML Strings */
    protected final static String encoding = "ISO-8859-15";

    /** The client instance of LSF SOAPSearch web service */
    protected SOAPSearch soapsearch;

    /** The client instance of LSF DBInterface web service */
    protected DBInterface dbinterface;

    /** The Log4J Logger */
    protected final static Logger LOGGER = LogManager.getLogger(LSFService.class);

    protected LSFServiceCache cache;

    private static final String PARAM_PID = "pid";

    static final String PARAM_FIRSTNAME = "firstname";

    static final String PARAM_LASTNAME = "lastName";

    private static final String PARAM_ENCODING = "UTF-8";

    public LSFService() {
        try {
            cache = LSFServiceCache.instance();

            String baseURL = MCRConfiguration2.getStringOrThrow("UBO.LSF.SOAP.BaseURL");

            URL soapSearchEndpoint = new URL(baseURL + "/soapsearch");
            soapsearch = new SOAPSearchServiceLocator().getsoapsearch(soapSearchEndpoint);

            URL dbInterfaceEndpoint = new URL(baseURL + "/dbinterface");
            dbinterface = new DBInterfaceServiceLocator().getdbinterface(dbInterfaceEndpoint);
        } catch (Exception ex) {
            String msg = "Could not locate HIS LSF WebService";
            throw new MCRException(msg, ex);
        }
    }

    /**
     * TODO: rewrite doc/check impl
     * Returns an XML element containing detailed data of the given person.
     *pid
     * @param attributes the HIS LSF person ID
     */
    public Element getPersonDetails(Map<String, String> attributes) {
        Element person = new Element("Person");

        if (attributes.containsKey(PARAM_PID)) {
            String pid = attributes.get(PARAM_PID);

            person = cache.getIfUpToDate(pid);

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
        } else {
            LOGGER.info("Could not get HIS LSF person details, no pid provided in attributes: {}", attributes);
        }

        return person.clone();
    }

    /**
     * TODO: rewrite doc/check impl
     * Returns an XML element containing a list of all person data found in HIS LSF for the given name.
     * The method searches in the field personal.nachname.
     *
     * @param attributes The last name (or part of it) of the person to search for. The first name of the person to search for.
     */
    public Element searchPerson(Map<String, String> attributes) {
        Element results = new Element("results");
        String firstName = "";
        String lastName = "";

        if (attributes.containsKey(PARAM_FIRSTNAME) && attributes.containsKey(PARAM_LASTNAME)) {
            try {
                lastName = URLDecoder.decode(attributes.get(PARAM_LASTNAME), PARAM_ENCODING);
                firstName = URLDecoder.decode(attributes.get(PARAM_FIRSTNAME), PARAM_ENCODING);

                results = cache.getIfUpToDate(lastName);

                if (results == null) {
                    results = new Element("results");

                    Map<String, Element> found = new HashMap<String, Element>();
                    lookup(lastName, found);

                    String variantName = lastName.replace("ue", "\u00fc").replace("oe", "\u00f6")
                        .replace("ae", "\u00e4")
                        .replace("ss", "\u00df");
                    if (!variantName.equals(lastName)) {
                        lookup(variantName, found);
                    }

                    variantName = Normalizer.normalize(lastName, Form.NFD).replaceAll("\\p{M}", "");
                    if (!variantName.equals(lastName)) {
                        lookup(variantName, found);
                    }

                    results.addContent(found.values());
                    cache.put(lastName, results);

                    LOGGER.info(
                        "LSF search for person with name = '" + lastName + "': " + results.getContentSize()
                            + " found.");
                }
            } catch (Exception ex) {
                LOGGER.error("Error while searching HIS LSF for person name", ex);
            }
        } else {
            LOGGER.info("Could not search for HIS LSF person details, missing {} and/or {} in attributes: {}",
                PARAM_FIRSTNAME, PARAM_LASTNAME, attributes);
        }

        results = results.clone();

        removeThoseWithNonMatchingFirstNames(firstName, results);

        return results;
    }

    @Override
    public PersonSearchResult searchPerson(String query) {
        PersonSearchResult personSearchResult = new PersonSearchResult();
        personSearchResult.personList = new ArrayList<PersonSearchResult.PersonResult>();
        Set<String> retrievedIDs = new HashSet<String>();

        query = query.replaceAll("[^\\p{L}\\,]", " ").trim();
        String lastName = "";
        String firstName = "";

        if (query.contains(",")) {
            lastName = query.split(",")[0].trim();
            firstName = query.split(",")[1].trim();
        } else if (query.contains(" ")) {
            int pos = query.lastIndexOf(" ");
            firstName = query.substring(0, pos).trim();
            lastName = query.substring(pos + 1).trim();
        } else {
            lastName = query;
        }

        if (!lastName.isBlank()) {
            lookup(firstName, lastName, personSearchResult.personList, retrievedIDs);

            String variantName = lastName
                .replace("ue", "\u00fc")
                .replace("oe", "\u00f6")
                .replace("ae", "\u00e4")
                .replace("ss", "\u00df");
            if (!variantName.equals(lastName)) {
                lookup(firstName, variantName, personSearchResult.personList, retrievedIDs);
            }

            variantName = Normalizer.normalize(lastName, Form.NFD).replaceAll("\\p{M}", "");
            if (!variantName.equals(lastName)) {
                lookup(firstName, variantName, personSearchResult.personList, retrievedIDs);
            }
        }

        personSearchResult.count = personSearchResult.personList.size();
        return personSearchResult;
    }

    private void lookup(String firstName, String lastName, List<PersonSearchResult.PersonResult> results,
        Set<String> retrievedIDs) {
        String request = buildRequest(lastName);

        try {
            String response = soapsearch.search(request);
            LOGGER.debug("Response: " + response);

            Element xml = new SAXBuilder().build(new StringReader(response)).getRootElement();

            Element list = xml.getChild("success").getChild("list");

            for (Element object : list.getChildren("object")) {

                PersonSearchResult.PersonResult personResult = new PersonSearchResult.PersonResult(this);

                List<Element> attributes = object.getChildren("attribute");
                for (Element attribute : attributes) {
                    String value = attribute.getAttributeValue("value");
                    if (value == null || value.isBlank()) {
                        continue;
                    }

                    String name = attribute.getAttributeValue("name");
                    if ("Nachname".equals(name))
                        personResult.lastName = value;
                    else if ("Vorname".equals(name))
                        personResult.firstName = value;
                    else if ("ID".equals(name))
                        personResult.pid = value;
                }

                if (doesFirstNameMatch(firstName, personResult.firstName)
                    && (personResult.pid != null)
                    && !retrievedIDs.contains(personResult.pid)) {
                    personResult.displayName = personResult.lastName + ( personResult.firstName == null ? "" : ", " + personResult.firstName );
                    results.add(personResult);
                    retrievedIDs.add(personResult.pid);
                    personResult.affiliation = new ArrayList<String>();
                    getAffiliation(personResult);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            LOGGER.warn(ex);
        }
    }

    private void getAffiliation(PersonSearchResult.PersonResult pr) {
        Map<String, String> attr = new HashMap<>();
        attr.put(PARAM_PID, pr.pid);
        Element person = new LSFService().getPersonDetails(attr);
        XPathExpression<Element> expr = XPathFactory.instance().compile(
            "Funktion/EinDtx[string-length(text()) > 0]|Kontakt/FBZEDez[string-length(text()) > 0]",
            Filters.element());
        for (Element text : expr.evaluate(person)) {
            String affiliation = text.getTextTrim();
            if (!pr.affiliation.contains(affiliation))
                pr.affiliation.add(affiliation);
        }
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
                    if (value == null || value.trim().length() == 0) {
                        continue;
                    }

                    person.addContent(new Element(name.toLowerCase(Locale.ROOT)).setText(value.trim()));
                }

                if (person.getContentSize() > 0) {
                    String id = person.getChildTextTrim("id");
                    if (!results.containsKey(id)) {
                        results.put(id, person);
                    }
                }
            }
        } catch (Exception ex) {
            LOGGER.error("Error while searching HIS LSF for person name", ex);
        }
    }

    private boolean doesFirstNameMatch(String givenFirstName, String retrievedFirstName) {
        if (givenFirstName.isBlank())
            return true;
        if ((retrievedFirstName == null) || retrievedFirstName.isBlank())
            return true;

        String[] givenFirstNameParts = getNormalizedNameParts(givenFirstName);
        String[] retrievedFirstNameParts = getNormalizedNameParts(retrievedFirstName);

        return matches(givenFirstNameParts, retrievedFirstNameParts);
    }

    private void removeThoseWithNonMatchingFirstNames(String firstName, Element results) {
        String[] firstNameParts = getNormalizedNameParts(firstName);

        for (Iterator<Element> iterator = results.getChildren().iterator(); iterator.hasNext();) {
            Element person = iterator.next();
            String vorname = person.getChildTextTrim("vorname");
            if (vorname == null) {
                continue;
            }

            String[] candidateNameParts = getNormalizedNameParts(vorname);
            if (!matches(firstNameParts, candidateNameParts)) {
                iterator.remove();
            }
        }
    }

    private boolean matches(String[] firstNameParts, String[] candidateNameParts) {
        for (String a : firstNameParts) {
            for (String b : candidateNameParts) {
                if (b.startsWith(a)) {
                    return true;
                }
            }
        }

        return false;
    }

    private String[] getNormalizedNameParts(String text) {
        text = text.toLowerCase(Locale.ROOT);
        text = new MCRHyphenNormalizer().normalize(text).replace("-", " ");
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

        Map<String, String> attributes = new HashMap<>();
        attributes.put(PARAM_LASTNAME, "L\\u00fctzenkirchen");
        attributes.put(PARAM_FIRSTNAME, "Frank");
        attributes.put(PARAM_PID, "11775");

        Element results = new LSFService().searchPerson(attributes);
        xout.output(results, System.out);

        Element person = new LSFService().getPersonDetails(attributes);
        xout.output(person, System.out);
    }
}
