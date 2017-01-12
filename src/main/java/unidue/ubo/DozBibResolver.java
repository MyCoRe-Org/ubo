/**
 * Copyright (c) 2016 Duisburg-Essen University Library
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package unidue.ubo;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.jdom2.Document;
import org.jdom2.transform.JDOMSource;

import unidue.ubo.DozBibManager;

/**
 * Implements MyCoRe URI Resolver to retrieve bibliography entries.
 * 
 * ubo:4711
 *   returns the metadata of entry 4711
 *   
 *   To use this resolver add property:
 *    
 *   MCR.URIResolver.ModuleResolver.ubo=unidue.ubo.DozBibResolver
 * 
 * @author Harald Richter
 */

public class DozBibResolver implements URIResolver {

    public Source resolve(String href, String base) throws TransformerException {
        String id = href.substring(href.indexOf(":") + 1);
        int ID = Integer.parseInt(id);
        try {
            Document entry = DozBibManager.instance().getEntry(ID);
            return new JDOMSource(entry.detachRootElement());
        } catch (Exception ex) {
            throw new TransformerException(ex);
        }
    }
}
