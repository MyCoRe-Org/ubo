package org.mycore.mods.enrichment;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom2.Document;
import org.jdom2.Element;
import org.mycore.common.MCRConstants;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;
import org.mycore.ubo.AccessControl;

/**
 * Backend of enrichmentDebugger.xed:
 * Takes mods:mods and selected enrichment resolver configuration.
 * Returns enrichment resolver debugging output to be rendered by debugEnrichment.xsl
 * 
 * @author Frank Lützenkirchen
 **/
@SuppressWarnings("serial")
public class EnrichmentDebuggerServlet extends MCRServlet {

    public void doGetPost(MCRServletJob job) throws Exception {
        HttpServletRequest req = job.getRequest();
        HttpServletResponse res = job.getResponse();

        if (!AccessControl.currentUserIsAdmin()) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        Document doc = (Document) req.getAttribute("MCRXEditorSubmission");
        Element root = doc.getRootElement();

        Element choosenEnricher = root.getChild("enrichers").getChild("enricher");
        String enricherID = choosenEnricher.getAttributeValue("id");
        
        if ("custom".equals(enricherID)) {
            // Do ad-hoc configuration of a custom enrichment resolver
            String propertyName = "MCR.MODS.EnrichmentResolver.DataSources." + enricherID;
            MCRConfiguration2.set(propertyName, choosenEnricher.getTextTrim());
        }
        
        MCREnricher enricher = new MCREnricher(enricherID);

        MCRToXMLEnrichmentDebugger debugger = new MCRToXMLEnrichmentDebugger();
        enricher.setDebugger(debugger);

        Element mods = root.getChild("mods", MCRConstants.MODS_NAMESPACE).detach();
        enricher.enrich(mods);

        Element output = debugger.getDebugXML();
        output.addContent(0, choosenEnricher.detach());
        output.addContent(new Element("result").addContent(mods));

        getLayoutService().doLayout(req, res, new MCRJDOMContent(output));
    }
}
