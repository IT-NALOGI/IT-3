package org.acme.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.model.User;
import org.acme.rabbitmq.RabbitMQProducer;
import org.jboss.logging.Logger;

@ApplicationScoped
public class UserServiceCommand {

    @Inject
    RabbitMQProducer rabbitMQProducer;

    private static final Logger LOGGER = Logger.getLogger(UserServiceCommand.class);

    public Uni<User> addUser(User user) {
        LOGGER.infof("Adding user: %s", user);
        return Panache.withTransaction(() -> {
                    return User.persist(user)  // This returns a Uni<Void> indicating the completion of the persist operation
                            .onItem().transformToUni(v -> Uni.createFrom().item(user)); // After persisting, return the user
                }).onItem().invoke(u -> rabbitMQProducer.sendMessage("Adding user: " + u, "logQueue"))
                .onFailure().invoke(e -> LOGGER.error("Failed to add user", e));
    }

    public Uni<Boolean> deleteUser(Long id) {
        LOGGER.infof("Deleting user with ID: %d", id);
        return Panache.withTransaction(() -> User.deleteById(id))
                .onItem().invoke(deleted -> rabbitMQProducer.sendMessage("Deleting user with ID: " + id, "logQueue"))
                .onFailure().invoke(e -> LOGGER.error("Failed to delete user with ID: " + id, e));
    }
}
