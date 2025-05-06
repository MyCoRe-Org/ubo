package org.mycore.ubo.modsperson;

import java.io.IOException;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRConstants;
import org.mycore.common.MCRException;
import org.mycore.common.MCRPersistenceException;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.events.MCREvent;
import org.mycore.common.events.MCREventHandlerBase;
import org.mycore.common.xml.MCRXMLHelper;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.mods.MCRMODSSorter;
import org.mycore.mods.MCRMODSWrapper;
import org.mycore.mods.merger.MCRMergeTool;
import org.mycore.mods.merger.MCRMerger;
import org.mycore.mods.merger.MCRMergerFactory;
import org.mycore.ubo.modsperson.merger.MCRNameMerger;

/**
 * EventHandler to connect personal information in mods objects to the corresponding information in modsperson
 * objects.
 */
public class MODSPersonLinkingEventHandler extends MCREventHandlerBase {

    private final static Logger LOGGER = LogManager.getLogger();

    private final static Element PERSON_TEMPLATE;

    private String leadIDName;

    static {
        String projectID = MCRConfiguration2.getStringOrThrow("UBO.projectid.default");
        String defaultID = MCRObjectID.formatID(projectID, "modsperson", 0);

        Element modsName = new Element("name", MCRConstants.MODS_NAMESPACE).setAttribute("type", "personal");
        Element mods = new Element("mods", MCRConstants.MODS_NAMESPACE).addContent(modsName);
        Element mc = new Element("modsContainer").addContent(mods);
        Element dmc = new Element("def.modsContainer").setAttribute("class", "MCRMetaXML").addContent(mc);

        Element mycoreobject = new Element("mycoreobject");
        mycoreobject.setAttribute("ID", defaultID);
        mycoreobject.setAttribute("noNamespaceSchemaLocation", "datamodel-modsperson.xsd", MCRConstants.XSI_NAMESPACE);
        mycoreobject.addContent(new Element("structure"));
        mycoreobject.addContent(new Element("metadata").addContent(dmc));
        mycoreobject.addContent(new Element("service"));

        PERSON_TEMPLATE = mycoreobject;
    }

    public MODSPersonLinkingEventHandler() {
        super();
        this.leadIDName = MCRConfiguration2.getString("MCR.user2.matching.lead_id").orElse("");
    }

    @Override
    protected void handleObjectCreated(MCREvent evt, MCRObject obj) {
        handleObject(obj);
    }

    @Override
    protected void handleObjectUpdated(MCREvent evt, MCRObject obj) {
        handleObject(obj);
    }

    @Override
    protected void handleObjectRepaired(MCREvent evt, MCRObject obj) {
        boolean wasModified = handleObject(obj);
        if (wasModified) {
            MCRXMLMetadataManager.instance().update(obj.getId(), obj.createXML(), new Date());
        }
    }

    protected boolean handleObject(MCRObject obj) {
        if (!"mods".equals(obj.getId().getTypeId())) {
            return false;
        }

        MCRMODSWrapper wrapper = new MCRMODSWrapper(obj);
        final String publicationId = obj.getId().toString();
        wrapper.getElements("mods:name[@type='personal']").forEach(modsName -> {

            MCRObject person = getPersonReferencedIn(modsName);
            if (person == null) {
                person = findPersonMatching(modsName, publicationId);
            }

            boolean isNewPerson = false;
            if ((person == null) && leadIDExists(modsName)) {
                person = buildNewPerson();
                isNewPerson = true;
            }

            if (person != null) {
                mergeDataFromNameToPerson(modsName, person);
                setReferencedPerson(modsName, person);
                if (isNewPerson) {
                    MODSPersonLookup.add(person);
                }
            }
        });

        return wasModified(wrapper.getMODS());
    }

    private boolean wasModified(Element mods) {
        boolean modified = mods.removeAttribute("modified");
        LOGGER.debug("Publication was modified? " + modified);
        return modified;
    }

    private MCRObject getPersonReferencedIn(Element modsName) {
        String personID = modsName.getAttributeValue("href", MCRConstants.XLINK_NAMESPACE);
        if (!StringUtils.isEmpty(personID)) {
            LOGGER.debug("Retrieving person object already referenced in publication: " + personID);
            MCRObjectID oid = MCRObjectID.getInstance(personID);

            try {
                return MCRMetadataManager.retrieveMCRObject(oid);
            } catch (MCRPersistenceException ex) {
                LOGGER.warn("Modsperson object " + personID + " not found, remove reference link: " + ex.getMessage());
                modsName.removeAttribute("href", MCRConstants.XLINK_NAMESPACE);
            }
        }
        return null;
    }

    private void setReferencedPerson(Element modsName, MCRObject person) {
        if (modsName.getAttribute("href", MCRConstants.XLINK_NAMESPACE) == null) {
            String personID = person.getId().toString();
            LOGGER.info("Linking mods:name in publication to " + personID);
            modsName.setAttribute("href", personID, MCRConstants.XLINK_NAMESPACE);
            modsName.getParentElement().setAttribute("modified", "true");
            MCRMODSSorter.sort(modsName);
        }
    }

    private MCRObject findPersonMatching(Element modsName, String publicationID) {
        Set<MODSPersonLookup.PersonCache> cachedPersons = MODSPersonLookup.lookup(modsName);
        if (cachedPersons == null || cachedPersons.isEmpty()) {
            return null;
        }
        MODSPersonLookup.PersonCache firstMatch = cachedPersons.iterator().next();
        if (cachedPersons.size() > 1) {
            String allIDs = cachedPersons.stream()
                .map(o -> o.getPersonmodsId().toString()).collect(Collectors.joining(", "));

            LOGGER.warn("There are multiple modsperson-objects matching the person in publication "
                + publicationID + ": ["+ allIDs +"]. Chosing " + firstMatch.getPersonmodsId().toString() + ".");
        }
        return MCRMetadataManager.retrieveMCRObject(firstMatch.getPersonmodsId());
    }

    private boolean leadIDExists(Element modsName) {
        return modsName.getChildren("nameIdentifier", MCRConstants.MODS_NAMESPACE)
            .stream().anyMatch(ni -> leadIDName.equals(ni.getAttributeValue("type")));
    }

    private MCRObject buildNewPerson() {
        LOGGER.debug("Creating new person object...");
        return new MCRObject(new Document(PERSON_TEMPLATE.clone()));
    }

    /**
     * Writes additional data from mods name object into the modsperson object.
     * @param modsName the {@link Element} which contains information about the person
     * @param person the modsperson object that should be enriched with additional data
     */
    private void mergeDataFromNameToPerson(Element modsName, MCRObject person) {
        LOGGER.info("Merging data from mods:name in publication to person object...");

        MCRMODSWrapper wrapper = new MCRMODSWrapper(person);

        Element personName = wrapper.getElement("mods:name[@type='personal']");
        if (personName != null) {
            Element personOld = personName.clone();

            MCRMergeTool.merge(personName, filterMODS(modsName));

            // build an alternativeName, if names are not exactly the same
            MCRMerger mergeInto = MCRMergerFactory.buildFrom(personName);
            MCRMerger mergeFrom = MCRMergerFactory.buildFrom(modsName);
            if (mergeInto.isProbablySameAs(mergeFrom)) {
                ((MCRNameMerger) mergeInto).mergeAsAlternativeName(mergeFrom);
            }

            MCRMODSSorter.sort(wrapper.getMODS());
            MCRMODSSorter.sort(personName);

            if (!MCRXMLHelper.deepEqual(personName, personOld)) {
                LOGGER.info("Person object is changed after merge, saving...");
                try {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(new MCRJDOMContent(personName.clone()).asString());
                    }
                    MCRMetadataManager.update(person);
                } catch (MCRPersistenceException | MCRAccessException | IOException ex) {
                    throw new MCRException(ex);
                }
            } else {
                LOGGER.info("Person object is unchanged after merge, nothing to do.");
            }
        }
    }

    private Element filterMODS(Element modsName) {
        Element cloned = modsName.clone();
        cloned.removeAttribute("href", MCRConstants.XLINK_NAMESPACE);
        cloned.removeChildren("displayForm", MCRConstants.MODS_NAMESPACE);
        cloned.removeChildren("alternativeName", MCRConstants.MODS_NAMESPACE);
        cloned.removeChildren("role", MCRConstants.MODS_NAMESPACE);
        cloned.removeChildren("description", MCRConstants.MODS_NAMESPACE);
        cloned.removeChildren("etal", MCRConstants.MODS_NAMESPACE);
        return cloned;
    }
}
