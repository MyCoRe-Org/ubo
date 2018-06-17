package org.mycore.orcid;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.mycore.common.MCRUsageException;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.mods.MCRMODSWrapper;
import org.mycore.orcid.user.MCRORCIDSession;
import org.mycore.orcid.user.MCRORCIDUser;
import org.mycore.orcid.works.MCRWork;
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUserManager;
import org.xml.sax.SAXException;

public class MCRORCIDSynchronizer {

    private static final Logger LOGGER = LogManager.getLogger(MCRORCIDSynchronizer.class);

    public static String getStatus(String objectID) throws JDOMException, IOException, SAXException {
        MCRORCIDUser user = MCRORCIDSession.getORCIDUser();
        if (!user.hasORCIDProfile()) {
            return "no_orcid_user";
        }

        MCRObjectID oid = MCRObjectID.getInstance(objectID);
        if (!MCRMetadataManager.exists(oid)) {
            throw new MCRUsageException("No  publication stored with ID " + oid);
        }

        MCRObject obj = MCRMetadataManager.retrieveMCRObject(oid);
        MCRMODSWrapper wrapper = new MCRMODSWrapper(obj);

        if (!isMyPublication(wrapper)) {
            return "not_mine";
        }

        MCRORCIDProfile profile = user.getORCIDProfile();
        if (profile.getWorksSection().containsWork(oid)) {
            return "in_my_orcid_profile";
        }

        for (MCRWork work : profile.getWorksSection().getWorks()) {
            Element mods = work.getMODS().clone();
            MCRMODSWrapper workWrapper = new MCRMODSWrapper();
            workWrapper.setMODS(mods);
            List<Element> identifiers = workWrapper.getElements("mods:identifier");
            for (Element identifier : identifiers) {
                LOGGER.info(
                    "publication identifier " + identifier.getAttributeValue("type") + ":" + identifier.getTextTrim());
            }
        }
        return "not_in_my_orcid_profile";
    }

    private static boolean isMyPublication(MCRMODSWrapper wrapper) {
        Set<String> nameIdentifierKeys = getNameIdentifierKeysOfPublication(wrapper);
        Set<String> userIdentifierKeys = getIdentifierKeysOfUser(MCRUserManager.getCurrentUser());
        nameIdentifierKeys.retainAll(userIdentifierKeys);
        if (!nameIdentifierKeys.isEmpty()) {
            for (String key : nameIdentifierKeys) {
                LOGGER.info("user's identifier occurs in publication: " + key);
            }
        }
        return !nameIdentifierKeys.isEmpty();
    }

    private static Set<String> getNameIdentifierKeysOfPublication(MCRMODSWrapper wrapper) {
        Set<String> identifierKeys = new HashSet<String>();

        List<Element> nameIdentifiers = wrapper.getElements("mods:name/mods:nameIdentifier");
        for (Element nameIdentifier : nameIdentifiers) {
            String key = nameIdentifier.getAttributeValue("type") + ":" + nameIdentifier.getTextTrim();
            LOGGER.info("found name identifier in publication: " + key);
            identifierKeys.add(key);
        }
        return identifierKeys;
    }

    private static Set<String> getIdentifierKeysOfUser(MCRUser user) {
        Set<String> identifierKeys = new HashSet<String>();
        for (String attribute : user.getAttributes().keySet()) {
            String key = attribute.toLowerCase() + ":" + user.getUserAttribute(attribute);
            LOGGER.info("user has name identifier: " + key);
            identifierKeys.add(key);
        }
        return identifierKeys;
    }
}
