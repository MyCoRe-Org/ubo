package unidue.ubo.importer.orcid;

import java.io.IOException;
import java.util.List;

import org.jdom2.Element;
import org.mycore.common.MCRConstants;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.content.transformer.MCRContentTransformer;

public class Orcid2WorksTransformer extends MCRContentTransformer {

    public MCRJDOMContent transform(MCRContent source) throws IOException {
        String orcid = source.asString();

        OrcidWorksFetcher fetcher = new OrcidWorksFetcher(orcid);
        List<Element> works = fetcher.fetchWorks();

        Element modsCollection = new Element("modsCollection", MCRConstants.MODS_NAMESPACE);
        works.forEach(mods -> modsCollection.addContent(mods));
        return new MCRJDOMContent(modsCollection);
    }
}
