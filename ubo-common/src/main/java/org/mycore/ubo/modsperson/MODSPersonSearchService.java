package org.mycore.ubo.modsperson;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.jdom2.Element;
import org.mycore.common.MCRException;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.solr.MCRSolrClientFactory;
import org.mycore.solr.MCRSolrUtils;
import org.mycore.ubo.picker.IdentityService;
import org.mycore.ubo.picker.PersonSearchResult;

import javax.naming.OperationNotSupportedException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * {@link IdentityService} for search by modsperson. Activate by configuration:
 * <pre>MCR.IdentityPicker.strategy=org.mycore.ubo.modsperson.MODSPersonSearch</pre>
 * Uses a Solr search with object-type "modsperson" and field "person" for query.
 */
public class MODSPersonSearchService implements IdentityService {

    private final String LEAD_ID = MCRConfiguration2.getStringOrThrow("MCR.user2.matching.lead_id");

    @Override
    public Element getPersonDetails(Map<String, String> paramMap) {
        return null;
    }

    @Override
    public Element searchPerson(Map<String, String> paramMap) {
        try {
            SolrDocumentList searchResults = searchPersonSolr(paramMap.get("lastName") + ", " + paramMap.get("firstName"));
            final Element resultsElement = new Element("results");
            for (SolrDocument doc : searchResults) {
                resultsElement.addContent(createPersonElement(doc));
            }
            return resultsElement;
        } catch (SolrServerException | IOException e) {
            throw new MCRException("There was an error while searching for: " + paramMap.get("lastName") + ", " + paramMap.get("firstName"), e);
        }
    }

    @Override
    public PersonSearchResult searchPerson(String query) throws OperationNotSupportedException {
        try {
            SolrDocumentList results = searchPersonSolr(query);

            List<PersonSearchResult.PersonResult> personResults = new ArrayList<>();

            for (SolrDocument doc : results) {
                PersonSearchResult.PersonResult result = new PersonSearchResult.PersonResult(this);

                List<String> persons = (List) doc.getFieldValue("person");
                if (persons != null && !persons.isEmpty()) {
                    result.displayName = persons.get(0);
                }
                List<String> pids = ((List<String>) doc.getFieldValue("nid_" + LEAD_ID));
                if (pids != null && !pids.isEmpty()) {
                    result.pid = pids.get(0);
                }
                personResults.add(result);
            }

            PersonSearchResult personSearchResult = new PersonSearchResult();
            personSearchResult.count = personResults.size();
            personSearchResult.personList = personResults;
            return personSearchResult;
        } catch (SolrServerException | IOException e) {
            throw new MCRException("There was an error while searching for: " + query, e);
        }
    }

    private SolrDocumentList searchPersonSolr(String searchName) throws SolrServerException, IOException {
        final SolrQuery query = new SolrQuery("objectType:modsperson AND person:\"" + MCRSolrUtils.escapeSearchValue(searchName) + "\"")
            .setFields("nid_lsf, person").setRows(100).setParam("wt", "json");

        QueryResponse queryResponse = MCRSolrClientFactory.getMainSolrClient().query(query);
        return queryResponse.getResults();
    }

    private Element createPersonElement(SolrDocument doc) {
        final Element person = new Element("person");

        final Element idElement = new Element("id");
        idElement.addContent(((List<String>) doc.getFieldValue("nid_" + LEAD_ID)).get(0));
        person.addContent(idElement);

        final Element realNameElement = new Element("realName");
        final String realName = ((List<String>) doc.getFieldValue("person")).get(0);
        realNameElement.addContent(realName);
        person.addContent(realNameElement);

        final String[] realNameSplit = realName.split(" ", 2);

        final Element elementFirstName = new Element("firstName");
        elementFirstName.addContent(realNameSplit.length > 1 ? realNameSplit[1] : null);
        person.addContent(elementFirstName);

        final Element elementLastName = new Element("lastName");
        elementLastName.addContent(realNameSplit[0]);
        person.addContent(elementLastName);

        return person;
    }
}
