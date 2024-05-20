package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.model.User;
import org.acme.repository.UserRepository;
import org.bson.types.ObjectId;
import org.jboss.logging.Logger;

import java.util.List;

@ApplicationScoped
public class UserServiceQuery {

    @Inject
    UserRepository userRepository;

    private static final Logger LOGGER = Logger.getLogger(UserServiceQuery.class);

    public List<User> getAllUsers() {
        LOGGER.info("Fetching all users");
        return userRepository.listAll();
    }

    public User getUserById(String id) {
        LOGGER.infof("Fetching user with ID: %s", id);
        return userRepository.findById(new ObjectId(id));
    }
}
