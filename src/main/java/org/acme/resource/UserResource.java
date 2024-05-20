package org.acme.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.model.User;
import org.acme.service.UserServiceCommand;
import org.acme.service.UserServiceQuery;
import org.bson.types.ObjectId;
import org.jboss.logging.Logger;

import java.util.List;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    UserServiceCommand commandService;

    @Inject
    UserServiceQuery queryService;

    private static final Logger LOGGER = Logger.getLogger(UserResource.class);

    @POST
    public Response addUser(User user) {
        User newUser = commandService.addUser(user);
        return Response.status(Response.Status.CREATED).entity(newUser).build();
    }

    @GET
    public List<User> getAllUsers() {
        return queryService.getAllUsers();
    }

    @GET
    @Path("/{id}")
    public Response getUserById(@PathParam("id") String id) {
        User user = queryService.getUserById(String.valueOf(new ObjectId(id)));
        if (user != null) {
            return Response.ok(user).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response updateUser(@PathParam("id") String id, User user) {
        User updatedUser = commandService.updateUser(new ObjectId(id).toString(), user);
        if (updatedUser != null) {
            return Response.ok(updatedUser).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deleteUser(@PathParam("id") String id) {
        boolean deleted = commandService.deleteUser(new ObjectId(id).toString());
        if (deleted) {
            return Response.noContent().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
