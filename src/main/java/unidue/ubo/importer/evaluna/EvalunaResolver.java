/**
 * $Revision: 34550 $ 
 * $Date: 2016-02-10 20:00:24 +0100 (Mi, 10 Feb 2016) $
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
