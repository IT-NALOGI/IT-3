package org.acme;


import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import jakarta.transaction.Transactional;
import org.acme.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class UserResourceTest {

    @BeforeEach
    @Transactional
    public void setup() {
        User.deleteAll();

        // Initial setup or seeding database
        User user = new User();
        user.name = "John Doe";
        user.email = "johndoe@example.com";
        user.phoneNumber = "1234567890";
        user.address = "123 Main St";
        user.drivingLicenseNumber = "D1234567";
        user.dateOfBirth = LocalDate.of(1980, 1, 1);
        user.persist();
    }

    @Test
    public void testGetAllUsers() {
        RestAssured.given()
                .when().get("/users")
                .then()
                .statusCode(200)
                .body("$.size()", is(1),
                        "[0].name", is("John Doe"),
                        "[0].email", is("johndoe@example.com"));
    }


    @Test
    public void testGetUserById() {
        User user = User.find("email", "johndoe@example.com").firstResult();
        given()
                .pathParam("id", user.id)
                .when().get("/users/{id}")
                .then()
                .statusCode(200)
                .body("name", is("John Doe"),
                        "email", is("johndoe@example.com"));
    }

    @Test
    public void testAddUser() {
        given()
                .contentType("application/json")
                .body("{\"name\": \"Jane Roe\", \"email\": \"janeroe@example.com\", \"phoneNumber\": \"0987654321\", \"address\": \"456 Main St\", \"drivingLicenseNumber\": \"D7654321\", \"dateOfBirth\": \"1990-05-05\"}")
                .when().post("/users")
                .then()
                .statusCode(200)
                .body("name", is("Jane Roe"),
                        "email", is("janeroe@example.com"));
    }

    @Test
    public void testUpdateUser() {
        User user = User.find("email", "johndoe@example.com").firstResult();
        given()
                .contentType("application/json")
                .pathParam("id", user.id)
                .body("{\"name\": \"John Doe Updated\", \"email\": \"johndoe@example.com\", \"phoneNumber\": \"1234567890\", \"address\": \"123 Main St\", \"drivingLicenseNumber\": \"D1234567\", \"dateOfBirth\": \"1980-01-01\"}")
                .when().put("/users/{id}")
                .then()
                .statusCode(200)
                .body("name", is("John Doe Updated"));
    }

    @Test
    public void testDeleteUser() {
        User user = User.find("email", "johndoe@example.com").firstResult();
        given()
                .pathParam("id", user.id)
                .when().delete("/users/{id}")
                .then()
                .statusCode(204);

        // Verify user is deleted
        RestAssured.given()
                .when().get("/users")
                .then()
                .statusCode(200)
                .body("$.size()", is(0));
    }
}
