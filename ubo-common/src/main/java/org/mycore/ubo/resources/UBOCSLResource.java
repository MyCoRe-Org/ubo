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

package org.mycore.ubo.resources;

import com.google.gson.Gson;
import de.undercouch.citeproc.CSL;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.common.xml.MCRURIResolver;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Path("csl")
public class UBOCSLResource {


    public static final Namespace CSL_NAMESPACE = Namespace.getNamespace("csl", "http://purl.org/net/xbiblio/csl");

    @GET
    @Path("styles")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listStyles(){
        String[] styles = MCRConfiguration2.getStringOrThrow("MCR.Export.CSL.Styles").split(",");

        List<CSLEntry> result = new ArrayList<>(styles.length);
        for (String style : styles) {
            Element element = MCRURIResolver.instance().resolve("resource:" + style + ".csl");

            String title = Optional.ofNullable(element.getChild("info", CSL_NAMESPACE))
                    .map(el -> Optional.ofNullable(el.getChild("title", CSL_NAMESPACE)))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(Element::getTextNormalize).orElse(style);

            CSLEntry cslEntry = new CSLEntry(style, title);
            result.add(cslEntry);
        }

        String json = new Gson().toJson(result.toArray(), CSLEntry[].class);
        return Response.ok(json).build();
    }

    public static class CSLEntry {
        public CSLEntry(String id, String title) {
            this.id = id;
            this.title = title;
        }

        public String id;
        public String title;
    }
}
