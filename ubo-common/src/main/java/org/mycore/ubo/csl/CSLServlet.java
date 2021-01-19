package org.mycore.ubo.csl;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mycore.common.content.MCRContent;
import org.mycore.common.content.transformer.MCRContentTransformer;
import org.mycore.common.content.transformer.MCRContentTransformerFactory;
import org.mycore.common.content.transformer.MCRTransformerPipe;
import org.mycore.common.content.transformer.MCRXSLTransformer;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;

import de.undercouch.citeproc.CSL;

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

        MCRContentTransformer bibTeX2citation = MCRContentTransformerFactory.getTransformer(style + "-" + format);
        //TODO: build and configure transformer on-the-fly, if not yet existing

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
