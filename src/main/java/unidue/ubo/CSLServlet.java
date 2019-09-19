package unidue.ubo;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jbibtex.BibTeXDatabase;
import org.jdom2.Element;
import org.mycore.common.MCRConstants;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRJDOMContent;
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
import de.undercouch.citeproc.bibtex.BibTeXConverter;
import de.undercouch.citeproc.bibtex.BibTeXItemDataProvider;
import de.undercouch.citeproc.output.Bibliography;

@WebServlet(name = "CSLServlet", urlPatterns = { "/servlets/CSLServlet" })
public class CSLServlet extends MCRServlet {

    private static final long serialVersionUID = 1L;

    private MCRTransformerPipe mods2bibtex;

    public CSLServlet() {
        super();
        MCRXSLTransformer mods2bibmods = MCRXSLTransformer.getInstance("xsl/mods2bibmods.xsl");
        MCRContentTransformer bibmods2bibtex = MCRContentTransformerFactory.getTransformer("mods2bibtex");
        mods2bibtex = new MCRTransformerPipe(mods2bibmods, bibmods2bibtex);
    }

    @Override
    protected void doGetPost(MCRServletJob job) throws Exception {
        HttpServletRequest req = job.getRequest();
        HttpServletResponse res = job.getResponse();

        MCRContent modsCollectionXML = buildMODSCollection(req);
        MCRContent bibtex = mods2bibtex.transform(modsCollectionXML);

        InputStream cin = bibtex.getInputStream();
        BibTeXDatabase db = new BibTeXConverter().loadDatabase(cin);
        cin.close();
        BibTeXItemDataProvider provider = new BibTeXItemDataProvider();
        provider.addDatabase(db);

        String style = req.getParameter("style");
        String format = req.getParameter("format");
        if ((style == null) || style.isEmpty() || (format == null) || format.isEmpty()) {
            String styles = "style= " + String.join(", ", CSL.getSupportedStyles());
            String formats = "format=" + String.join(", ", CSL.getSupportedOutputFormats());
            send(styles, "Choose a style:\n\n", formats, "text/plain", res);
            return;
        }

        CSL citeproc = new CSL(provider, style);
        provider.registerCitationItems(citeproc);
        citeproc.setOutputFormat(format);
        Bibliography bibl = citeproc.makeBibliography();

        if ("html".equals(format)) {
            send(bibl.makeString(), "<html>\n<body>\n", "\n</body>\n</html>", "text/html", res);
        } else {
            send(bibl.makeString(), "", "", "text/plain", res);
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
        Element modsCollection = new Element("modsCollection", MCRConstants.MODS_NAMESPACE);
        for (String id : req.getParameterValues("id")) {
            MCRObjectID oid = MCRObjectID.getInstance(id);
            MCRObject obj = MCRMetadataManager.retrieveMCRObject(oid);
            MCRMODSWrapper wrapper = new MCRMODSWrapper(obj);
            Element mods = wrapper.getMODS().detach();
            modsCollection.addContent(mods);
        }
        MCRContent content = new MCRJDOMContent(modsCollection);
        return content;
    }
}
