/**
 * Copyright (c) 2016 Duisburg-Essen University Library
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 **/

package unidue.ubo.lsf;

import java.net.URLDecoder;
import java.util.Hashtable;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

    private final static Logger LOGGER = LogManager.getLogger(LSFResolver.class);

    private static final String PARAM_ENCODING = "UTF-8";
    private static final String PARAM_FIRSTNAME = "firstname";
    private static final String PARAM_LASTNAME = "lastName";
    private static final String PARAM_PID = "pid";

    public Source resolve(String href, String base) throws TransformerException {
        String key = href.substring(href.indexOf(":") + 1);
        Hashtable<String, String> params = MCRURIResolver.getParameterMap(key);
        LOGGER.info("PARAMS: " + params.toString());
        try {
            Element result = null;
            if (params.containsKey(PARAM_PID)) {
                String pid = params.get(PARAM_PID);
                result = new LSFService().getPersonDetails(params);
            } else {
                String lastName = URLDecoder.decode(params.get(PARAM_LASTNAME), PARAM_ENCODING);
                String firstName = URLDecoder.decode(params.get(PARAM_FIRSTNAME), PARAM_ENCODING);
                result = new LSFService().searchPerson(params);
            }
            return new JDOMSource(result);
        } catch (Exception ex) {
            throw new TransformerException(ex);
        }
    }
}
