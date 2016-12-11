package london.stambourne6.swimlog.controller;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface UsersEndpoint {
    @DELETE
    @Path("/{userId}")
    Response deleteUser(@PathParam("userId") int userId);

    @PUT
    @Path("/{userId}")
    Response updateUser(@PathParam("userId") int userId, PlaintextUserCredentialsWithRole newUserState);

    @GET
    @Path("/")
    Response listUsers();

    @POST
    @Path("/")
    Response createUser(PlaintextUserCredentials newUser);

    @GET
    @Path("/{userId}/swims")
    Response listSwimsForUser(
            @PathParam("userId") int userId,
            @QueryParam("fromDate") LocalDateParam fromDate,
            @QueryParam("toDate") LocalDateParam toDate);

    @GET
    @Path("/{userName}")
    Response userInfo(@PathParam("userName") String userName);
}
