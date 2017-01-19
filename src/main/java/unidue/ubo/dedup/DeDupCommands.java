/**
 * Copyright (c) 2016 Duisburg-Essen University Library
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package unidue.ubo.dedup;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.cli.MCRAbstractCommands;
import org.mycore.frontend.cli.annotation.MCRCommand;
import org.mycore.frontend.cli.annotation.MCRCommandGroup;
import org.mycore.mods.MCRMODSWrapper;

@MCRCommandGroup(name = "UBO DeDup Commands")
public class DeDupCommands extends MCRAbstractCommands {

    private final static Logger LOGGER = LogManager.getLogger(DeDupCommands.class);

    @MCRCommand(syntax = "ubo build duplicates report to directory {0}", help = "builds report on possibly duplicate entries and writes it as xml to file duplicates.xml in directory {0}")
    public static void buildDuplicatesReport(String targetDirectory) throws Exception {
        LOGGER.info("Building duplicates report...");

        Map<DeDupCriterion, DeDupGroup> key2group = new HashMap<DeDupCriterion, DeDupGroup>();
        DeDupCriteriaBuilder builder = new DeDupCriteriaBuilder();
        int numEntries = 0;

        for (String id : MCRXMLMetadataManager.instance().listIDsOfType("mods")) {
            try {
                MCRObject obj = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(id));
                Element mods = new MCRMODSWrapper(obj).getMODS();
                numEntries++;

                DeDupGroup group = new DeDupGroup(builder.buildFromMODS(mods), id);
                mergeWithOtherMatchingGroups(key2group, group);
            } catch (Exception ex) {
                String msg = "Skipping corrupted entry " + id;
                LOGGER.warn(msg, ex);
            }
        }

        List<DeDupGroup> groups = getFinalGroups(key2group);
        int numUniqueEntries = removeUniqueEntries(groups);

        LOGGER.info("Number of      entries processed: " + numEntries);
        LOGGER.info("Number of         unique entries: " + numUniqueEntries);
        LOGGER.info("Number of     non-unique entries: " + (numEntries - numUniqueEntries));
        LOGGER.info("Number of   deduplication groups: " + groups.size());
        LOGGER.info("Number of potentially duplicates: " + (numEntries - numUniqueEntries - groups.size()));

        Element report = new Element("duplicates");
        for (DeDupGroup group : groups)
            report.addContent(group.buildXML());

        File targetFile = new File(targetDirectory, "duplicates.xml");
        new MCRJDOMContent(report).sendTo(targetFile);
        LOGGER.info("Wrote report to file " + targetFile.getAbsolutePath());
    }

    private static int removeUniqueEntries(List<DeDupGroup> groups) {
        int numUniqueEntries = 0;
        for (Iterator<DeDupGroup> iterator = groups.iterator(); iterator.hasNext();) {
            DeDupGroup group = iterator.next();
            if (group.getIDs().size() == 1) {
                numUniqueEntries++;
                iterator.remove();
            }
        }
        return numUniqueEntries;
    }

    private static List<DeDupGroup> getFinalGroups(Map<DeDupCriterion, DeDupGroup> key2group) {
        List<DeDupGroup> groups = new ArrayList<DeDupGroup>();
        groups.addAll(getGroups(key2group));
        Collections.sort(groups);
        return groups;
    }

    private static void mergeWithOtherMatchingGroups(Map<DeDupCriterion, DeDupGroup> key2group, DeDupGroup group) {
        Set<DeDupGroup> otherGroups = findOtherMatchingGroups(key2group, group);

        for (DeDupGroup other : otherGroups)
            group.assimilate(other);
        for (DeDupCriterion criterion : group.getCriteria())
            key2group.put(criterion, group);
    }

    private static Set<DeDupGroup> getGroups(Map<DeDupCriterion, DeDupGroup> key2group) {
        return new HashSet<DeDupGroup>(key2group.values());
    }

    private static Set<DeDupGroup> findOtherMatchingGroups(Map<DeDupCriterion, DeDupGroup> key2group,
        DeDupGroup group) {
        Set<DeDupGroup> otherGroups = new HashSet<DeDupGroup>();

        for (DeDupCriterion criterion : group.getCriteria()) {
            DeDupGroup other = key2group.get(criterion);
            if (other != null) {
                LOGGER
                    .info(group.listIDs() + " may be same as " + other.listIDs() + " because of matching " + criterion);
                criterion.markAsUsedInMatch();
                otherGroups.add(other);
            }
        }
        return otherGroups;
    }
}
