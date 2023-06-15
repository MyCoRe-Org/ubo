/*
 * This file is part of ***  M y C o R e  ***
 * See http://www.mycore.de/ for details.
 *
 * MyCoRe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyCoRe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MyCoRe.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mycore.ubo;

import java.util.List;
import java.util.stream.Collectors;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.util.JAXBSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.transform.JDOMSource;
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUserManager;
import org.mycore.user2.utils.MCRUserTransformer;

/**
 * Resolves a User by connection id
 */
public class UBOUserByConnectionResolver implements URIResolver {

    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        String connection_id = href.split(":", 2)[1];
        final List<MCRUser> users = MCRUserManager.getUsers("id_connection", connection_id).collect(Collectors.toList());
        if(users.size()!=1){
            LOGGER.warn("There is a connection ID which has no User in DB {}", connection_id);
            return new JDOMSource(new Element("null"));
        }
        final MCRUser user = users.get(0);

        try {
            return new JAXBSource(MCRUserTransformer.JAXB_CONTEXT, user.getSafeCopy());
        } catch (JAXBException e) {
            throw new TransformerException(e);
        }
    }

}
