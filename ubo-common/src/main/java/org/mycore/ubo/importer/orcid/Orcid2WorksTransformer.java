package org.mycore.ubo.importer.orcid;

import java.io.IOException;

import org.jdom2.Document;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.content.transformer.MCRContentTransformer;
import org.mycore.orcid2.metadata.MCRORCIDUserInfo;
import org.mycore.orcid2.user.MCRORCIDUser;
import org.mycore.orcid2.user.MCRORCIDUserUtils;

public class Orcid2WorksTransformer extends MCRContentTransformer {

    public MCRJDOMContent transform(MCRContent source) throws IOException {
        String orcid = source.asString();

        MCRORCIDUser user = MCRORCIDUserUtils.getORCIDUserByORCID(orcid);
        MCRORCIDUserInfo userInfo = new MCRORCIDUserInfo(orcid);
        /* TODO
        MCRORCIDProfile profile = new MCRORCIDProfile(orcid);
        try {
            MCRWorksSection worksSection = profile.getWorksSection();
            worksSection.fetchDetails();

            Element modsCollection = worksSection.buildMODSCollection();
            return new MCRJDOMContent(modsCollection);
        } catch (JDOMException | SAXException ex) {
            throw new IOException(ex);
        }*/

        return new MCRJDOMContent(new Document());
    }
}
