package org.mycore.ubo.modsperson;

import org.jdom2.Element;
import org.mycore.common.MCRConstants;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.datamodel.metadata.MCRObjectID;

public class MODSPersonUtils {

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
    }

    public static Element getMODSPersonTemplate() {
        return PERSON_TEMPLATE;
    }
}
