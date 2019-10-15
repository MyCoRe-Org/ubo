package unidue.ubo.csl;

import java.io.IOException;

import org.mycore.common.config.MCRConfiguration;
import org.mycore.common.config.MCRConfigurationException;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRStringContent;
import org.mycore.common.content.transformer.MCRContentTransformer;

import de.undercouch.citeproc.CSL;
import de.undercouch.citeproc.output.Bibliography;

/* 
 * This is actually not thread-safe, but not to re-use the 
 * citation processor will make transformation much too slow.
 * Consider as experimental solution. 
 */
public class MCRBibTeXCSLTransformer extends MCRContentTransformer {

    private MCRItemDataProvider dataProvider;

    private CSL citationProcessor;

    @Override
    public void init(String id) {
        super.init(id);

        String prefix = "MCR.ContentTransformer." + id + ".";
        String style = MCRConfiguration.instance().getString(prefix + "CitationStyle");
        String format = MCRConfiguration.instance().getString(prefix + "OutputFormat");

        this.dataProvider = new MCRItemDataProvider();
        try {
            this.citationProcessor = new CSL(dataProvider, style);
        } catch (IOException ex) {
            String msg = "Unsupported citation style (%s) or output format (%s)";
            throw new MCRConfigurationException(String.format(msg, style, format), ex);
        }
        this.citationProcessor.setOutputFormat(format);
    }

    @Override
    public MCRContent transform(MCRContent bibTeX) throws IOException {
        dataProvider.addBibTeX(bibTeX);
        dataProvider.registerCitationItems(citationProcessor);

        Bibliography biblio = citationProcessor.makeBibliography();
        String result = biblio.makeString();

        // Issue 53 with citeproc-java, may return "null", call twice
        if (result.contains("null")) {
            biblio = citationProcessor.makeBibliography();
            result = biblio.makeString();
        }

        citationProcessor.reset();
        dataProvider.reset();

        return new MCRStringContent(result);
    }
}
