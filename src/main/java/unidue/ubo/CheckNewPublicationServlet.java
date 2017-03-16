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
import java.net.URLEncoder;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocumentList;
import org.jdom2.Document;
import org.jdom2.Element;
import org.mycore.common.MCRConstants;
import org.mycore.common.MCRSessionMgr;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;
import org.mycore.solr.MCRSolrClientFactory;
import org.mycore.solr.MCRSolrUtils;

import unidue.ubo.dedup.DeDupCriteriaBuilder;
import unidue.ubo.dedup.DeDupCriterion;

@SuppressWarnings("serial")
public class CheckNewPublicationServlet extends MCRServlet {

    public final static Logger LOGGER = LogManager.getLogger(CheckNewPublicationServlet.class);

    public void doGetPost(MCRServletJob job) throws Exception {
        HttpServletRequest req = job.getRequest();
        HttpServletResponse res = job.getResponse();

        Document doc = (Document) req.getAttribute("MCRXEditorSubmission");
        Element mods = doc.getRootElement();

        Set<DeDupCriterion> criteria = new DeDupCriteriaBuilder().buildFromMODS(mods);

        StringBuilder q = new StringBuilder("status:confirmed AND (");
        for (DeDupCriterion criterion : criteria)
            q.append("dedup:").append(MCRSolrUtils.escapeSearchValue(criterion.getKey())).append(" OR ");

        q.append("(title:\"").append(getTitle(mods)).append('"');
        q.append(" AND person:\"").append(getName(mods)).append("\"))");

        String importKey = buildImportKey(criteria);
        MCRSessionMgr.getCurrentSession().put(importKey, mods.detach());
        mods.removeChild("name", MCRConstants.MODS_NAMESPACE);

        StringBuffer url = new StringBuffer();
        if (publicationMayAlreadyExist(q.toString())) {
            url.append(MCRServlet.getServletBaseURL()).append("SolrSelectProxy?q=");
            url.append(URLEncoder.encode(q.toString(), "UTF-8"));
            url.append("&rows=10&XSL.Style=duplicates&XSL.importKey=").append(importKey);
        } else {
            url.append(MCRFrontendUtil.getBaseURL()).append("edit-publication.xed?importKey=").append(importKey);
        }
        res.sendRedirect(res.encodeRedirectURL(url.toString()));
    }

    private String getName(Element mods) {
        return mods.getChild("name", MCRConstants.MODS_NAMESPACE).getChildTextTrim("namePart",
            MCRConstants.MODS_NAMESPACE);
    }

    private String getTitle(Element mods) {
        return mods.getChild("titleInfo", MCRConstants.MODS_NAMESPACE).getChildTextTrim("title",
            MCRConstants.MODS_NAMESPACE);
    }

    private String buildImportKey(Set<DeDupCriterion> criteria) {
        StringBuilder sb = new StringBuilder();
        for (DeDupCriterion criterion : criteria)
            sb.append(criterion.getKey());
        return sb.toString();
    }

    private boolean publicationMayAlreadyExist(String q) throws SolrServerException, IOException {
        SolrClient solrClient = MCRSolrClientFactory.getSolrClient();
        SolrQuery query = new SolrQuery();
        query.setQuery(q);
        query.setRows(0);
        SolrDocumentList results = solrClient.query(query).getResults();
        return results.getNumFound() > 0;
    }
}