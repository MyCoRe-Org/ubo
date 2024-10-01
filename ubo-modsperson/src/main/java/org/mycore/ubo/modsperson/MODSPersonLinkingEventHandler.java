package org.mycore.ubo.modsperson;

import java.io.IOException;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
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

// TODO: Documentation
// TODO: Wait for SOLR commit in batch mode? Or cache IDs in batch mode?

public class MODSPersonLinkingEventHandler extends MCREventHandlerBase {

    private final static Logger LOGGER = LogManager.getLogger();

    private final static Element PERSON_TEMPLATE;

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
        System.out.println(new XMLOutputter(Format.getPrettyFormat()).outputString(mycoreobject));
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
        wrapper.getElements("mods:name[@type='personal']").forEach(modsName -> {

            MCRObject person = getPersonReferencedIn(modsName);
            if (person == null) {
                person = findPersonMatching(modsName);
            }
            if ((person == null) && leadIDExists(modsName)) {
                person = buildNewPerson();
            }

            if (person != null) {
                mergeDataFromNameToPerson(modsName, person);
                setReferencedPerson(modsName, person);
                mergeDataFromPersonToName(modsName, person);
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
        if (StringUtils.isEmpty(personID)) {
            return null;
        } else {
            LOGGER.debug("Retrieving person object already referenced in publication: " + personID);
            MCRObjectID oid = MCRObjectID.getInstance(personID);
            return MCRMetadataManager.retrieveMCRObject(oid);
        }
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

    private MCRObject findPersonMatching(Element modsName) {
        Element personElement = MODSPersonLookup.lookup(modsName);
        if (personElement == null) {
            return null;
        }
        MCRObject obj = new MCRObject(new Document(PERSON_TEMPLATE.clone()));
        MCRMODSWrapper wrapper = new MCRMODSWrapper(obj);
        wrapper.setMODS(personElement);
        return obj;
    }

    private boolean leadIDExists(Element modsName) {
        return modsName.getChildren("nameIdentifier", MCRConstants.MODS_NAMESPACE)
            .stream().anyMatch(ni -> "lsf".equals(ni.getAttributeValue("type")));
    }

    private MCRObject buildNewPerson() {
        LOGGER.debug("Creating new person object...");
        return new MCRObject(new Document(PERSON_TEMPLATE.clone()));
    }

    private void mergeDataFromNameToPerson(Element modsName, MCRObject person) {
        LOGGER.info("Merging data from mods:name in publication to person object...");

        MCRMODSWrapper wrapper = new MCRMODSWrapper(person);

        Element personName = wrapper.getElement("mods:name[@type='personal']");
        if (personName != null) {
            Element personOld = personName.clone();

            MCRMergeTool.merge(personName, filterMODS(modsName));

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

    private void mergeDataFromPersonToName(Element modsName, MCRObject person) {
        LOGGER.info("Merging data from person object into mods:name in publication...");
        MCRMODSWrapper wrapper = new MCRMODSWrapper(person);
        Element personNameElement = wrapper.getMODS() != null
                                    ? wrapper.getMODS().getChild("name", MCRConstants.MODS_NAMESPACE)
                                    : null;
        if (personNameElement != null) {
            MCRMergeTool.merge(modsName, personNameElement);
        }
    }
}
