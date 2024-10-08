package org.mycore.ubo.modsperson;

import java.util.HashMap;
import java.util.HashSet;
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

    private static Map<String, Set<MCRObject>> nameId2person = new HashMap<>();

    private static Map<String, MCRObject> id2person = new HashMap<>();

    public static void add(MCRObject person) {
        Element mods = new MCRMODSWrapper(person).getMODS().clone();
        id2person.put(person.getId().toString(), person);
        getNameIdentifiers(mods).forEach(nameID -> {
            String key = buildKey(nameID);
            nameId2person.computeIfAbsent(key, k -> new HashSet<>()).add(person);
        });
    }

    public static void remove(MCRObject person) {
        Element mods = new MCRMODSWrapper(person).getMODS().clone();
        id2person.remove(person.getId().toString());

        getNameIdentifiers(mods).forEach(nameID -> {
            String key = buildKey(nameID);
            Set<MCRObject> personSet = nameId2person.get(key);
            if (personSet != null) {
                personSet.removeIf(testedPerson -> testedPerson.getId().equals(person.getId()));
                if (personSet.isEmpty()) {
                    nameId2person.remove(key);
                }
            }
        });
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

    public static Set<MCRObject> lookup(Element modsName) {
        Set<String> keys2lookup = modsName.getChildren("nameIdentifier", MCRConstants.MODS_NAMESPACE)
            .stream().map(MODSPersonLookup::buildKey).collect(Collectors.toSet());

        keys2lookup.retainAll(nameId2person.keySet());

        keys2lookup.removeIf(s -> nameId2person.get(s) == null || nameId2person.get(s).stream()
            .filter(obj -> {
                Element savedElement = new MCRMODSWrapper(obj).getMODS()
                    .getChild("name", MCRConstants.MODS_NAMESPACE);
                return savedElement == null || !hasSameNames(modsName, savedElement);
            }).count() == nameId2person.get(s).size());

        // TODO: If there are multiple objects, we need the one that has most IDs in common with keys2lookup
        return keys2lookup.isEmpty() ? null : nameId2person.get(keys2lookup.iterator().next());
    }

    private static boolean hasSameNames(Element element1, Element element2) {
        MCRMerger merger1 = MCRMergerFactory.buildFrom(element1);
        MCRMerger merger2 = MCRMergerFactory.buildFrom(element2);
        return merger1.isProbablySameAs(merger2);
    }

}
