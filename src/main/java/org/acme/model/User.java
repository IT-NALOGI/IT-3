package org.acme.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "`User`")
public class User extends PanacheEntity {

    @Column(length = 100, nullable = false)
    public String name;

    @Column(unique = true, nullable = false)
    public String email;

    @Column(length = 20, nullable = false)
    public String phoneNumber;

    @Column(length = 100, nullable = false)
    public String address;

    @Column(length = 30, nullable = false)
    public String drivingLicenseNumber;

    public LocalDate dateOfBirth;

}
