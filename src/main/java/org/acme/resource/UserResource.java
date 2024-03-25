package org.acme.resource;


import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.acme.model.User;

import java.util.List;
import org.jboss.logging.Logger;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    private static final Logger LOGGER = Logger.getLogger(UserResource.class.getName());

    @GET
    public List<User> getAllUsers() {
        LOGGER.info("Fetching all users");
        return User.listAll();
    }

    @GET
    @Path("/{id}")
    public User getUserById(@PathParam("id") Long id) {
        LOGGER.infof("Fetching user by id: %d", id);
        return User.findById(id);
    }

    @POST
    @Transactional
    public User addUser(User user) {
        LOGGER.infof("Adding user: %s", user);
        User.persist(user);
        return user;
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
        return entity;
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public void deleteUser(@PathParam("id") Long id) {
        LOGGER.infof("Deleting user with id: %d", id);
        User entity = User.findById(id);
        if(entity == null) {
            LOGGER.warnf("User not found for id: %d", id);
            throw new NotFoundException();
        }
        User.deleteById(id);
    }
}