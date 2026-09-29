package org.mycore.ubo.modsperson.linking;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRException;
import org.mycore.common.MCRPersistenceException;
import org.mycore.common.events.MCREvent;
import org.mycore.common.events.MCREventHandler;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.mods.MCRMODSWrapper;
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUserAttribute;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.mycore.common.MCRConstants.MODS_NAMESPACE;

/**
 * For each change in an {@link MCRUser}, checks if the user is an authenticated ORCID-user AND
 * if the user is linked to a modsperson (owns the attribute "id_modsperson"). If so, and if
 * the authenticated ORCID-ID is not yet known to the modsperson, copy it over from the user.
 */
public class MODSPersonORCIDLinkingEventHandler implements MCREventHandler {

    private final static Logger LOGGER = LogManager.getLogger(MODSPersonORCIDLinkingEventHandler.class);

    private final static String ID_MODSPERSON = "id_modsperson";
    private final static String ORCID_PREFIX = "orcid_credential_";
    private final static String ORCID_TYPE = "orcid";
    private final static String TYPE_ATTRIBUTE = "type";

    private enum Action {ADD, REMOVE}

    @Override
    public void doHandleEvent(MCREvent mcrEvent) throws MCRException {
        processUserEvent(mcrEvent, Action.ADD);
    }

    @Override
    public void undoHandleEvent(MCREvent mcrEvent) throws MCRException {
        processUserEvent(mcrEvent, Action.REMOVE);
    }

    private void processUserEvent(MCREvent mcrEvent, Action action) {
        if (MCREvent.ObjectType.USER != mcrEvent.getObjectType()) {
            return;
        }
        MCRUser user = (MCRUser) mcrEvent.get(MCREvent.USER_KEY);
        if (user == null) {
            return;
        }
        Set<MCRUserAttribute> attributes = user.getAttributes();

        String modspersonId = attributes.stream()
            .filter(a -> ID_MODSPERSON.equals(a.getName()))
            .map(MCRUserAttribute::getValue)
            .findFirst()
            .orElse(null);
        if (modspersonId == null) {
            return;
        }

        List<String> orcidSuffixes = attributes.stream()
            .map(MCRUserAttribute::getName)
            .filter(n -> n.startsWith(ORCID_PREFIX))
            .map(n -> n.substring(ORCID_PREFIX.length()))
            .filter(s -> !s.isEmpty())
            .toList();
        if (orcidSuffixes.isEmpty()) {
            return;
        }

        switch (mcrEvent.getEventType()) {
            case CREATE, UPDATE -> {
                modifyOrcidAttributes(orcidSuffixes, modspersonId, user.getUserName(), action);
            }
        }
    }

    private void modifyOrcidAttributes(List<String> orcidSuffixes, String modspersonId,
        String userName, Action action) {
        try {
            MCRObject modsperson = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(modspersonId));
            MCRMODSWrapper wrapper = new MCRMODSWrapper(modsperson);
            Element personName = wrapper.getElement("mods:name[@type='personal']");

            if (Action.ADD.equals(action)) {
                Set<String> existing = personName.getChildren("nameIdentifier", MODS_NAMESPACE).stream()
                    .filter(e -> ORCID_TYPE.equals(e.getAttributeValue(TYPE_ATTRIBUTE)))
                    .map(Element::getText)
                    .collect(Collectors.toSet());

                for (String orcidSuffix : orcidSuffixes) {
                    if (!existing.contains(orcidSuffix)) {
                        personName.addContent(new Element("nameIdentifier", MODS_NAMESPACE)
                            .setAttribute(TYPE_ATTRIBUTE, ORCID_TYPE).setText(orcidSuffix));
                        LOGGER.info("Adding the orcid-ID {} to person {}", orcidSuffix, modspersonId);
                    } else {
                        LOGGER.debug("ORCID {} already present for person {}, skipping", orcidSuffix, modspersonId);
                    }
                }

            } else if (Action.REMOVE.equals(action)) {
                for (String orcidSuffix : orcidSuffixes) {
                    personName.getChildren("nameIdentifier", MODS_NAMESPACE).stream()
                        .filter(e -> ORCID_TYPE.equals(e.getAttributeValue(TYPE_ATTRIBUTE))
                            && orcidSuffix.equals(e.getText()))
                        .findFirst()
                        .ifPresent(personName::removeContent);
                }
            }

            if (!orcidSuffixes.isEmpty()) {
                MCRMetadataManager.update(modsperson);
            }
        } catch (MCRPersistenceException e) {
            doPersistanceErrorLog(e, modspersonId, userName);
        } catch (MCRAccessException e) {
            doAccessErrorLog(e, modspersonId);
        }
    }

    private void doPersistanceErrorLog(MCRPersistenceException e, String modspersonId, String userName) {
        LOGGER.warn("Modsperson with id {} is referenced by user {}, but not found: "
            , modspersonId, userName, e);
    }

    private void doAccessErrorLog(MCRAccessException e, String modspersonId) {
        LOGGER.warn("Couldn't access Modsperson with id {}: "
            , modspersonId, e);
    }

}
