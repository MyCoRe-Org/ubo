/**
 * Copyright (c) 2016 Duisburg-Essen University Library
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package unidue.ubo;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocumentList;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.common.MCRConstants;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.xml.MCRLayoutService;
import org.mycore.common.xml.MCRURIResolver;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;
import org.mycore.solr.MCRSolrClientFactory;
import org.mycore.solr.MCRSolrUtils;

import unidue.ubo.dedup.DeDupCriteriaBuilder;
import unidue.ubo.dedup.DeDupCriterion;

@SuppressWarnings("serial")
public class NewPublicationWizard extends MCRServlet {

    public final static Logger LOGGER = LogManager.getLogger(NewPublicationWizard.class);

    private final static String sessionKey = "ubo.submission";

    public void doGetPost(MCRServletJob job) throws Exception {
        storeAnySubmittedMODS(job);

        String step = job.getRequest().getParameter("step");
        if (step == null) {
            if (hasIdentifier()) {
                resolveByIdentifier();
                if (hasEmptyTitle()) {
                    job.getRequest().setAttribute("XSL.Style", "wizard-notfound");
                    Element mods = getMODSfromSession();
                    MCRLayoutService.instance().doLayout(job.getRequest(), job.getResponse(), new MCRJDOMContent(mods));
                    return;
                }
            }
            checkForExistingDuplicates(job);
        } else if (step.equals("genres"))
            redirectToGenreSelection(job.getResponse());
        else if (step.equals("form"))
            redirectToForm(job.getResponse());
    }

    private void storeAnySubmittedMODS(MCRServletJob job) throws IOException {
        Document doc = (Document) job.getRequest().getAttribute("MCRXEditorSubmission");
        if (doc != null) {
            Element mods = doc.getRootElement().detach();
            MCRSessionMgr.getCurrentSession().put(sessionKey, mods);
        }
    }

    private Element getMODSfromSession() {
        return (Element) (MCRSessionMgr.getCurrentSession().get(sessionKey));
    }

    private void resolveByIdentifier() {
        Element mods = getMODSfromSession();
        mods.removeChildren("titleInfo", MCRConstants.MODS_NAMESPACE);
        mods.removeChildren("name", MCRConstants.MODS_NAMESPACE);
        mods = MCRURIResolver.instance().resolve("enrich:import:session:" + sessionKey);
        MCRSessionMgr.getCurrentSession().put(sessionKey, mods);
    }

    private boolean hasEmptyTitle() {
        Element mods = getMODSfromSession();
        String title = getByXPath(mods, "mods:titleInfo/mods:title");
        return title.isEmpty();
    }

    private boolean hasIdentifier() {
        Element mods = getMODSfromSession();
        String identifier = getByXPath(mods, "mods:identifier");
        String shelfmark = getByXPath(mods, "mods:location/mods:shelfLocator");
        return !(identifier.isEmpty() && shelfmark.isEmpty());
    }

    private void checkForExistingDuplicates(MCRServletJob job)
        throws UnsupportedEncodingException, SolrServerException, IOException {
        Element mods = getMODSfromSession();
        String query = buildQuery(mods);

        if (publicationMayAlreadyExist(query))
            redirectToResults(job.getResponse(), query);
        else
            redirectToGenreSelection(job.getResponse());
    }

    private String buildQuery(Element mods) {
        StringBuilder query = new StringBuilder("status:confirmed AND (");

        Set<DeDupCriterion> criteria = new DeDupCriteriaBuilder().buildFromMODS(mods);
        for (DeDupCriterion criterion : criteria)
            query.append("dedup:").append(MCRSolrUtils.escapeSearchValue(criterion.getKey())).append(" OR ");

        query.append("(title:\"").append(getByXPath(mods, "mods:titleInfo/mods:title")).append('"');
        query.append(" AND person:\"").append(getByXPath(mods, "mods:name/mods:namePart")).append("\"))");

        return query.toString();
    }

    private boolean publicationMayAlreadyExist(String q) throws SolrServerException, IOException {
        SolrClient solrClient = MCRSolrClientFactory.getSolrClient();
        SolrQuery query = new SolrQuery();
        query.setQuery(q);
        query.setRows(0);
        SolrDocumentList results = solrClient.query(query).getResults();
        return results.getNumFound() > 0;
    }

    private void redirectToResults(HttpServletResponse res, String q) throws UnsupportedEncodingException, IOException {
        StringBuffer url = new StringBuffer();
        url.append(MCRServlet.getServletBaseURL()).append("SolrSelectProxy?q=");
        url.append(URLEncoder.encode(q.toString(), "UTF-8"));
        url.append("&rows=10&XSL.Style=duplicates");
        res.sendRedirect(res.encodeRedirectURL(url.toString()));
    }

    private void redirectToGenreSelection(HttpServletResponse res) throws IOException {
        String url = MCRFrontendUtil.getBaseURL() + "select-genre.xed?step=form";
        res.sendRedirect(res.encodeRedirectURL(url));
    }

    private void redirectToForm(HttpServletResponse res) throws IOException {
        Element mods = getMODSfromSession();
        if (!hasIdentifier())
            mods.removeChild("name", MCRConstants.MODS_NAMESPACE);
        
        String form = mods.getAttribute("form").detach().getValue();
        String url = MCRFrontendUtil.getBaseURL() + form + "?key=" + sessionKey;
        res.sendRedirect(res.encodeRedirectURL(url));
    }

    private String getByXPath(Element mods, String xPath) {
        List<Namespace> NS = new ArrayList<Namespace>();
        NS.add(Namespace.getNamespace("mods", "http://www.loc.gov/mods/v3"));
        XPathExpression<Element> xPathExpr = XPathFactory.instance().compile(xPath, Filters.element(), null, NS);
        Element result = xPathExpr.evaluateFirst(mods);
        return (result == null ? "" : result.getTextTrim());
    }
}