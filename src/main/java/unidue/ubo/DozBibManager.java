/**
 * Copyright (c) 2016 Duisburg-Essen University Library
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package unidue.ubo;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.mycore.common.MCRConstants;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.ifs2.MCRMetadataStore;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.metadata.MCRObjectService;
import org.mycore.mods.MCRMODSWrapper;

import java.text.SimpleDateFormat;

import unidue.ubo.dedup.DeDupCriteriaBuilder;

public class DozBibManager {

    private final static String DATE_FORMAT_LASTMODIFIED = "yyyy-MM-dd HH:mm:ss";

    /**
     * Iterates over the IDs of all objects in the store.
     */
    public Iterator<Integer> iterateStoredIDs() {
        MCRObjectID oid = buildMCRObjectID(0);
        MCRMetadataStore store = MCRXMLMetadataManager.instance().getStore(oid);
        return store.listIDs(true);
    }

    public List<String> listStoredIDs() {
        return MCRXMLMetadataManager.instance().listIDsForBase(buildMCRObjectID(0).getBase());
    }

    private final static DozBibManager manager = new DozBibManager();

    public static DozBibManager instance() {
        return manager;
    }

    /**
     * Checks if an object with the given ID already exists in the store.
     */
    public boolean exists(int id) throws Exception {
        MCRObjectID oid = buildMCRObjectID(id);
        return MCRMetadataManager.exists(oid);
    }

    /**
     * Checks if an object with the given ID already exists in the store.
     * 
     * @param id
     *            the ID of the object, which must be parseable to int.
     */
    public boolean exists(String id) throws Exception {
        return exists(Integer.parseInt(id));
    }

    public Document getEntry(int id) throws Exception {
        MCRObjectID oid = buildMCRObjectID(id);
        if (!MCRMetadataManager.exists(oid))
            return null;

        MCRObject obj = MCRMetadataManager.retrieveMCRObject(oid);
        MCRMODSWrapper wrapper = new MCRMODSWrapper(obj);
        Element mods = wrapper.getMODS().clone();
        Element bibentry = new Element("bibentry");
        bibentry.addContent(mods);
        bibentry.setAttribute("status", wrapper.getServiceFlag("status"));
        bibentry.setAttribute("id", String.valueOf(id));
        Date dateModified = obj.getService().getDate(MCRObjectService.DATE_TYPE_MODIFYDATE);
        String lastModified = new SimpleDateFormat(DATE_FORMAT_LASTMODIFIED).format(dateModified);
        bibentry.setAttribute("lastModified", lastModified);

        return new Document(bibentry);
    }

    public int createEntry(Document xml) throws Exception {
        new DeDupCriteriaBuilder().updateDeDupCriteria(xml);

        Element root = xml.getRootElement();

        MCRMODSWrapper wrapper = new MCRMODSWrapper();
        wrapper.setServiceFlag("status", root.getAttributeValue("status"));
        Element mods = root.getChild("mods", MCRConstants.MODS_NAMESPACE).clone();
        wrapper.setMODS(mods);
        MCRObject obj = wrapper.getMCRObject();

        MCRObjectID oid = buildMCRObjectID(0);
        oid = MCRObjectID.getNextFreeId(oid.getBase());
        obj.setId(oid);

        MCRMetadataManager.create(obj);

        DozBibIndexer.instance().add(xml);

        int id = oid.getNumberAsInteger();
        root.setAttribute("id", String.valueOf(id));
        return id;
    }

    public void updateEntry(Document xml) throws Exception {
        new DeDupCriteriaBuilder().updateDeDupCriteria(xml);

        Element root = xml.getRootElement();
        int id = Integer.parseInt(root.getAttributeValue("id"));

        MCRObjectID oid = buildMCRObjectID(id);
        MCRObject obj = MCRMetadataManager.retrieveMCRObject(oid);

        MCRMODSWrapper wrapper = new MCRMODSWrapper(obj);
        wrapper.setServiceFlag("status", root.getAttributeValue("status"));
        Element mods = root.getChild("mods", MCRConstants.MODS_NAMESPACE).clone();
        wrapper.setMODS(mods);

        MCRMetadataManager.update(obj);

        DozBibIndexer.instance().remove(id);
        DozBibIndexer.instance().add(xml);
    }

    public void deleteEntry(int id) throws Exception {
        DozBibIndexer.instance().remove(id);

        MCRObjectID oid = buildMCRObjectID(id);
        MCRMetadataManager.deleteMCRObject(oid);
    }

    public static MCRObjectID buildMCRObjectID(int id) {
        return MCRObjectID.getInstance(MCRObjectID.formatID("ubo", "mods", id));
    }
}
