package unidue.ubo.importer.orcid;

import java.io.IOException;

import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.content.transformer.MCRContentTransformer;
import org.mycore.orcid.MCRORCIDProfile;
import org.mycore.orcid.works.MCRWorks;
import org.xml.sax.SAXException;

public class Orcid2WorksTransformer extends MCRContentTransformer {

    public MCRJDOMContent transform(MCRContent source) throws IOException {
        String orcid = source.asString();

        MCRORCIDProfile profile = new MCRORCIDProfile(orcid);
        MCRWorks works = profile.getWorks();
        try {
            works.fetchSummaries();
            works.fetchDetails();
        } catch (JDOMException | SAXException ex) {
            throw new IOException(ex);
        }
        Element modsCollection = works.buildMODSCollection();
        return new MCRJDOMContent(modsCollection);
    }
}
