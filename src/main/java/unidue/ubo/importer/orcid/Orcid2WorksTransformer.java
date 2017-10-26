package unidue.ubo.importer.orcid;

import java.io.IOException;

import org.jdom2.Element;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.content.transformer.MCRContentTransformer;
import org.mycore.common.xml.MCRURIResolver;

public class Orcid2WorksTransformer extends MCRContentTransformer {

    public MCRJDOMContent transform(MCRContent source) throws IOException {
        String orcid = source.asString();
        String pattern = "https://pub.orcid.org/v2.1/%s/works";
        String uri = String.format(pattern, orcid);
        Element result = MCRURIResolver.instance().resolve(uri);
        return new MCRJDOMContent(result);
    }
}
