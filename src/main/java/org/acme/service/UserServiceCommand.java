package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.model.User;
import org.acme.rabbitmq.RabbitMQProducer;
import org.acme.repository.UserRepository;
import org.bson.types.ObjectId;
import org.jboss.logging.Logger;

@ApplicationScoped
public class UserServiceCommand {

    @Inject
    UserRepository userRepository;

    @Inject
    RabbitMQProducer rabbitMQProducer;

    private static final Logger LOGGER = Logger.getLogger(UserServiceCommand.class);

    public User addUser(User user) {
        userRepository.persist(user);
        rabbitMQProducer.sendMessage("Adding user: " + user, "logQueue");
        LOGGER.infof("Added user: %s", user);
        return user;
    }

    public User updateUser(String id, User updatedUser) {
        User existingUser = userRepository.findById(new ObjectId(id));
        if (existingUser != null) {
            existingUser.name = updatedUser.name;
            existingUser.email = updatedUser.email;
            existingUser.phoneNumber = updatedUser.phoneNumber;
            existingUser.address = updatedUser.address;
            existingUser.drivingLicenseNumber = updatedUser.drivingLicenseNumber;
            existingUser.dateOfBirth = updatedUser.dateOfBirth;
            userRepository.update(existingUser);
            rabbitMQProducer.sendMessage("Updated user: " + existingUser, "logQueue");
            LOGGER.infof("Updated user: %s", existingUser);
            return existingUser;
        } else {
            return null;
        }
    }

    public boolean deleteUser(String id) {
        boolean deleted = userRepository.deleteById(new ObjectId(id));
        if (deleted) {
            rabbitMQProducer.sendMessage("Deleted user with ID: " + id, "logQueue");
            LOGGER.infof("Deleted user with ID: %s", id);
        }
        return deleted;
    }
}
