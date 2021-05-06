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
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.common.xml.MCRURIResolver;
import org.mycore.frontend.MCRFrontendUtil;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Path("export")
public class UBOExportResource {

    public static final Namespace CSL_NAMESPACE = Namespace.getNamespace("csl", "http://purl.org/net/xbiblio/csl");

    private static String encode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

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

    @GET
    @Path("link/{format}/{pids:.+}")
    public Response link(
        @PathParam("format") String format,
        @PathParam("pids") String pidSegment,
        @QueryParam("sortField") String sortField,
        @QueryParam("sortDirection") String sortDirection,
        @QueryParam("year") int year,
        @QueryParam("style") String style) throws URISyntaxException {

        Set<String> pids = Stream.of(pidSegment.split(";")).collect(Collectors.toSet());

        if (pids.size() == 0) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        if(sortField == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        if(sortDirection == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        if(year == -1) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        String nidConnectionValue = "(" + String.join(" OR ", pids) + ")";
        String baseURL = MCRFrontendUtil.getBaseURL();

        String solrQuery = "nid_connection:" + nidConnectionValue + " and year:>=" + year;
        StringBuilder solrRequest = new StringBuilder()
            .append(baseURL).append("servlets/solr/select")
            .append("?q=").append(encode(solrQuery))
            .append("&rows=9999&")
            .append("&sort=").append(encode(sortField)).append(encode(" ")).append(encode(sortDirection));

        if (style == null) {
            solrRequest.append("&XSL.Transformer=").append(encode(format));
        } else {
            solrRequest.append("&XSL.Transformer=response-csl-").append(encode(format));
            solrRequest.append("&XSL.style=").append(encode(style));
        }

        URI newLocation = new URI(solrRequest.toString());
        return Response.temporaryRedirect(newLocation).build();
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
