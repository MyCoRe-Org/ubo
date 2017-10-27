package unidue.ubo.importer.orcid;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.mycore.common.MCRConstants;
import org.mycore.common.MCRException;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.content.MCRStringContent;
import org.mycore.common.content.transformer.MCRXSLTransformer;
import org.mycore.common.xml.MCRURIResolver;
import org.mycore.mods.merger.MCRMerger;
import org.mycore.mods.merger.MCRMergerFactory;

import unidue.ubo.importer.bibtex.BibTeX2MODSTransformer;

public class OrcidWorksFetcher {

    private final static Logger LOGGER = LogManager.getLogger(OrcidWorksFetcher.class);

    private final static String NS_URI_WORK = "http://www.orcid.org/ns/work";
    private final static String NS_URI_ACTIVITIES = "http://www.orcid.org/ns/activities";

    private final static Namespace NS_ACTIVITIES = Namespace.getNamespace("activities", NS_URI_ACTIVITIES);
    private final static Namespace NS_WORK = Namespace.getNamespace("work", NS_URI_WORK);

    private final static String URI_2_FETCH_WORKS = "https://pub.orcid.org/v2.1/%s/works";

    private final static String URI_2_FETCH_WORK = "https://pub.orcid.org/v2.1/%s/work/%s";

    private final static MCRXSLTransformer T_WORK2MODS = new MCRXSLTransformer("xsl/import/orcid2mods.xsl");

    private final static BibTeX2MODSTransformer T_BIBTEX2MODS = new BibTeX2MODSTransformer();

    private String orcid;

    public OrcidWorksFetcher(String orcid) {
        this.orcid = orcid;
    }

    public List<Element> fetchWorks() {
        String uri = String.format(URI_2_FETCH_WORKS, orcid);
        LOGGER.info("fetching all works from " + uri);
        Element works = MCRURIResolver.instance().resolve(uri);

        List<Element> modsWorks = new ArrayList<Element>();
        for (Element group : works.getChildren("group", NS_ACTIVITIES)) {
            Element mods = fetchMergedGroupOfWorks(group);
            modsWorks.add(mods);
        }
        return modsWorks;
    }

    private Element fetchMergedGroupOfWorks(Element group) {
        Element modsResult = null;

        for (Element ws : group.getChildren("work-summary", NS_WORK)) {
            String putCode = ws.getAttributeValue("put-code");
            Element work = fetchWork(putCode);
            Element modsFromWork = work2MODS(work).detach();

            String bibTeX = getBibTeX(work);
            if (bibTeX != null) {
                Element modsFromBibTeX = bibTeX2MODS(bibTeX);
                LOGGER.info("merging MODS from BibTeX into work");
                merge(modsFromWork, modsFromBibTeX);
            }

            if (modsResult == null) {
                modsResult = modsFromWork;
            } else {
                LOGGER.info("merging work with first in group");
                merge(modsResult, modsFromWork);
            }

        }

        return modsResult;
    }

    private Element fetchWork(String putCode) {
        String uri = String.format(URI_2_FETCH_WORK, orcid, putCode);
        LOGGER.info("fetching work from " + uri);
        return MCRURIResolver.instance().resolve(uri);
    }

    private Element work2MODS(Element work) {
        try {
            MCRContent result = T_WORK2MODS.transform(new MCRJDOMContent(work));
            return result.asXML().detachRootElement();
        } catch (Exception ex) {
            throw new MCRException(ex);
        }
    }

    private String getBibTeX(Element work) {
        for (Element citation : work.getChildren("citation", NS_WORK)) {
            String type = citation.getChildTextTrim("citation-type", NS_WORK);
            if ("bibtex".equals(type)) {
                String bibtex = citation.getChildTextTrim("citation-value", NS_WORK);
                if (!bibtex.trim().isEmpty()) {
                    return bibtex;
                }
            }
        }
        return null;
    }

    private Element bibTeX2MODS(String bibTeX) {
        try {
            MCRContent result = T_BIBTEX2MODS.transform(new MCRStringContent(bibTeX));
            return result.asXML().detachRootElement().getChild("mods", MCRConstants.MODS_NAMESPACE);
        } catch (Exception ex) {
            throw new MCRException(ex);
        }
    }

    private void merge(Element mods, Element work) {
        MCRMerger mBase = MCRMergerFactory.buildFrom(mods);
        MCRMerger mWork = MCRMergerFactory.buildFrom(work);
        mBase.mergeFrom(mWork);
    }
}
