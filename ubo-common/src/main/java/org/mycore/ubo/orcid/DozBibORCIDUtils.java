package org.mycore.ubo.orcid;

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
import org.orcid.jaxb.model.v3.release.record.summary.Works;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class DozBibORCIDUtils {

    protected static final Logger LOGGER = LogManager.getLogger(DozBibORCIDUtils.class);

    /**
     * Get the total number of publications for all orcids connected with UBO.
     *
     * @return the total number of publications
     * */
    public static int getNumWorks() {
        MCRORCIDUser orcidUser;
        try {
            orcidUser = MCRORCIDSessionUtils.getCurrentUser();
        } catch (Exception ex) {
            LOGGER.error("Could not get numWorks for user {}",
                MCRSessionMgr.getCurrentSession().getUserInformation().getUserID(), ex);
            return 0;
        }

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
        MCRORCIDUser orcidUser;
        try {
            orcidUser = MCRORCIDSessionUtils.getCurrentUser();
        } catch (Exception ex) {
            LOGGER.error("Could not get numWorks for orcid {} of user {}", orcid,
                MCRSessionMgr.getCurrentSession().getUserInformation().getUserID(), ex);
            return 0;
        }

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
        MCRORCIDUser orcidUser;
        try {
            orcidUser = MCRORCIDSessionUtils.getCurrentUser();
        } catch (Exception ex) {
            String uid = MCRSessionMgr.getCurrentSession().getUserInformation().getUserID();
            LOGGER.error("Could not get first orcid for user {}", uid, ex);
            return "";
        }
        return orcidUser.getORCIDs().isEmpty() ? "" : orcidUser.getORCIDs().iterator().next();
    }

    public static boolean weAreTrustedParty() {
        if (MCRXMLFunctions.isCurrentUserGuestUser()) {
            return false;
        }
        Map<String, MCRORCIDCredential> credentials;
        try {
            MCRORCIDUser orcidUser = MCRORCIDSessionUtils.getCurrentUser();
            credentials = orcidUser.getCredentials();
        } catch (Exception e) {
            LOGGER.error("Could not determine if ubo instance is a trusted party", e);
            return false;
        }

        return !credentials.isEmpty();
    }

    public static boolean isConnected(String orcid) {
        return MCRORCIDSessionUtils.getCurrentUser().getCredentialByORCID(orcid) != null;
    }
}
