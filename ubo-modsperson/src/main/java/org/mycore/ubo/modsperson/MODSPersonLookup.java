package org.mycore.ubo.modsperson;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.compress.utils.Lists;
import org.jdom2.Element;
import org.mycore.common.MCRConstants;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.mods.MCRMODSWrapper;
import org.mycore.mods.merger.MCRMerger;
import org.mycore.mods.merger.MCRMergerFactory;

public class MODSPersonLookup {

    private static Map<String, Set<PersonCache>> nameId2person = new HashMap<>();

    private static Map<String, PersonCache> id2person = new HashMap<>();

    public static void add(MCRObject person) {
        Element mods = new MCRMODSWrapper(person).getMODS().clone();
        PersonCache personCache = PersonCache.newPersonCacheFromMCRObject(person);
        id2person.put(person.getId().toString(), personCache);
        getNameIdentifiers(mods).forEach(nameID -> {
            String key = buildKey(nameID);
            nameId2person.computeIfAbsent(key, k -> new HashSet<>()).add(personCache);
        });
    }

    public static void remove(MCRObject person) {
        Element mods = new MCRMODSWrapper(person).getMODS().clone();
        id2person.remove(person.getId().toString());

        getNameIdentifiers(mods).forEach(nameID -> {
            String key = buildKey(nameID);
            Set<PersonCache> personSet = nameId2person.get(key);
            if (personSet != null) {
                personSet.removeIf(testedPerson -> testedPerson.getPersonmodsId().equals(person.getId()));
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
        return String.join("|", nameIdentifier.getTextTrim(), nameIdentifier.getAttributeValue("type"));
    }

    private static List<Element> getNameIdentifiers(Element mods) {
        return mods.getChild("name", MCRConstants.MODS_NAMESPACE).getChildren("nameIdentifier",
            MCRConstants.MODS_NAMESPACE);
    }

    public static Set<PersonCache> lookup(Element modsName) {
        Set<String> keys2lookup = modsName.getChildren("nameIdentifier", MCRConstants.MODS_NAMESPACE)
            .stream().map(MODSPersonLookup::buildKey).collect(Collectors.toSet());

        keys2lookup.retainAll(nameId2person.keySet());

        keys2lookup.removeIf(key -> nameId2person.get(key) == null || nameId2person.get(key).stream()
            .filter(savedPerson -> savedPerson == null || !hasSameNames(modsName, savedPerson))
            .count() == nameId2person.get(key).size());
        return keys2lookup.isEmpty() ? null : nameId2person.get(keys2lookup.iterator().next());
    }

    private static boolean hasSameNames(Element elementToCompare, PersonCache cache) {
        Element savedElement = new Element("name", MCRConstants.MODS_NAMESPACE);
        savedElement.setAttribute("type", "personal");

        Element familyNameElement = new Element("namePart", MCRConstants.MODS_NAMESPACE);
        familyNameElement.setAttribute("type", "family");
        familyNameElement.setText(cache.getFamilyName());

        Element givenNameElement = new Element("namePart", MCRConstants.MODS_NAMESPACE);
        givenNameElement.setAttribute("type", "given");
        givenNameElement.setText(cache.getGivenName());

        savedElement.setContent(Arrays.asList(familyNameElement,givenNameElement));

        MCRMerger merger1 = MCRMergerFactory.buildFrom(elementToCompare);
        MCRMerger merger2 = MCRMergerFactory.buildFrom(savedElement);
        return merger1.isProbablySameAs(merger2);
    }

    public static class PersonCache {

        public PersonCache(MCRObjectID personmodsId, String familyName, String givenName, Set<String> keys) {
            this.personmodsId = personmodsId;
            this.familyName = familyName;
            this.givenName = givenName;
            this.keys = keys;
        }

        private MCRObjectID personmodsId;
        private String familyName;
        private String givenName;
        private Set<String> keys;

        public static PersonCache newPersonCacheFromMCRObject(MCRObject obj) {
            MCRMODSWrapper wrapper = new MCRMODSWrapper(obj);
            return newPersonCacheFromModsNameElement(wrapper.getMODS(), obj.getId());
        }

        public static PersonCache newPersonCacheFromModsNameElement(Element modsName, MCRObjectID personmodsId) {
            List<Element> familyNames = modsName.getChild("name", MCRConstants.MODS_NAMESPACE).getChildren()
                .stream().filter(e -> "family".equals(e.getAttributeValue("type"))).toList();
            String familyNameString = "";
            if (!familyNames.isEmpty()) {
                familyNameString = familyNames.get(0).getText();
            }

            List<Element> givenNames = modsName.getChild("name", MCRConstants.MODS_NAMESPACE).getChildren()
                .stream().filter(e -> "given".equals(e.getAttributeValue("type"))).toList();
            String givenNameString = "";
            if (!givenNames.isEmpty()) {
                givenNameString = givenNames.get(0).getText();
            }

            Set<String> allKeys = modsName.getChild("name", MCRConstants.MODS_NAMESPACE)
                .getChildren("nameIdentifier", MCRConstants.MODS_NAMESPACE)
                .stream().map(MODSPersonLookup::buildKey).collect(Collectors.toSet());

            return new PersonCache(personmodsId, familyNameString, givenNameString, allKeys);
        }

        public MCRObjectID getPersonmodsId() {
            return personmodsId;
        }

        public void setPersonmodsId(MCRObjectID personmodsId) {
            this.personmodsId = personmodsId;
        }

        public String getFamilyName() {
            return familyName;
        }

        public void setFamilyName(String familyName) {
            this.familyName = familyName;
        }

        public String getGivenName() {
            return givenName;
        }

        public void setGivenName(String givenName) {
            this.givenName = givenName;
        }

        public Set<String> getKeys() {
            return keys;
        }

        public void setKeys(Set<String> keys) {
            this.keys = keys;
        }

        public void addKey(String key) {
            this.keys.add(key);
        }
    }

}
