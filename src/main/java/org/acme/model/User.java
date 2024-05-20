package org.acme.model;

import io.quarkus.mongodb.panache.common.MongoEntity;
import io.quarkus.mongodb.panache.PanacheMongoEntity;
import lombok.Data;
import org.bson.types.ObjectId;

import java.time.LocalDate;

@Data
@MongoEntity(database = "user", collection = "user")
public class User extends PanacheMongoEntity {
    public String name;
    public String email;
    public String phoneNumber;
    public String address;
    public String drivingLicenseNumber;
    public LocalDate dateOfBirth;
}
