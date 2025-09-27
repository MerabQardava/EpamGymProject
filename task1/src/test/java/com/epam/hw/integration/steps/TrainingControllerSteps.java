package com.epam.hw.integration.steps;


import com.epam.hw.entity.Training;
import com.epam.hw.entity.TrainingType;
import com.epam.hw.repository.TrainingRepository;
import com.epam.hw.repository.TrainingTypeRepository;
import com.epam.hw.service.TrainingService;
import com.epam.hw.service.UserService;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class TrainingControllerSteps {

    @LocalServerPort
    private int port;

    private Response response;
    private String jwtToken;
    private String traineeUsername;
    private String trainerUsername;
    private Long trainingId;

    @Autowired
    private UserService userService;

    @Autowired
    private TrainingService trainingService;

    @Autowired
    private TrainingTypeRepository trainingTypeRepository;

    @Autowired
    private TrainingRepository trainingRepository;

    @Before
    public void setup() {
        RestAssured.port = port;
        TrainingType yoga = new TrainingType();
        Optional<TrainingType> optionalYoga = trainingTypeRepository.findByTrainingTypeName("YOGA");
        if(optionalYoga.isEmpty()) {
            yoga.setTrainingTypeName("YOGA");
            trainingTypeRepository.save(yoga);
        }

        setupTestUserAndToken();
    }

    private void setupTestUserAndToken() {
        traineeUsername = userService.register("John", "Doe", LocalDate.of(1990, 1, 1), "123 Test St", "testPassword")
                .getUser().getUsername();
        System.out.println("Trainee username: " + traineeUsername);
        // Register a test trainer
        trainerUsername = userService.register("Jane", "Smith", "YOGA", "trainerPassword")
                .getUser().getUsername();
        System.out.println("Trainer username: " + trainerUsername);
        jwtToken = userService.verify(traineeUsername, "testPassword");
        if ("fail".equals(jwtToken)) {
            throw new RuntimeException("Failed to authenticate test user for JWT token: " + traineeUsername);
        }
        if (!jwtToken.startsWith("Bearer ")) {
            jwtToken = "Bearer " + jwtToken;
        }
        System.out.println("JWT token: " + jwtToken);
    }

    @Given("the application is running")
    public void applicationIsRunning() {
    }

    @Given("a training with ID {int} exists")
    public void aTrainingWithIdExists(int id) {

        String body = """
        {
            "trainingName": "Yoga Session",
            "trainingTypeName": "YOGA",
            "date": "2025-09-20",
            "duration": 60
        }
        """;

        response = given()
                .header("Authorization", jwtToken)
                .contentType("application/json")
                .body(body.replace("John.Doe", traineeUsername).replace("Jane.Smith", trainerUsername))
                .when()
                .post("/training/trainee/" + traineeUsername + "/trainer/" + trainerUsername)
                .then()
                .statusCode(202)
                .extract().response();

        Training training = trainingRepository.findAll().stream()
                .filter(t -> t.getTrainingName().equals("Yoga Session"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Failed to find created training"));
        trainingId = Long.valueOf(training.getId());
        System.out.println("Training ID: " + trainingId);
    }

    @When("I send a GET request to {string}")
    public void iSendAGETRequestTo(String endpoint) {
        response = given()
                .header("Authorization", jwtToken)
                .when()
                .get(endpoint)
                .then()
                .log().all() // Log response for debugging
                .extract().response();
    }

    @When("I send a POST request to {string} with body:")
    public void iSendAPOSTRequestToWithBody(String endpoint, String body) {
        // Ensure trainerUsername is not null
        if (trainerUsername == null) {
            throw new IllegalStateException("Trainer username is not initialized");
        }
        response = given()
                .header("Authorization", jwtToken)
                .contentType("application/json")
                .body(body.replace("John.Doe", traineeUsername).replace("Jane.Smith", trainerUsername))
                .when()
                .post(endpoint)
                .then()
                .log().all()
                .extract().response();
    }

    @When("I send a DELETE request to {string}")
    public void iSendADELETERequestTo(String endpoint) {
        String updatedEndpoint = endpoint.replace("{trainingId}", trainingId.toString());
        response = given()
                .header("Authorization", jwtToken)
                .when()
                .delete(updatedEndpoint)
                .then()
                .log().all() // Log response for debugging
                .extract().response();
    }

    @Then("the response status should be {int}")
    public void theResponseStatusShouldBe(int status) {
        response.then().statusCode(status);
    }

    @Then("the response body should be {string}")
    public void theResponseBodyShouldBe(String expectedBody) {
        assertEquals(expectedBody, response.getBody().asString());
    }

    @Then("the response body should contain a list of training types")
    public void theResponseBodyShouldContainAListOfTrainingTypes() {
        response.then().body(is(not(empty())));
    }
}