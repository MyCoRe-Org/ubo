/**
 * Copyright (c) 2016 Duisburg-Essen University Library
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package unidue.ubo;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.mycore.common.config.MCRConfigurationException;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.datamodel.ifs2.MCRMetadataStore;
import org.mycore.datamodel.ifs2.MCRStore;
import org.mycore.datamodel.ifs2.MCRStoreManager;
import org.mycore.datamodel.ifs2.MCRStoredMetadata;
import org.mycore.services.fieldquery.MCRSearcher;
import org.mycore.services.fieldquery.MCRSearcherFactory;
import org.mycore.services.fieldquery.data2fields.MCRData2FieldsXML;
import org.mycore.services.fieldquery.data2fields.MCRIndexEntry;
import org.xml.sax.SAXException;

import unidue.ubo.dedup.DeDupCriteriaBuilder;

public class DozBibManager {

    private static final Logger LOG = LogManager.getLogger(DozBibManager.class.getName());

    /** The type of object that is stored, e.g. Document, LegalEntity */
    private String objectType;

    /** The IFS2 metadata store that is used for object persistence */
    private MCRMetadataStore store;

    /**
     * Creates (if not already existing) or retrieves the metadata store.
     */
    public static MCRMetadataStore buildMetadataStore(String storeID) {
        MCRMetadataStore ms = (MCRMetadataStore) (MCRStoreManager.getStore(storeID));

        if (ms == null) {
            try {
                ms = MCRStoreManager.createStore(storeID, MCRMetadataStore.class);
            } catch (Exception ex) {
                String msg = "Unable to create metadata store " + storeID;
                throw new MCRConfigurationException(msg, ex);
            }
        }

        return ms;
    }

    /**
     * Returns the IFS2 metadata store used for persistence.
     */
    public MCRMetadataStore getStore() {
        return store;
    }

    /**
     * Returns the object type that is persisted, e.g. Document, LegalEntity
     */
    public String getObjectType() {
        return objectType;
    }

    /**
     * Checks if an object with the given ID already exists in the store.
     */
    public boolean exists(int id) throws Exception {
        return store.exists(id);
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

    /**
     * Iterates over the IDs of all objects in the store.
     */
    public Iterator<Integer> iterateStoredIDs() {
        return store.listIDs(MCRStore.ASCENDING);
    }

    /**
     * Retrieves the xml content stored for the given object.
     */
    public MCRContent retrieveContent(int id) throws IOException {

        MCRStoredMetadata metadata = store.retrieve(id);
        try {
            return metadata.getMetadata();
        } catch (NullPointerException e) {
            /*
             * throw for compatibility of many other classes that use this
             * manager
             */
            LOG.info("could not find metadata in store: ->" + store.getID() + "<- for id: ->" + id + "<-");
            throw e;
        }
    }

    private final static DozBibManager manager = new DozBibManager();

    private DozBibManager() {
        this.objectType = "ubo";
        store = buildMetadataStore(objectType.toLowerCase());
    }

    public static DozBibManager instance() {
        return manager;
    }

    private String dateFormat = "yyyy-MM-dd HH:mm:ss";

    private MCRSearcher searcher = MCRSearcherFactory.getSearcherForIndex("ubo");

    public Document getEntry(int id) throws IOException, JDOMException, SAXException {
        MCRStoredMetadata sm = store.retrieve(id);
        return (sm == null ? null : sm.getMetadata().asXML());
    }

    public int saveEntry(Document xml) throws IOException, JDOMException {
        return saveEntry(xml, true);
    }

    public int saveEntry(Document xml, boolean setLastModified) throws IOException, JDOMException {
        Element root = xml.getRootElement();

        if (setLastModified)
            root.setAttribute("lastModified", new SimpleDateFormat(dateFormat).format(new Date()));

        new DeDupCriteriaBuilder().updateDeDupCriteria(xml);

        int id = Integer.parseInt(root.getAttributeValue("id", "0"));
        if (id == 0) {
            id = store.getNextFreeID();
            root.setAttribute("id", String.valueOf(id));
        }

        if (store.exists(id)) {
            store.retrieve(id).update(new MCRJDOMContent(xml));
            searcher.removeFromIndex("ubo:" + id);
            addToIndex(xml);
        } else {
            store.create(new MCRJDOMContent(xml), id);
            addToIndex(xml);
        }

        return id;
    }

    public void deleteEntry(int id) throws IOException {
        searcher.removeFromIndex("ubo:" + id);
        store.delete(id);
    }

    private void addToIndex(Document entry) {
        MCRIndexEntry indexEntry = new MCRIndexEntry();
        indexEntry.setEntryID("ubo:" + entry.getRootElement().getAttributeValue("id"));
        new MCRData2FieldsXML("ubo", entry).addFieldValues(indexEntry);
        searcher.addToIndex(indexEntry);
    }

    /** Rebuilds the search index */
    public static void rebuildIndex() throws Exception {
        DozBibManager.instance().searcher.clearIndex();

        Iterator<Integer> IDs = DozBibManager.instance().iterateStoredIDs();
        while (IDs.hasNext()) {
            int id = IDs.next();
            Document entry = DozBibManager.instance().getEntry(id);
            DozBibManager.instance().addToIndex(entry);
        }
    }
}
