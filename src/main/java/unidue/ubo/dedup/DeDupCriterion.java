/**
 * $Revision: 34413 $ 
 * $Date: 2016-01-26 15:11:20 +0100 (Di, 26 Jan 2016) $
 *
 * This file is part of the MILESS repository software.
 * Copyright (C) 2011 MILESS/MyCoRe developer team
 * See http://duepublico.uni-duisburg-essen.de/ and http://www.mycore.de/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
