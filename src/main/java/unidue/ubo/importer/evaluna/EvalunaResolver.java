/**
 * Copyright (c) 2016 Duisburg-Essen University Library
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package unidue.ubo.importer.evaluna;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.jdom2.Document;
import org.jdom2.transform.JDOMSource;

/**
 * Implements MyCoRe URI Resolver to retrieve data from Evaluna Biblio database.
 * 
 * evaluna:insitutions
 *   returns the list of institutions
 *   
 *   To use this resolver add property:
 *    
 *   MCR.URIResolver.ModuleResolver.evaluna=unidue.ubo.importer.evaluna.EvalunaResolver
 * 
 * @author Frank L\u00FCtzenkirchen
 */

public class EvalunaResolver implements URIResolver {

    public Source resolve(String href, String base) throws TransformerException {
        try {
            Document institutions = new EvalunaConnection().addInstitutionRequest().getResponse().asXML();
            return new JDOMSource(institutions);
        } catch (Exception ex) {
            throw new TransformerException(ex);
        }
    }
}
