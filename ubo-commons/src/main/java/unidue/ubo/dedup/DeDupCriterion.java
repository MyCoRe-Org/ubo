/**
 * Copyright (c) 2016 Duisburg-Essen University Library
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package unidue.ubo.dedup;

import org.apache.commons.codec.digest.DigestUtils;
import org.jdom2.Element;

/**
 * Represents a criterion to decide equality of publications.
 * When two publications result in equal DeDupCriterion, they may be duplicates.
 * 
 * @author Frank L\u00FCtzenkirchen
 */
public class DeDupCriterion {

    /** Indicates the type of criterion, e.g. title, identifier, shelfmark */
    private String type;

    /** The value of the criterion, e.g. a normalized title or identifier */
    private String value;

    /** A hash built over type and value. Two criteria are equal if their hash values are equal. */
    private String key;

    /** A flag which, during building a deduplication report, indicates that this criterion resulted in a match of possibly duplcate publications */
    private boolean usedInMatch = false;

    /**
     * Builds a new deduplication criterion.
     * 
     * @param type indicates the type of criterion, e.g. title, identifier, shelfmark
     * @param value the value of the criterion, e.g. a normalized title or identifier
     */
    public DeDupCriterion(String type, String value) {
        this.type = type;
        this.value = value;
        this.key = DigestUtils.md2Hex(type + ":" + value);
    }

    /**
     * Returns the type of criterion, e.g. title, identifier, shelfmark
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the value of the criterion, e.g. a normalized title or identifier
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns a hash key built over type and value of this criterion. 
     * Two criteria are equal if their hash values are equal
     */
    public String getKey() {
        return key;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof DeDupCriterion ? this.key.equals(((DeDupCriterion) other).key) : false;
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public String toString() {
        return key + " => " + type + ":\"" + value + "\"";
    }

    /**
     * Returns an XML representation of this criterion, used in deduplication reports
     */
    public Element toXML() {
        Element element = new Element("dedup");
        element.setText(value);
        element.setAttribute("key", key);
        element.setAttribute("type", type);
        return element;
    }

    /**
     * During building a deduplication report, indicates that this criterion resulted in a match of possibly duplcate publications
     */
    public boolean isUsedInMatch() {
        return usedInMatch;
    }

    /**
     * During building a deduplication report, marks that this criterion resulted in a match of possibly duplcate publications
     */
    public void markAsUsedInMatch() {
        this.usedInMatch = true;
    }
}
