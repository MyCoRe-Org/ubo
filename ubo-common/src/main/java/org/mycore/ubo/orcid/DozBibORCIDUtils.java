package org.mycore.ubo.orcid;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.xml.MCRXMLFunctions;
import org.mycore.orcid2.client.MCRORCIDCredential;
import org.mycore.orcid2.client.MCRORCIDUserClient;
import org.mycore.orcid2.client.exception.MCRORCIDRequestException;
import org.mycore.orcid2.user.MCRORCIDSessionUtils;
import org.mycore.orcid2.user.MCRORCIDUser;
import org.mycore.orcid2.v3.client.MCRORCIDClientHelper;
import org.mycore.orcid2.v3.client.MCRORCIDSectionImpl;
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUserManager;
import org.orcid.jaxb.model.v3.release.record.summary.Works;

public class DozBibORCIDUtils {

    protected static final Logger LOGGER = LogManager.getLogger(DozBibORCIDUtils.class);

    /**
     * Get the total number of publications for all orcids connected with UBO.
     *
     * @return the total number of publications
     * */
    public static int getNumWorks() {
        MCRORCIDUser orcidUser = MCRORCIDSessionUtils.getCurrentUser();
        Set<String> orcidIdentifiers = orcidUser.getORCIDs();

        AtomicInteger numWorks = new AtomicInteger(0);

        orcidIdentifiers.forEach(orcid -> {
            MCRORCIDCredential credential = orcidUser.getCredentialByORCID(orcid);
            if (credential != null) {
                try {
                    MCRORCIDUserClient client = MCRORCIDClientHelper.getClientFactory()
                        .createUserClient(orcid, credential);
                    Works works = client.fetch(MCRORCIDSectionImpl.WORKS, Works.class);
                    numWorks.addAndGet(works.getWorkGroup().size());
                } catch (MCRORCIDRequestException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        });

        return numWorks.get();
    }

    /**
     * Returns the number of publications for the given orcid. The orcid must be connected with UBO otherwise 0
     * will be returned.
     *
     * @param orcid the orcid for which the number of publications will be retrieved
     *
     * @return the number of publications for the given orcid
     * */
    public static int getNumWorks(String orcid) {
        MCRORCIDUser orcidUser = MCRORCIDSessionUtils.getCurrentUser();
        MCRORCIDCredential credentialByORCID = orcidUser.getCredentialByORCID(orcid);

        MCRORCIDUserClient client = MCRORCIDClientHelper.getClientFactory().createUserClient(orcid, credentialByORCID);
        AtomicInteger numWorks = new AtomicInteger(0);

        try {
            Works works = client.fetch(MCRORCIDSectionImpl.WORKS, Works.class);
            numWorks.addAndGet(works.getWorkGroup().size());
        } catch (MCRORCIDRequestException e) {
            LOGGER.error(e.getMessage(), e);
        }

        return numWorks.get();
    }

    public static String getFirstOrcidByCurrentUser() {
        MCRORCIDUser orcidUser = MCRORCIDSessionUtils.getCurrentUser();
        return orcidUser.getORCIDs().isEmpty() ? "" : orcidUser.getORCIDs().iterator().next();
    }

    public static boolean weAreTrustedParty() {
        if (MCRXMLFunctions.isCurrentUserGuestUser()) {
            return false;
        }

        MCRORCIDUser orcidUser = MCRORCIDSessionUtils.getCurrentUser();
        Map<String, MCRORCIDCredential> credentials = orcidUser.getCredentials();

        return !credentials.isEmpty();
    }

    public static boolean isConnected(String orcid) {
        return MCRORCIDSessionUtils.getCurrentUser().getCredentialByORCID(orcid) != null;
    }

    public static boolean hasSyncEnabled() {
        MCRUser user = MCRUserManager.getUser(MCRSessionMgr.getCurrentSession().getUserInformation().getUserID());
        return user.getAttributes().stream()
            .filter(a -> a.getName().equals("orcid_update_profile") && a.getValue().equals(String.valueOf(true)))
            .findFirst()
            .isPresent();
    }
}
