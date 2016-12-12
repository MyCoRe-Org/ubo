/**
 * $Revision$ 
 * $Date$
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

package unidue.ubo;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

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
            return new JDOMSource(DozBibManager.instance().getEntry(ID).detachRootElement());
        } catch (Exception ex) {
            throw new TransformerException(ex);
        }
    }
}
