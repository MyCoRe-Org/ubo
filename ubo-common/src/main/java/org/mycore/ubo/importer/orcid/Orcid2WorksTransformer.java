package org.mycore.ubo.importer.orcid;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.content.transformer.MCRContentTransformer;
import org.mycore.orcid2.MCRORCIDUtils;
import org.mycore.orcid2.client.exception.MCRORCIDRequestException;
import org.mycore.orcid2.v3.client.MCRORCIDClientHelper;
import org.mycore.orcid2.v3.client.MCRORCIDSectionImpl;
import org.mycore.orcid2.v3.work.MCRORCIDWorkUtils;
import org.orcid.jaxb.model.v3.release.record.Work;
import org.orcid.jaxb.model.v3.release.record.summary.WorkGroup;
import org.orcid.jaxb.model.v3.release.record.summary.WorkSummary;
import org.orcid.jaxb.model.v3.release.record.summary.Works;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class Orcid2WorksTransformer extends MCRContentTransformer implements Comparator<WorkSummary> {

    protected Logger LOGGER = LogManager.getLogger(Orcid2WorksTransformer.class);

    public MCRJDOMContent transform(MCRContent source) throws IOException {
        List<Work> workList = new ArrayList<>();

        Arrays.stream(source.asString().split("\\s"))
            .filter(orcid -> orcid.trim().length() > 0)
            .forEach(orcid -> {
                try {
                    Works works = MCRORCIDClientHelper
                        .getClientFactory()
                        .createReadClient()
                        .fetch(orcid, MCRORCIDSectionImpl.WORKS, Works.class);

                    works.getWorkGroup()
                        .stream()
                        .map(WorkGroup::getWorkSummary)
                        .filter(wg -> wg.size() > 0)
                        .forEach(workSummaries -> {
                            workSummaries.sort(this);
                            WorkSummary workSummary = workSummaries.get(0);

                            Work work = MCRORCIDClientHelper
                                .getClientFactory()
                                .createReadClient()
                                .fetch(orcid, MCRORCIDSectionImpl.WORK, Work.class, workSummary.getPutCode());

                            workList.add(work);
                        });
                } catch (MCRORCIDRequestException e) {
                    LOGGER.warn("Could not get works for ORCID {}. {}", orcid, e.getMessage());
                }
            });

        List<Element> workElements = MCRORCIDWorkUtils.buildUnmergedMODSFromWorks(workList);
        Element modsCollection = MCRORCIDUtils.buildMODSCollection(workElements);

        return new MCRJDOMContent(modsCollection);
    }

    @Override
    public int compare(WorkSummary ws1, WorkSummary ws2) {
        int v = 0;
        try {
            v = Integer.parseInt(ws1.getDisplayIndex()) - Integer.parseInt(
                ws2.getDisplayIndex());
        } catch (Exception e) {
            LOGGER.error("Could not compare WorkSummaries", e);
        }
        return v;
    }
}
