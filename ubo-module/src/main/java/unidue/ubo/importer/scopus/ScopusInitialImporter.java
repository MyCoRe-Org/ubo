package unidue.ubo.importer.scopus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.MCRUserInformation;
import org.mycore.common.content.MCRURLContent;
import org.xml.sax.SAXException;

public class ScopusInitialImporter {

    private static final ExecutorService poolExecutor = Executors.newSingleThreadExecutor();
    private static final int MAX_COUNT = 200;
    public static final String IMPORT_SINGLE_COMMAND = "ubo initial import from scopus id {0}";
    public static final String IMPORT_BATCH_COMMAND = "ubo initial import from scopus affiliation {0} at {1}";


    public static void doImport(String id) throws MCRAccessException {
        final ScopusImporter scopusImporter = new ScopusImporter();
        scopusImporter.doImport(id);
    }

    public static List<String> initialImport(String affiliation, int start) throws IOException, JDOMException, SAXException {
        final ArrayList<String> commandList = new ArrayList<>();
        final ScopusAffiliationQuery scopusAffiliationQuery = new ScopusAffiliationQuery(affiliation);
        scopusAffiliationQuery.setCount(MAX_COUNT);
        scopusAffiliationQuery.setStart(start);
        Document response = new MCRURLContent(scopusAffiliationQuery.buildAffiliationQuery()).asXML();
        final Element rootElement = response.getRootElement();
        final Element documents = rootElement.getChild("documents");

        documents.getChildren("abstract-document")
                .stream()
                .map(scopusAffiliationQuery::getScopusIDFromContainer)
                .map(id -> IMPORT_SINGLE_COMMAND.replace("{0}",id))
                .forEach(commandList::add);
        scopusAffiliationQuery.setStart(scopusAffiliationQuery.getStart()+scopusAffiliationQuery.getCount());
        int totalAvailable = Integer.parseInt(documents.getAttributeValue("total-available"));

        if(scopusAffiliationQuery.getStart()<totalAvailable){
            commandList.add(IMPORT_BATCH_COMMAND
                    .replace("{0}", affiliation)
                    .replace("{1}", String.valueOf(scopusAffiliationQuery.getStart())));
        }

        return commandList;
    }



}
