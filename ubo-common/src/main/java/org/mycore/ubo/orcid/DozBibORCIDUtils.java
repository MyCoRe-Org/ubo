package org.mycore.ubo.orcid;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.mycore.orcid2.user.MCRORCIDSessionUtils;
import org.mycore.orcid2.user.MCRORCIDUser;
import org.mycore.orcid2.v3.MCRORCIDClientHelper;
import org.mycore.orcid2.v3.MCRORCIDSectionImpl;
import org.orcid.jaxb.model.v3.release.record.summary.Works;

public class DozBibORCIDUtils {

    public static int getNumWorks() {
        MCRORCIDUser orcidUser = MCRORCIDSessionUtils.getCurrentUser();
        Set<String> orcidIdentifiers = orcidUser.getORCIDs();

        AtomicInteger numWorks = new AtomicInteger(0);

        orcidIdentifiers.forEach(next -> {
            Works works = MCRORCIDClientHelper.getClientFactory()
                .createUserClient(next, orcidUser.getCredentials().values().stream().findFirst().get()).
                fetch(MCRORCIDSectionImpl.WORKS, Works.class);
            numWorks.addAndGet(works.getWorkGroup().size());
        });

        return numWorks.get();
    }
}
