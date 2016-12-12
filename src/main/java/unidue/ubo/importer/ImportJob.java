package unidue.ubo.importer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.transformer.MCRContentTransformer;
import org.mycore.common.content.transformer.MCRContentTransformerFactory;
import org.xml.sax.SAXException;

public abstract class ImportJob {

    private final static Logger LOGGER = Logger.getLogger(ImportJob.class);

    protected MCRContent source;

    protected String type;

    protected String label;

    protected ImportJob(String type) {
        this.type = type;
    }

    public List<Document> transform() throws IOException, JDOMException, SAXException {
        LOGGER.info("Importing " + type + " from " + label);

        String transformerID = "import." + type;
        MCRContentTransformer transformer = MCRContentTransformerFactory.getTransformer(transformerID);
        MCRContent transformed = transformer.transform(source);
        Element collection = transformed.asXML().getRootElement();
        List<Document> entries = extractBibEntries(collection);

        int num = entries.size();
        LOGGER.info("Transformed " + num + " " + type + " entries to MODS bibliography entries");
        return entries;
    }

    private List<Document> extractBibEntries(Element collection) {
        List<Document> entries = new ArrayList<Document>();
        for (Element bibentry : collection.getChildren())
            entries.add(new Document(bibentry.clone()));
        return entries;
    }
}