/**
 * Copyright (c) 2016 Duisburg-Essen University Library
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package org.mycore.ubo.basket;

import java.util.List;

import org.jdom2.Element;
import org.mycore.common.MCRConstants;
import org.mycore.common.config.MCRConfiguration2;

/**
 * Represents a name entry of a modsName.
 * These name entries are edited using edit-contributors.xml.
 * 
 * Each NameEntry has a key built from name and pid data to find equal
 * names in a list of bibliography entries. NameEntry can also count 
 * the number of occurrences.
 * 
 * @author Frank L\u00FCtzenkirchen
 */
class NameEntry implements Comparable<NameEntry> {
    private static final String LEAD_ID = MCRConfiguration2.getStringOrThrow("MCR.user2.matching.lead_id");
    /** The number of occurrences of this name */
    private int num = 1;

    /** A key to find equal entries built from name and pid */
    private String key;

    /** The modsName XML element that is the source of this NameEntry */
    private Element modsName;

    /** Builds a new NameEntry object from the given modsName XML element */
    NameEntry(Element modsName) {
        this.modsName = modsName;
        buildKey();
    }

    /**
     * Builds a key to find equal entries. The key contains 
     * the name and pid data.
     */
    private void buildKey() {
        String lastName = getNameValue("namePart", "family");
        String firstName = getNameValue("namePart", "given");
        String pid = getNameValue("nameIdentifier", LEAD_ID);

        StringBuffer sb = new StringBuffer(lastName);
        if ((firstName != null) && !firstName.isEmpty())
            sb.append(", ").append(firstName);
        if ((pid != null) && !pid.isEmpty())
            sb.append(" [").append(pid).append("]");

        this.key = sb.toString();
    }

    private String getNameValue(String elementName, String type) {
        List<Element> nameParts = getModsName().getChildren(elementName, MCRConstants.MODS_NAMESPACE);
        for (Element namePart : nameParts)
            if (type.equals(namePart.getAttributeValue("type")))
                return namePart.getTextTrim();
        return null;
    }

    /**
     * Returns a key that contains name and pid data, and
     * allows to find similar name entries.
     */
    String getKey() {
        return key;
    }

    /**
     * Returns the modsName element that is the source
     * of this NameEntry.
     */
    Element getModsName() {
        return modsName;
    }

    /**
     * Returns an XML element that contains all data of this entry
     * and that is used as input for a single row in edit-contributors.xml.
     */
    Element buildXML() {
        Element nameEntry = new Element("nameEntry");
        nameEntry.addContent((Element) (getModsName().clone()));
        nameEntry.setAttribute("num", String.valueOf(num));
        nameEntry.setAttribute("key", getKey());
        return nameEntry;
    }

    /**
     * Increments the counter for the number of occurrences of this entry. 
     */
    void count() {
        num++;
    }

    /**
     * Allows sorting of NameEntry objects in a list, sorted by
     * number of occurrences, descending (most frequent first).
     */
    public int compareTo(NameEntry other) {
        return other.num - this.num;
    }

    /**
     * Two NameEntry objects are regarded equal when their keys 
     * are equal.
     */
    public boolean equals(Object other) {
        return this.getKey().equals(((NameEntry) other).getKey());
    }

    public int hashCode() {
        return getKey().hashCode();
    }
}
