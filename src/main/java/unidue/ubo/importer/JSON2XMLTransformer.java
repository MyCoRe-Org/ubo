package unidue.ubo.importer;

import java.io.IOException;
import java.net.URL;

import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRStringContent;
import org.mycore.common.content.MCRURLContent;
import org.mycore.common.content.transformer.MCRContentTransformer;
import org.mycore.common.content.transformer.MCRContentTransformerFactory;
import org.mycore.common.content.transformer.MCRParameterizedTransformer;
import org.mycore.common.events.MCRStartupHandler;
import org.mycore.common.xsl.MCRParameterCollector;

/**
 * Input: JSON string
 * Output: XML as converted by XSLT 3.0 json-to-xml() function
 *
 * @author Frank L\u00FCtzenkirchen
 */
public class JSON2XMLTransformer extends MCRContentTransformer {

    public static void main(String[] args) throws Exception {
        MCRStartupHandler.startUp(null);

        String url = "https://api.oadoi.org/v2/10.1038/nature12373?email=frank.luetzenkirchen@uni-due.de";
        MCRContent json = new MCRURLContent(new URL(url));
        MCRContent copy = json.getReusableCopy();
        System.out.println(copy.asString());

        MCRContentTransformer json2xml = MCRContentTransformerFactory.getTransformer("json2xml");
        MCRContent out = json2xml.transform(json);
        System.out.println(out.asString());
    }

    @Override
    public MCRContent transform(MCRContent json) throws IOException {
        MCRContent dummy = new MCRStringContent("<dummy/>");
        MCRContentTransformer t = MCRContentTransformerFactory.getTransformer("dummy+json2xml");
        MCRParameterizedTransformer pt = (MCRParameterizedTransformer) t;
        MCRParameterCollector params = new MCRParameterCollector();
        params.setParameter("json", json.asString());
        MCRContent result = pt.transform(dummy, params);
        return result;

    }
}
