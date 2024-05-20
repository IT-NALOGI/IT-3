package org.acme.repository;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.model.User;
import org.bson.types.ObjectId;

@ApplicationScoped
public class UserRepository implements PanacheMongoRepository<User> {

    public User findById(String id) {
        return findById(new ObjectId(id));
    }

    public boolean deleteById(String id) {
        return deleteById(new ObjectId(id));
    }
}
