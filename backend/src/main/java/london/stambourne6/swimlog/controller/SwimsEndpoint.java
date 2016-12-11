package london.stambourne6.swimlog.controller;

import london.stambourne6.swimlog.model.Swim;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api/swims")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface SwimsEndpoint {
    @DELETE
    @Path("/{swimId}")
    Response deleteSwim(@PathParam("swimId") int swimId);

    @PUT
    @Path("/{swimId}")
    Response updateSwim(@PathParam("swimId") int swimId, Swim newSwimState);

    @GET
    @Path("/")
    Response listSwims(
            @QueryParam("fromDate") LocalDateParam fromDate,
            @QueryParam("toDate") LocalDateParam toDate);

    @POST
    @Path("/")
    Response createSwim(Swim swim);
}
