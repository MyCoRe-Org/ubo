package org.mycore.ubo.importer.orcid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.content.transformer.MCRContentTransformer;
import org.mycore.orcid2.MCRORCIDUtils;
import org.mycore.orcid2.client.exception.MCRORCIDRequestException;
import org.mycore.orcid2.v3.MCRORCIDClientHelper;
import org.mycore.orcid2.v3.MCRORCIDSectionImpl;
import org.mycore.orcid2.v3.MCRORCIDWorkUtils;
import org.orcid.jaxb.model.v3.release.record.Work;
import org.orcid.jaxb.model.v3.release.record.summary.WorkGroup;
import org.orcid.jaxb.model.v3.release.record.summary.WorkSummary;
import org.orcid.jaxb.model.v3.release.record.summary.Works;

public class Orcid2WorksTransformer extends MCRContentTransformer {

    protected Logger LOGGER = LogManager.getLogger(Orcid2WorksTransformer.class);

    public MCRJDOMContent transform(MCRContent source) throws IOException {
        List<Work> workList = new ArrayList<>();

        Arrays.stream(source.asString().split("\\s"))
            .filter(orcid -> orcid.trim().length() > 0)
            .forEach(orcid -> {
                try {
                    Works works = MCRORCIDClientHelper.getClientFactory().createReadClient().fetch(orcid,
                        MCRORCIDSectionImpl.WORKS, Works.class);

                    for (WorkGroup wg : works.getWorkGroup()) {
                        WorkSummary ws = wg.getWorkSummary().stream()
                            .filter(workSummary -> workSummary.getDisplayIndex().equals("1")).findFirst().get();

                        Work work = MCRORCIDClientHelper.getClientFactory().createReadClient().fetch(orcid,
                            MCRORCIDSectionImpl.WORK, Work.class, ws.getPutCode());
                        workList.add(work);
                    }
                } catch (MCRORCIDRequestException e) {
                    LOGGER.warn("Could not get works for ORCID {}. {}", orcid, e.getMessage());
                }
            });

        List<Element> workElements = MCRORCIDWorkUtils.buildUnmergedMODSFromWorks(workList);
        Element modsCollection = MCRORCIDUtils.buildMODSCollection(workElements);

        /*TODO handle putCOdes */
        return new MCRJDOMContent(modsCollection);
    }
}
