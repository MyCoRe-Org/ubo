package unidue.ubo.importer.scopus;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.mycore.common.MCRException;
import org.mycore.common.content.MCRURLContent;
import org.xml.sax.SAXException;

public class ScopusAffiliationQuery extends AbstractScopusQuery{

    private static final String AFFILIATION_QUERY_PATTERN = "/affiliation/affiliation_id/%1$s?&apikey=%2$s&insttoken=%3$s&count=%4$s&start=%5$s&view=DOCUMENTS";
    private static final int MAX_COUNT = 200;

    public String getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    private String affiliation;
    private int count;
    private int start;

    public ScopusAffiliationQuery(String affiliation, int count, int start) {
        setAffiliation(affiliation);
        setCount(count);
        setStart(start);
    }

    public ScopusAffiliationQuery(String affiliation){
        setAffiliation(affiliation);
        setCount(MAX_COUNT);
    }

    private URL buildAffiliationQuery() throws MalformedURLException {
        String queryString = String.format(AFFILIATION_QUERY_PATTERN, getAffiliation(), API_KEY, INST_TOKEN,getCount(), getStart());
        return new URL(API_URL + queryString);
    }

    @Override
    public List<String> resolveIDs() throws JDOMException, IOException, SAXException {
        return resolveDocuments().stream()
                .map(Document::getRootElement)
                .map(this::getScopusIDFromContainer)
                .collect(Collectors.toList());

    }

    public List<Document> resolveDocuments() throws IOException, JDOMException, SAXException {
        Document response = new MCRURLContent(buildAffiliationQuery()).asXML();
        return response.getRootElement()
                .getChild("documents")
                .getChildren("abstract-document")
                .stream()
                .map(Element::clone)
                .map(Element::detach)
                .map(Document::new)
                .collect(Collectors.toList());
    }

    public void resolveAllIDs(Consumer<String> onResolve){
        try {
            int totalAvailable=-1;
            this.setStart(0);
            do {
                Document response = new MCRURLContent(buildAffiliationQuery()).asXML();
                final Element rootElement = response.getRootElement();
                final Element documents = rootElement.getChild("documents");

                documents.getChildren("abstract-document")
                        .stream()
                        .map(this::getScopusIDFromContainer)
                        .forEach(onResolve);
                totalAvailable = Integer.parseInt(documents.getAttributeValue("total-available"));
                this.setStart(getStart()+getCount());
            } while (getStart()<totalAvailable);

        } catch (JDOMException|IOException|SAXException e) {
            throw new MCRException("Error while Streaming all documents!");
        }

    }
}
