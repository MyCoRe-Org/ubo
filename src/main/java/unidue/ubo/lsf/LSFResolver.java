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

package unidue.ubo.lsf;

import java.net.URLDecoder;
import java.util.Hashtable;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.jdom2.Element;
import org.jdom2.transform.JDOMSource;
import org.mycore.common.xml.MCRURIResolver;

/**
 * Implements MyCoRe URI Resolver to search HIS LSF for person data and retrieve entries.
 * 
 * lsf:pid=4711
 *   returns the metadata of person 4711
 * lsf:lastName=Meier&firstName=E
 *   returns a list of matching person entries 
 *   
 * To use this resolver add property:
 *    
 *   MCR.URIResolver.ModuleResolver.lsf=unidue.ubo.lsf.LSFResolver
 * 
 * @author Frank L\u00FCtzenkirchen
 */

public class LSFResolver implements URIResolver {

    private static final String PARAM_ENCODING = "UTF-8";
    private static final String PARAM_FIRSTNAME = "firstname";
    private static final String PARAM_LASTNAME = "lastName";
    private static final String PARAM_PID = "pid";

    public Source resolve(String href, String base) throws TransformerException {
        String key = href.substring(href.indexOf(":") + 1);
        Hashtable<String, String> params = MCRURIResolver.getParameterMap(key);

        try {
            Element result = null;
            if (params.containsKey(PARAM_PID)) {
                String pid = params.get(PARAM_PID);
                result = LSFClient.instance().getPersonDetails(pid);
            } else {
                String lastName = URLDecoder.decode(params.get(PARAM_LASTNAME), PARAM_ENCODING);
                String firstName = URLDecoder.decode(params.get(PARAM_FIRSTNAME), PARAM_ENCODING);
                result = LSFClient.instance().searchPerson(lastName, firstName);
            }
            return new JDOMSource(result);
        } catch (Exception ex) {
            throw new TransformerException(ex);
        }
    }
}
