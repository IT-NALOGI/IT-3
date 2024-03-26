package org.acme.resource;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
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
        return Uni.createFrom().item(() -> User.listAll())
                .onItem().call(users -> rabbitMQProducer.sendMessage("Fetching all users", "logQueue"))
                .onFailure().invoke(e -> LOGGER.error("Failed to fetch all users", e));
    }


    @GET
    @Path("/{id}")
    public Uni<Object> getUserById(@PathParam("id") Long id) {
        return Uni.createFrom().item(() -> User.findById(id))
                .onItem().ifNotNull().invoke(user -> {
                    LOGGER.infof("Fetching user by id: %d", id);
                    rabbitMQProducer.sendMessage("Fetching user by id: " + id, "logQueue");
                })
                .onItem().ifNull().failWith(() -> new NotFoundException("User not found for id: " + id));
    }


    @POST
    @Transactional
    public Uni<User> addUser(User user) {
        LOGGER.infof("Adding user: %s", user);
        return Uni.createFrom().item(user)
                .call(u -> {
                    User.persist(u);
                    return rabbitMQProducer.sendMessage("Adding user: " + u, "logQueue");
                })
                .onFailure().invoke(e -> LOGGER.error("Failed to add user", e));
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Uni<Void> deleteUser(@PathParam("id") Long id) {
        return Uni.createFrom().item(() -> User.findById(id))
                .onItem().ifNotNull().transformToUni(entity -> {
                    LOGGER.infof("Deleting user with id: %d", id);
                    User.deleteById(id);
                    return rabbitMQProducer.sendMessage("Deleting user with id: " + id, "logQueue");
                })
                .onItem().ifNull().failWith(() -> new NotFoundException("User not found for id: " + id))
                .replaceWith(Uni.createFrom().nullItem());
    }
    @PUT
    @Path("/{id}")
    @Transactional
    public User updateUser(@PathParam("id") Long id, User user) {
        LOGGER.infof("Updating user with id: %d", id);
        User entity = User.findById(id);
        if(entity == null) {
            LOGGER.warnf("User not found for id: %d", id);
            throw new NotFoundException();
        }
        entity.name = user.name;
        entity.email = user.email;
        entity.phoneNumber = user.phoneNumber;
        entity.address = user.address;
        entity.drivingLicenseNumber = user.drivingLicenseNumber;
        entity.dateOfBirth = user.dateOfBirth;
        try {
            rabbitMQProducer.sendMessage("Updating user with id: %d", "logQueue");
        } catch (Exception e) {
            LOGGER.error("Failed to send message to RabbitMQ", e);
        }
        return entity;
    }
}
