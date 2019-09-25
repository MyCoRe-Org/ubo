package unidue.ubo;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jbibtex.BibTeXDatabase;
import org.jbibtex.ParseException;
import org.jdom2.Element;
import org.mycore.common.MCRConstants;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.content.MCRStringContent;
import org.mycore.common.content.transformer.MCRContentTransformer;
import org.mycore.common.content.transformer.MCRContentTransformerFactory;
import org.mycore.common.content.transformer.MCRTransformerPipe;
import org.mycore.common.content.transformer.MCRXSLTransformer;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;
import org.mycore.mods.MCRMODSWrapper;

import de.undercouch.citeproc.CSL;
import de.undercouch.citeproc.ItemDataProvider;
import de.undercouch.citeproc.bibtex.BibTeXConverter;
import de.undercouch.citeproc.bibtex.BibTeXItemDataProvider;
import de.undercouch.citeproc.csl.CSLItemData;
import de.undercouch.citeproc.output.Bibliography;

@WebServlet(name = "CSLServlet", urlPatterns = { "/servlets/CSLServlet" })
public class CSLServlet extends MCRServlet {

    private static final long serialVersionUID = 1L;

    private MCRTransformerPipe mods2bibTeX;

    public CSLServlet() {
        super();
        MCRXSLTransformer mods2bibmods = MCRXSLTransformer.getInstance("xsl/mods2bibmods.xsl");
        MCRContentTransformer bibmods2bibtex = MCRContentTransformerFactory.getTransformer("mods2bibtex");
        mods2bibTeX = new MCRTransformerPipe(mods2bibmods, bibmods2bibtex);
    }

    @Override
    protected void doGetPost(MCRServletJob job) throws Exception {
        HttpServletRequest req = job.getRequest();
        HttpServletResponse res = job.getResponse();

        String style = req.getParameter("style");
        String format = req.getParameter("format");
        if ((style == null) || style.isEmpty() || (format == null) || format.isEmpty()) {
            sendMissingParameterResponse(res);
            return;
        }

        MCRContentTransformer bibTeX2citation = MCRBibTeXCSLTransformerFactory.getTransformer(style, format);

        MCRContent modsCollectionXML = buildMODSCollection(req);
        MCRContent bibTeX = mods2bibTeX.transform(modsCollectionXML);
        MCRContent citation = bibTeX2citation.transform(bibTeX);

        sendFormattedCitation(res, format, citation);
    }

    private void sendMissingParameterResponse(HttpServletResponse res) throws IOException {
        List<String> styleIDs = new ArrayList<String>(CSL.getSupportedStyles());
        Collections.sort(styleIDs);

        String styles = "style= \n" + String.join("\n", styleIDs);
        String formats = "format=" + String.join(", ", CSL.getSupportedOutputFormats()) + "\n\n";
        send("Choose one of " + styleIDs.size() + " styles:\n\n", formats, styles, "text/plain", res);
    }

    private void sendFormattedCitation(HttpServletResponse res, String format, MCRContent citation) throws IOException {
        if ("html".equals(format)) {
            send(citation.asString(), "<html>\n<body>\n", "\n</body>\n</html>", "text/html", res);
        } else {
            send(citation.asString(), "", "", "text/plain", res);
        }
    }

    private void send(String output, String prefix, String suffix, String mimeType, HttpServletResponse res)
        throws IOException {
        res.setContentType(mimeType);
        res.setCharacterEncoding("UTF-8");
        PrintWriter pw = res.getWriter();
        pw.write(prefix);
        pw.write(output);
        pw.write(suffix);
        pw.close();
    }

    private MCRContent buildMODSCollection(HttpServletRequest req) {
        MCRMODSCollectionContent mcc = new MCRMODSCollectionContent();
        mcc.addMODSFrom(req.getParameterValues("id"));
        return mcc;
    }
}

class MCRMODSCollectionContent extends MCRJDOMContent {

    private Element modsCollection;

    public MCRMODSCollectionContent() {
        super(new Element("modsCollection", MCRConstants.MODS_NAMESPACE));
        modsCollection = super.asXML().getRootElement();
    }

    public void addMODSFrom(String... objectIDs) {
        for (String id : objectIDs) {
            addMODSFrom(id);
        }
    }

    private void addMODSFrom(String objectID) {
        addMODSFrom(MCRObjectID.getInstance(objectID));
    }

    private void addMODSFrom(MCRObjectID oid) {
        MCRObject obj = MCRMetadataManager.retrieveMCRObject(oid);
        MCRMODSWrapper wrapper = new MCRMODSWrapper(obj);
        Element mods = wrapper.getMODS().detach();
        modsCollection.addContent(mods);
    }
}

class MCRBibTeXCSLTransformerFactory {

    private static Map<String, MCRBibTeXCSLTransformer> cslTransformers = new HashMap<String, MCRBibTeXCSLTransformer>();

    public static MCRBibTeXCSLTransformer getTransformer(String citationStyle, String outputFormat)
        throws IOException {
        String key = citationStyle + "-" + outputFormat;

        MCRBibTeXCSLTransformer bibTeX2citation;
        synchronized (MCRBibTeXCSLTransformerFactory.class) {
            if (cslTransformers.containsKey(key)) {
                bibTeX2citation = cslTransformers.get(key);
            } else {
                bibTeX2citation = new MCRBibTeXCSLTransformer(citationStyle, outputFormat);
                cslTransformers.put(key, bibTeX2citation);
            }
        }

        return bibTeX2citation;
    }
}

/* 
 * This is actually not thread-safe, but not to re-use the 
 * citation processor will make transformation much too slow.
 * Consider as experimental solution. 
 */
class MCRBibTeXCSLTransformer extends MCRContentTransformer {

    private MCRItemDataProvider dataProvider;

    private CSL citationProcessor;

    public MCRBibTeXCSLTransformer(String citationStyle, String outputFormat) throws IOException {
        this.dataProvider = new MCRItemDataProvider();
        this.citationProcessor = new CSL(dataProvider, citationStyle);
        this.citationProcessor.setOutputFormat(outputFormat);
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

/** Wrapper around BibTeXItemDataProvider to make it reusable */
class MCRItemDataProvider implements ItemDataProvider {

    private BibTeXItemDataProvider wrappedProvider = new BibTeXItemDataProvider();

    public void addBibTeX(MCRContent bibTeX) throws IOException {
        InputStream in = bibTeX.getInputStream();
        BibTeXDatabase db;
        try {
            db = new BibTeXConverter().loadDatabase(in);
        } catch (ParseException ex) {
            throw new IOException(ex);
        }
        in.close();

        wrappedProvider.addDatabase(db);
    }

    public void reset() {
        wrappedProvider = new BibTeXItemDataProvider();
    }

    @Override
    public CSLItemData retrieveItem(String id) {
        return wrappedProvider.retrieveItem(id);
    }

    @Override
    public String[] getIds() {
        return wrappedProvider.getIds();
    }

    public void registerCitationItems(CSL citationProcessor) {
        wrappedProvider.registerCitationItems(citationProcessor);
    }
}
