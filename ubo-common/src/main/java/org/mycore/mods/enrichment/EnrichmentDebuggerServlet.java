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

        String enricherID = root.getAttributeValue("enricherID");
        
        if("custom".equals(enricherID)) {
            String propertyName = "MCR.MODS.EnrichmentResolver.DataSources.custom";
            String dataSources = root.getChildTextTrim("customDataSources");
            MCRConfiguration2.set(propertyName, dataSources);
        }
        
        MCREnricher enricher = new MCREnricher(enricherID);
        
        MCRToXMLEnrichmentDebugger debugger = new MCRToXMLEnrichmentDebugger();
        enricher.setDebugger(debugger);

        Element mods = root.getChild("mods", MCRConstants.MODS_NAMESPACE).detach();
        enricher.enrich(mods);

        Element output = debugger.getDebugXML();
        output.addContent(new Element("result").addContent(mods));

        getLayoutService().doLayout(req, res, new MCRJDOMContent(output));
    }
}
