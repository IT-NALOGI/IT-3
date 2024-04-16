package org.acme.resource;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.common.annotation.NonBlocking;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.model.User;
import org.acme.rabbitmq.RabbitMQProducer;
import org.jboss.logging.Logger;

import java.util.List;

@Path("/users")
public class UserResource {

    @Inject
    RabbitMQProducer rabbitMQProducer;

    private static final Logger LOGGER = Logger.getLogger(UserResource.class.getName());

    @GET
    public Uni<List<PanacheEntityBase>> getAllUsers() {
        LOGGER.info("Fetching all users");
        return Panache.withTransaction(() -> User.listAll())
                .onItem().invoke(users -> rabbitMQProducer.sendMessage("Fetching all users", "logQueue"))
                .onFailure().invoke(e -> LOGGER.error("Failed to fetch all users", e));
    }

    @GET
    @Path("/{id}")
    public Uni<PanacheEntityBase> getUserById(@PathParam("id") Long id) {
        LOGGER.infof("Fetching user by id: %d", id);
        return Panache.withTransaction(() -> User.findById(id))
                .onItem().ifNotNull().invoke(user -> {
                    LOGGER.info("User found: " + user);
                    rabbitMQProducer.sendMessage("Fetching user by id: " + id, "logQueue");
                })
                .onItem().ifNull().failWith(() -> new NotFoundException("User not found for id: " + id));
    }

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<User> addUser(User user) {
        LOGGER.infof("Adding user: %s", user);
        return Panache.withTransaction(() -> {
                    return User.persist(user)  // This returns a Uni<Void> indicating the completion of the persist operation
                            .onItem().transformToUni(v -> Uni.createFrom().item(user)); // After persisting, return the user
                }).onItem().invoke(u -> rabbitMQProducer.sendMessage("Adding user: " + u, "logQueue"))
                .onFailure().invoke(e -> LOGGER.error("Failed to add user", e));
    }

    @DELETE
    @Path("/{id}")
    public Uni<Response> deleteUser(@PathParam("id") Long id) {
        return User.findById(id)
                .onItem().ifNotNull().transformToUni(user -> User.deleteById(id))
                .onItem().transform(deleted -> deleted ? Response.noContent().build() : Response.status(Response.Status.NOT_FOUND).build())
                .onFailure().recoverWithItem(th -> {
                    LOGGER.error("Failed to delete user", th);
                    return Response.serverError().build();
                });
    }
}
