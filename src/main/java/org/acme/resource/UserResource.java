package org.acme.resource;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.acme.model.User;
import org.acme.rabbitmq.RabbitMQProducer;
import org.jboss.logging.Logger;

import java.util.List;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
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
    @Transactional
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
    @Transactional
    public Uni<Void> deleteUser(@PathParam("id") Long id) {
        LOGGER.infof("Deleting user with id: %d", id);
        return Panache.withTransaction(() -> User.findById(id))
                .onItem().ifNotNull().transformToUni(entity -> {
                    User.deleteById(id);
                    return Uni.createFrom().voidItem();
                }).onItem().invoke(() -> rabbitMQProducer.sendMessage("Deleting user with id: " + id, "logQueue"))
                .onItem().ifNull().failWith(() -> new NotFoundException("User not found for id: " + id));
    }
}
