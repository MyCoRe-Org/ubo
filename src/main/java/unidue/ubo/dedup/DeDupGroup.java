/**
 * Copyright (c) 2016 Duisburg-Essen University Library
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package unidue.ubo.dedup;

import java.util.HashSet;
import java.util.Set;

import org.jdom2.Element;

import unidue.ubo.DozBibManager;

class DeDupGroup implements Comparable<DeDupGroup> {

    private Set<DeDupCriterion> criteria = new HashSet<DeDupCriterion>();

    private Set<Integer> IDs = new HashSet<Integer>();

    public DeDupGroup(Set<DeDupCriterion> criteria, int id) {
        this.criteria.addAll(criteria);
        this.IDs.add(id);
    }

    public Set<DeDupCriterion> getCriteria() {
        return criteria;
    }

    public Set<Integer> getIDs() {
        return IDs;
    }

    public void assimilate(DeDupGroup other) {
        IDs.addAll(other.IDs);
        criteria.addAll(other.criteria);
    }

    @Override
    public int compareTo(DeDupGroup other) {
        return this.IDs.size() - other.IDs.size();
    }

    public Element buildXML() {
        Element group = new Element("group");

        for (DeDupCriterion criterion : criteria)
            if (criterion.isUsedInMatch())
                group.addContent(criterion.toXML());

        for (Integer id : IDs)
            group.addContent(new Element("id").setText(DozBibManager.buildMCRObjectID(id).toString()));

        return group;
    }

    public String listIDs() {
        StringBuffer sb = new StringBuffer();
        for (Integer id : IDs)
            sb.append(id).append(" ");
        return sb.toString().trim();
    }
}
