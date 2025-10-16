package org.mycore.ubo.modsperson;

import org.jdom2.Element;
import org.mycore.common.MCRConstants;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.mods.merger.MCRMerger;
import org.mycore.mods.merger.MCRMergerFactory;
import org.mycore.ubo.modsperson.merger.MCRNameMerger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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

    /**
     * Compares a modsName Element with a given cached person for same names.
     * @param elementToCompare modsName Element to compare to person in cache
     * @param cache Element from cache that is compared
     * @return true if the names are the same or if the cache has an alternative name matching the elementToCompare
     */
    protected static boolean hasSameNames(Element elementToCompare, MODSPersonLookup.PersonCache cache) {
        Element savedElement = personCache2ModsName(cache);

        MCRMerger merger1 = MCRMergerFactory.buildFrom(elementToCompare);
        MCRMerger merger2 = MCRMergerFactory.buildFrom(savedElement);
        return merger1.isProbablySameAs(merger2) || ((MCRNameMerger) merger2).hasAlternativeNameSameAs(merger1);
    }

    /**
     * Creates a mods:name Element containing only names. Util method for name comparison. Element
     * contains family name, given name and potential alternative names.
     * @param cache the {@link MODSPersonLookup.PersonCache} to compare.
     * @return the Element with all names
     */
    protected static Element personCache2ModsName(MODSPersonLookup.PersonCache cache) {
        Element newModsName = new Element("name", MCRConstants.MODS_NAMESPACE);
        newModsName.setAttribute("type", "personal");

        List<Element> innerNameElements = new ArrayList<>();

        Element familyNameElement = new Element("namePart", MCRConstants.MODS_NAMESPACE);
        familyNameElement.setAttribute("type", "family");
        familyNameElement.setText(cache.getFamilyName());
        innerNameElements.add(familyNameElement);

        Element givenNameElement = new Element("namePart", MCRConstants.MODS_NAMESPACE);
        givenNameElement.setAttribute("type", "given");
        givenNameElement.setText(cache.getGivenName());
        innerNameElements.add(givenNameElement);


        for (Map.Entry<String, String> entry : cache.getAlternativeNames()) {
            Element altNameElement = new Element("alternativeName", MCRConstants.MODS_NAMESPACE);

            Element altFamilyNameElement = new Element("namePart", MCRConstants.MODS_NAMESPACE);
            altFamilyNameElement.setAttribute("type", "family");
            altFamilyNameElement.setText(entry.getKey());

            Element altGivenNameElement = new Element("namePart", MCRConstants.MODS_NAMESPACE);
            altGivenNameElement.setAttribute("type", "given");
            altGivenNameElement.setText(entry.getValue());

            altNameElement.setContent(Arrays.asList(altFamilyNameElement, altGivenNameElement));
            innerNameElements.add(altNameElement);
        }

        newModsName.setContent(innerNameElements);
        return  newModsName;
    }
}
