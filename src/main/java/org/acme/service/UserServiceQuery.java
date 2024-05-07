package org.acme.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import org.acme.model.User;
import org.jboss.logging.Logger;

import java.util.List;

@ApplicationScoped
public class UserServiceQuery {

    private static final Logger LOGGER = Logger.getLogger(UserServiceQuery.class);

    public Uni<List<User>> getAllUsers() {
        LOGGER.info("Fetching all users");
        return Panache.withTransaction(User::listAll);
    }

}
