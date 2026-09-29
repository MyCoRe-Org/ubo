package org.mycore.ubo.modsperson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jdom2.Element;
import org.mycore.common.MCRConstants;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.mods.MCRMODSWrapper;

import static org.mycore.ubo.modsperson.MODSPersonUtils.hasSameNames;

public class MODSPersonLookup {

    private static final Map<String, Set<PersonCache>> nameId2person = new HashMap<>();

    private static final Map<String, PersonCache> id2person = new HashMap<>();

    public static void add(MCRObject person) {
        Element mods = new MCRMODSWrapper(person).getMODS().clone();
        PersonCache personCache = PersonCache.newPersonCacheFromMCRObject(person);
        if (personCache != null) {
            id2person.put(person.getId().toString(), personCache);
            getNameIdentifiers(mods).forEach(nameID -> {
                String key = buildKey(nameID);
                nameId2person.computeIfAbsent(key, k -> new HashSet<>()).add(personCache);
            });
        }
    }

    public static void add(PersonCache personCache) {
        id2person.put(personCache.getPersonmodsId().toString(), personCache);
        personCache.getKeys().forEach(key -> {
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

    /**
     * Fully empties the lookup. Use with caution!
     */
    public static void clear() {
        id2person.clear();
        nameId2person.clear();
    }

    public static void update(MCRObject person) {
        remove(person);
        add(person);
    }

    private static String buildKey(Element nameIdentifier) {
        return String.join("|", nameIdentifier.getTextTrim(), nameIdentifier.getAttributeValue("type"));
    }

    private static List<Element> getNameIdentifiers(Element mods) {
        if (mods != null && mods.getChild("name", MCRConstants.MODS_NAMESPACE) != null) {
            return mods.getChild("name", MCRConstants.MODS_NAMESPACE).getChildren("nameIdentifier",
                MCRConstants.MODS_NAMESPACE);
        }
        return new ArrayList<>();
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

    /**
     * @return gets number of modsperson-objects in cache by modsperson-ID
     */
    public static int getCacheSize() {
        return id2person.size();
    }

    public static class PersonCache {

        public PersonCache(MCRObjectID personmodsId, String familyName, String givenName, Set<String> keys,
            Set<Map.Entry<String, String>> alternativeNames) {
            this.personmodsId = personmodsId;
            this.familyName = familyName;
            this.givenName = givenName;
            this.keys = keys;
            this.alternativeNames = alternativeNames;
        }

        private MCRObjectID personmodsId;
        private String familyName;
        private String givenName;
        private Set<String> keys;

        // in the form: Entry<familyName, givenName>
        private Set<Map.Entry<String, String>> alternativeNames;

        public static PersonCache newPersonCacheFromMCRObject(MCRObject obj) {
            MCRMODSWrapper wrapper = new MCRMODSWrapper(obj);
            Element modsName = wrapper.getMODS().getChild("name", MCRConstants.MODS_NAMESPACE);
            if (modsName == null) {
                return null;
            }

            String familyNameString = modsName
                .getChildren().stream().filter(e -> "family".equals(e.getAttributeValue("type")))
                .map(Element::getText).findFirst().orElse("").trim();

            String givenNameString = modsName
                .getChildren().stream().filter(e -> "given".equals(e.getAttributeValue("type")))
                .map(Element::getText).findFirst().orElse("").trim();

            Set<String> allKeys = modsName
                .getChildren("nameIdentifier", MCRConstants.MODS_NAMESPACE)
                .stream().map(MODSPersonLookup::buildKey).collect(Collectors.toSet());

            Set<Map.Entry<String, String>> alternativeNamesLocal = new HashSet<>();

            modsName.getChildren("alternativeName", MCRConstants.MODS_NAMESPACE).forEach(alternativeName -> {
                    String altFamilyNameLocal = alternativeName.getChildren().stream()
                        .filter(e -> "family".equals(e.getAttributeValue("type")))
                        .map(Element::getText).findFirst().orElse("");

                    String altGivenNameLocal = alternativeName.getChildren().stream()
                        .filter(e -> "given".equals(e.getAttributeValue("type")))
                        .map(Element::getText).findFirst().orElse("");


                    alternativeNamesLocal.add(Map.entry(altFamilyNameLocal, altGivenNameLocal));

                });

            return new PersonCache(obj.getId(), familyNameString, givenNameString, allKeys, alternativeNamesLocal);
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

        public Set<Map.Entry<String, String>> getAlternativeNames() {
            return alternativeNames;
        }

        public void setAlternativeNames(Set<Map.Entry<String, String>> alternativeNames) {
            this.alternativeNames = alternativeNames;
        }

        public void addAlternativeNames(Map.Entry<String, String> alternativeName) {
            alternativeNames.add(alternativeName);
        }
    }

}
