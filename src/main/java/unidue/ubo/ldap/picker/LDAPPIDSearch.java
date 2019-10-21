package unidue.ubo.ldap.picker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.xml.MCRURIResolver;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;
import org.xml.sax.SAXException;
import unidue.ubo.picker.IdentityPickerService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * TODO: rename, remove "PID"...
 */
public class LDAPPIDSearch extends MCRServlet implements IdentityPickerService {

    private final static Logger LOGGER = LogManager.getLogger(LDAPPIDSearch.class);

    @Override
    public void handleRequest(MCRServletJob job) {
        HttpServletRequest req = job.getRequest();
        HttpServletResponse res = job.getResponse();

        String session = req.getParameter("_xed_subselect_session");

        try {
            doSearch(req, res, session);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doSearch(HttpServletRequest req, HttpServletResponse res, String session)
            throws TransformerException, IOException, SAXException {
        Element ldappidsearch = new Element("ldappidsearch");
        Map<String, String> attributes = getFlattenedParameters(req);
        LOGGER.info("SEARCHING with: {}", attributes);
        ldappidsearch.addContent(new LDAPService().searchPerson(attributes));
        MCRServlet.getLayoutService().doLayout(req, res, new MCRJDOMContent(ldappidsearch));
    }

    /**
     * TODO: doc
     */
    private Map<String, String> getFlattenedParameters(HttpServletRequest req) {
        // fix problem of encoded URL... TODO: ask Frank if this can be fixed or not
        // ask: "kann man innerhalb von XED das encoding ausschalten?" -> call-java{...}
        Map<String, String> flatParamMap = new HashMap<>();
        String decodedQueryString = "";
        try {
            decodedQueryString = URLDecoder.decode(req.getQueryString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        LOGGER.info("queryString: {}", decodedQueryString);
        flatParamMap = MCRURIResolver.getParameterMap(decodedQueryString);
        return flatParamMap;

        /*Map<String, String> flatParamMap = new HashMap<>();
        for(Map.Entry<String, String[]> parameterEntry : req.getParameterMap().entrySet()) {
            String paramName = parameterEntry.getKey();
            String paramValue = "";
            if(parameterEntry.getValue().length > 0) {
                paramValue = parameterEntry.getValue()[0];
            }
            flatParamMap.put(paramName, paramValue);
        }
        return flatParamMap;*/
    }

}

