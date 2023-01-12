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

import javax.naming.OperationNotSupportedException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import com.google.gson.Gson;
import org.mycore.access.MCRAccessManager;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.ubo.local.LocalService;
import org.mycore.ubo.picker.IdentityService;
import org.mycore.ubo.picker.PersonSearchResult;

@Path(UBOPersonSearchResource.PATH)
public class UBOPersonSearchResource {

    private static final String STRATEGY_CONFIG_STRING = "MCR.IdentityPicker.strategy";

    private static final String STRATEGY_CLASS_SUFFIX = "Service";

    public static final String PATH = "search/person";

    private static final boolean INCLUDE_LOCAL_USERS = MCRConfiguration2.getBoolean(
        "UBO.Search.include.Local.Users").orElse(false);

    @GET
    public Response search(@QueryParam("query") String searchQuery) {
        String id = "webpage:/rsc/" + PATH;
        if (MCRAccessManager.hasRule(id, "read")) {
            if (!MCRAccessManager.checkPermission(id, "read")) {
                return Response.status(Response.Status.FORBIDDEN).build();
            }
        }

        String classPrefix = MCRConfiguration2.getStringOrThrow(STRATEGY_CONFIG_STRING);
        IdentityService service = MCRConfiguration2.instantiateClass(classPrefix + STRATEGY_CLASS_SUFFIX);

        try {
            PersonSearchResult results = service.searchPerson(searchQuery);
            if (INCLUDE_LOCAL_USERS && !LocalService.class.getName().equals(classPrefix + STRATEGY_CLASS_SUFFIX)) {
                LocalService ls = new LocalService();
                PersonSearchResult localSr = ls.searchPerson(searchQuery);
                results.personList.addAll(0, localSr.personList);
            }

            return Response.ok(new Gson().toJson(results, PersonSearchResult.class)).build();
        } catch (OperationNotSupportedException e) {
            return Response.status(Response.Status.NOT_IMPLEMENTED).build();
        }
    }
}
