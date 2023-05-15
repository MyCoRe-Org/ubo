package org.mycore.ubo.orcid;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.mycore.common.MCRSessionMgr;
import org.mycore.common.xml.MCRXMLFunctions;
import org.mycore.orcid2.client.MCRORCIDCredential;
import org.mycore.orcid2.user.MCRORCIDSessionUtils;
import org.mycore.orcid2.user.MCRORCIDUser;
import org.mycore.orcid2.v3.MCRORCIDClientHelper;
import org.mycore.orcid2.v3.MCRORCIDSectionImpl;
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUserManager;
import org.orcid.jaxb.model.v3.release.record.summary.Works;

public class DozBibORCIDUtils {

    public static int getNumWorks() {
        MCRORCIDUser orcidUser = MCRORCIDSessionUtils.getCurrentUser();
        Set<String> orcidIdentifiers = orcidUser.getORCIDs();

        AtomicInteger numWorks = new AtomicInteger(0);

        orcidIdentifiers.forEach(orcid -> {
            Works works = MCRORCIDClientHelper.getClientFactory()
                .createUserClient(orcid, orcidUser.getCredentials().values().stream().findFirst().get()).
                fetch(MCRORCIDSectionImpl.WORKS, Works.class);
            numWorks.addAndGet(works.getWorkGroup().size());
        });

        return numWorks.get();
    }

    public static boolean weAreTrustedParty() {
        if (MCRXMLFunctions.isCurrentUserGuestUser()) {
            return false;
        }

        MCRORCIDUser orcidUser = MCRORCIDSessionUtils.getCurrentUser();
        Map<String, MCRORCIDCredential> credentials = orcidUser.getCredentials();

        return !credentials.isEmpty();
    }

    public static boolean hasSyncEnabled() {
        MCRUser user = MCRUserManager.getUser(MCRSessionMgr.getCurrentSession().getUserInformation().getUserID());
        return user.getAttributes().stream()
            .filter(a -> a.getName().equals("orcid_sync") && a.getValue().equals(String.valueOf(true)))
            .findFirst()
            .isPresent();
    }
}
