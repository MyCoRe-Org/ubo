package org.mycore.ubo.dedup;

import com.google.gson.Gson;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.mycore.ubo.AccessControl;
import org.mycore.ubo.dedup.jpa.DeduplicationKeyManager;
import org.mycore.ubo.dedup.jpa.DeduplicationNoDuplicate;

import java.util.List;

@Path("dedup")
public class DeDupResource {

    @GET
    @Path("list/duplicates")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listDedup(@QueryParam("idSort") String idSortStr, @QueryParam("typeSort") String typeSortStr, @QueryParam("type") String type) {

        if (!AccessControl.currentUserIsAdmin()) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        DeduplicationKeyManager.SortOrder idSort = idSortStr == null ? DeduplicationKeyManager.SortOrder.NONE :
                switch (idSortStr) {
                    case "asc" -> DeduplicationKeyManager.SortOrder.ASC;
                    case "desc" -> DeduplicationKeyManager.SortOrder.DESC;
                    case "none" -> DeduplicationKeyManager.SortOrder.NONE;
                    default -> throw new BadRequestException("Invalid sort order: " + idSortStr);
                };

        DeduplicationKeyManager.SortOrder typeSort = typeSortStr == null ? DeduplicationKeyManager.SortOrder.NONE :
                switch (typeSortStr) {
                    case "asc" -> DeduplicationKeyManager.SortOrder.ASC;
                    case "desc" -> DeduplicationKeyManager.SortOrder.DESC;
                    case "none" -> DeduplicationKeyManager.SortOrder.NONE;
                    default -> throw new BadRequestException("Invalid sort order: " + typeSortStr);
                };

        List<PossibleDuplicate> duplicates = DeduplicationKeyManager.getInstance().getDuplicates(idSort, typeSort, type);
        String json = new Gson().toJson(duplicates.toArray(), PossibleDuplicate[].class);
        return Response.ok(json)
                .header("Access-Control-Allow-Origin", "*")
                .build();
    }

    @DELETE
    @Path("delete/no-duplicates/{id}")
    public Response deleteNoDuplicate(@PathParam("id") int id) {

        if (!AccessControl.currentUserIsAdmin()) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        if(id < 0) {
            throw new BadRequestException("Invalid id: " + id);
        }

        DeduplicationKeyManager.getInstance().removeNoDuplicate(id);
        return Response.ok().build();
    }

    @GET
    @Path("list/no-duplicates")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listNoDedup(@QueryParam("sortBy") String sortByStr, @QueryParam("sortOrder") String sortOrderStr) {

        if (!AccessControl.currentUserIsAdmin()) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        DeduplicationKeyManager.DeduplicationNoDuplicateOrderFields sortBy = sortByStr == null ? DeduplicationKeyManager.DeduplicationNoDuplicateOrderFields.DATE :
                switch (sortByStr) {
                    case "date" -> DeduplicationKeyManager.DeduplicationNoDuplicateOrderFields.DATE;
                    case "creator" -> DeduplicationKeyManager.DeduplicationNoDuplicateOrderFields.CREATOR;
                    case "mcrId1" -> DeduplicationKeyManager.DeduplicationNoDuplicateOrderFields.MCR_ID_1;
                    case "mcrId2" -> DeduplicationKeyManager.DeduplicationNoDuplicateOrderFields.MCR_ID_2;
                    default -> throw new BadRequestException("Invalid sort order: " + sortByStr);
                };

        DeduplicationKeyManager.SortOrder sortOrder = sortOrderStr == null ? DeduplicationKeyManager.SortOrder.DESC :
                switch (sortOrderStr) {
                    case "asc" -> DeduplicationKeyManager.SortOrder.ASC;
                    case "desc" -> DeduplicationKeyManager.SortOrder.DESC;
                    default -> throw new BadRequestException("Invalid sort direction: " + sortOrderStr);
                };

        List<DeduplicationNoDuplicate> noDuplicates = DeduplicationKeyManager.getInstance().getNoDuplicates(sortOrder, sortBy);

        List<NoDuplicate> noDuplicatesList = noDuplicates.stream()
                .map(nd -> new NoDuplicate(nd.getId(), nd.getMcrId1(), nd.getMcrId2(), nd.getCreator(), nd.getCreationDate()))
                .toList();

        String json = new Gson().toJson(noDuplicatesList.toArray(), NoDuplicate[].class);
        return Response.ok(json)
                .header("Access-Control-Allow-Origin", "*")
                .build();
    }

}
