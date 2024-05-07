package org.acme.resource;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.model.User;
import org.acme.service.UserServiceCommand;
import org.acme.service.UserServiceQuery;
import org.jboss.logging.Logger;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    UserServiceCommand commandService;

    @Inject
    UserServiceQuery queryService;

    private static final Logger LOGGER = Logger.getLogger(UserResource.class);

    @GET
    public Uni<Response.ResponseBuilder> getAllUsers() {
        return queryService.getAllUsers()
                .onItem().transform(Response::ok)
                .onFailure().invoke(e -> LOGGER.error("Failed to fetch all users", e));
    }


    @POST
    public Uni<Response.ResponseBuilder> addUser(User user) {
        return commandService.addUser(user)
                .onItem().transform(newUser -> Response.status(Response.Status.CREATED).entity(newUser)) // Changed 'user' to 'newUser'
                .onFailure().recoverWithItem(e -> Response.status(Response.Status.BAD_REQUEST));
    }

    @DELETE
    @Path("/{id}")
    public Uni<Response.ResponseBuilder> deleteUser(@PathParam("id") Long id) {
        return commandService.deleteUser(id)
                .onItem().transform(deleted -> deleted ? Response.noContent() : Response.status(Response.Status.NOT_FOUND))
                .onFailure().recoverWithItem(th -> Response.serverError());
    }
}
