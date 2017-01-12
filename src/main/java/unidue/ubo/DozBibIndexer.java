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
import java.util.Iterator;

import org.jdom2.Document;
import org.mycore.services.fieldquery.MCRSearcher;
import org.mycore.services.fieldquery.MCRSearcherFactory;
import org.mycore.services.fieldquery.data2fields.MCRData2FieldsXML;
import org.mycore.services.fieldquery.data2fields.MCRIndexEntry;

public class DozBibIndexer {

    private final static DozBibIndexer indexer = new DozBibIndexer();

    private DozBibIndexer() {
    }

    public static DozBibIndexer instance() {
        return indexer;
    }

    private MCRSearcher searcher = MCRSearcherFactory.getSearcherForIndex("ubo");

    public void add(Document entry) {
        MCRIndexEntry indexEntry = new MCRIndexEntry();
        indexEntry.setEntryID("ubo:" + entry.getRootElement().getAttributeValue("id"));
        new MCRData2FieldsXML("ubo", entry).addFieldValues(indexEntry);
        searcher.addToIndex(indexEntry);
    }

    public void remove(int id) throws IOException {
        searcher.removeFromIndex("ubo:" + id);
    }

    /** Rebuilds the search index */
    public static void rebuildIndex() throws Exception {
        DozBibIndexer.instance().searcher.clearIndex();

        Iterator<Integer> IDs = DozBibManager.instance().iterateStoredIDs();
        while (IDs.hasNext()) {
            int id = IDs.next();
            Document entry = DozBibManager.instance().getEntry(id);
            DozBibIndexer.instance().add(entry);
        }
    }
}
