package org.acme.resource;


import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.acme.model.User;

import java.util.List;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @GET
    public List<User> getAllUsers() {
        return User.listAll();
    }

    @GET
    @Path("/{id}")
    public User getUserById(@PathParam("id") Long id) {
        return User.findById(id);
    }

    @POST
    @Transactional
    public User addUser(User user) {
        User.persist(user);
        return user;
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public User updateUser(@PathParam("id") Long id, User user) {
        User entity = User.findById(id);
        if(entity == null) {
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
        User entity = User.findById(id);
        if(entity == null) {
            throw new NotFoundException();
        }
        User.deleteById(id);
    }
}