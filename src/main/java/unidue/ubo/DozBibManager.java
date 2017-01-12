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

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.mycore.common.config.MCRConfigurationException;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.datamodel.ifs2.MCRMetadataStore;
import org.mycore.datamodel.ifs2.MCRStore;
import org.mycore.datamodel.ifs2.MCRStoreManager;
import org.mycore.datamodel.ifs2.MCRStoredMetadata;
import org.xml.sax.SAXException;

import unidue.ubo.dedup.DeDupCriteriaBuilder;

public class DozBibManager {

    /** The IFS2 metadata store that is used for object persistence */
    private MCRMetadataStore store;

    /**
     * Iterates over the IDs of all objects in the store.
     */
    public Iterator<Integer> iterateStoredIDs() {
        return store.listIDs(MCRStore.ASCENDING);
    }

    private final static DozBibManager manager = new DozBibManager();

    private DozBibManager() {
        String storeID = "ubo";
        store = (MCRMetadataStore) (MCRStoreManager.getStore(storeID));

        if (store == null) {
            try {
                store = MCRStoreManager.createStore(storeID, MCRMetadataStore.class);
            } catch (Exception ex) {
                String msg = "Unable to create metadata store " + storeID;
                throw new MCRConfigurationException(msg, ex);
            }
        }
    }

    public static DozBibManager instance() {
        return manager;
    }

    private String dateFormat = "yyyy-MM-dd HH:mm:ss";

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
            DozBibIndexer.instance().remove(id);
            DozBibIndexer.instance().add(xml);
        } else {
            store.create(new MCRJDOMContent(xml), id);
            DozBibIndexer.instance().add(xml);
        }

        return id;
    }

    public void deleteEntry(int id) throws IOException {
        DozBibIndexer.instance().remove(id);
        store.delete(id);
    }
}
