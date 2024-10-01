package org.mycore.ubo.modsperson;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jdom2.Element;
import org.mycore.common.MCRConstants;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.mods.MCRMODSWrapper;
import org.mycore.mods.merger.MCRMerger;
import org.mycore.mods.merger.MCRMergerFactory;

public class MODSPersonLookup {

    private static Map<String, Element> nameID2person = new HashMap<>();

    private static Map<String, Element> id2person = new HashMap<>();

    public static void add(MCRObject person) {
        Element mods = new MCRMODSWrapper(person).getMODS().clone();
        id2person.put(person.getId().toString(), mods);
        getNameIdentifiers(mods).forEach(nameID -> nameID2person.put(buildKey(nameID), mods));
    }

    public static void remove(MCRObject person) {
        Element mods = id2person.remove(person.getId().toString());
        getNameIdentifiers(mods).forEach(nameID -> nameID2person.remove(buildKey(nameID)));
    }

    public static void update(MCRObject person) {
        remove(person);
        add(person);
    }

    private static String buildKey(Element nameIdentifier) {
        String key = String.join("|", nameIdentifier.getTextTrim(), nameIdentifier.getAttributeValue("type"));
        return key;
    }

    private static List<Element> getNameIdentifiers(Element mods) {
        return mods.getChild("name", MCRConstants.MODS_NAMESPACE).getChildren("nameIdentifier",
            MCRConstants.MODS_NAMESPACE);
    }

    public static Element lookup(Element modsName) {
        Set<String> keys2lookup = modsName.getChildren("nameIdentifier", MCRConstants.MODS_NAMESPACE)
            .stream().map(MODSPersonLookup::buildKey).collect(Collectors.toSet());

        keys2lookup.retainAll(nameID2person.keySet());

        keys2lookup.removeIf(s -> {
            Element savedElement = nameID2person.get(s) != null
                                   ? nameID2person.get(s).getChild("name", MCRConstants.MODS_NAMESPACE).clone()
                                   : null;
            return savedElement == null || !hasSameNames(modsName, savedElement);
        });

        return keys2lookup.isEmpty() ? null : nameID2person.get(keys2lookup.iterator().next()).clone();
    }

    public static Element lookup(String identifier, String identifierType) {
        String key = String.join("|", identifier.trim(), identifierType.trim());
        return nameID2person.get(key);
    }

    private static boolean hasSameNames(Element element1, Element element2) {
        MCRMerger merger1 = MCRMergerFactory.buildFrom(element1);
        MCRMerger merger2 = MCRMergerFactory.buildFrom(element2);
        return merger1.isProbablySameAs(merger2);
    }

}
