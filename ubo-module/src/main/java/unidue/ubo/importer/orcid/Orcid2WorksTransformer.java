package unidue.ubo.importer.orcid;

import java.io.IOException;

import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.content.transformer.MCRContentTransformer;
import org.mycore.orcid.MCRORCIDProfile;
import org.mycore.orcid.works.MCRWorksSection;
import org.xml.sax.SAXException;

public class Orcid2WorksTransformer extends MCRContentTransformer {

    public MCRJDOMContent transform(MCRContent source) throws IOException {
        String orcid = source.asString();

        MCRORCIDProfile profile = new MCRORCIDProfile(orcid);
        try {
            MCRWorksSection worksSection = profile.getWorksSection();
            worksSection.fetchDetails();

            Element modsCollection = worksSection.buildMODSCollection();
            return new MCRJDOMContent(modsCollection);
        } catch (JDOMException | SAXException ex) {
            throw new IOException(ex);
        }
    }
}
