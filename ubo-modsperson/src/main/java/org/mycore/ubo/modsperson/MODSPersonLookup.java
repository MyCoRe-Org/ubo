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

public class MODSPersonLookup {

    private Map<String, Element> nameID2person = new HashMap<String, Element>();

    private Map<String, Element> id2person = new HashMap<String, Element>();

    public void add(MCRObject person) {
        Element mods = new MCRMODSWrapper(person).getMODS().clone();
        id2person.put(person.getId().toString(), mods);
        getNameIdentifiers(mods).forEach(nameID -> nameID2person.put(buildKey(nameID), mods));
    }

    public void remove(MCRObject person) {
        Element mods = id2person.remove(person.getId().toString());
        getNameIdentifiers(mods).forEach(nameID -> nameID2person.remove(buildKey(nameID)));
    }

    public void update(MCRObject person) {
        remove(person);
        add(person);
    }

    private String buildKey(Element nameIdentifier) {
        String key = String.join("|", nameIdentifier.getTextTrim(), nameIdentifier.getAttributeValue("type"));
        return key;
    }

    private List<Element> getNameIdentifiers(Element mods) {
        return mods.getChild("name", MCRConstants.MODS_NAMESPACE).getChildren("nameIdentifier",
            MCRConstants.MODS_NAMESPACE);
    }

    public Element lookup(Element modsName) {
        Set<String> keys2lookup = modsName.getChildren("nameIdentifer", MCRConstants.MODS_NAMESPACE)
            .stream().map(nI -> buildKey(nI)).collect(Collectors.toSet());
        keys2lookup.retainAll(nameID2person.keySet());
        return keys2lookup.isEmpty() ? null : nameID2person.get(keys2lookup.iterator().next()).clone();
    }
}
