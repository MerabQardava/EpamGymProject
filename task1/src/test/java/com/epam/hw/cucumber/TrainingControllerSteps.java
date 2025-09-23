package com.epam.hw.cucumber;

import com.epam.hw.Task1Application;
import com.epam.hw.entity.Training;
import com.epam.hw.entity.TrainingType;
import com.epam.hw.repository.TrainingRepository;
import com.epam.hw.repository.TrainingTypeRepository;
import com.epam.hw.service.TrainingService;
import com.epam.hw.service.UserService;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@CucumberContextConfiguration
@SpringBootTest(classes = Task1Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
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

    @Autowired
    private PlatformTransactionManager transactionManager;

    private TransactionStatus transactionStatus;

    @Before
    public void setup() {
        RestAssured.port = port;
        // Start a new transaction for the scenario (including seeding and user creation)
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        transactionStatus = transactionManager.getTransaction(def);

        // Idempotent seeding: Only create "YOGA" if it doesn't exist
        if (trainingTypeRepository.findByTrainingTypeName("YOGA").isEmpty()) {
            TrainingType yoga = new TrainingType();
            yoga.setTrainingTypeName("YOGA");
            trainingTypeRepository.save(yoga);
            System.out.println("Seeded new TrainingType: YOGA");
        } else {
            System.out.println("TrainingType YOGA already exists, skipping seed");
        }
        // Register test user and token
        setupTestUserAndToken();
    }

    @After
    public void teardown() {
        // Always rollback the transaction after the scenario (even on failure)
        if (transactionStatus != null) {
            transactionManager.rollback(transactionStatus);
            System.out.println("Rolled back transaction for scenario");
        }
    }

    private void setupTestUserAndToken() {
        // Register a test trainee
        traineeUsername = userService.register("John", "Doe", LocalDate.of(1990, 1, 1), "123 Test St", "testPassword")
                .getUser().getUsername();
        System.out.println("Trainee username: " + traineeUsername);
        // Register a test trainer
        trainerUsername = userService.register("Jane", "Smith", "YOGA", "trainerPassword")
                .getUser().getUsername();
        System.out.println("Trainer username: " + trainerUsername);
        // Generate JWT token for the test trainee
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
        // No-op, handled by SpringBootTest
    }

    @Given("a training with ID {int} exists")
    public void aTrainingWithIdExists(int id) {
        // Create a test training
        trainingService.addTraining(
                traineeUsername,
                trainerUsername,
                "Yoga Session",
                "YOGA",
                LocalDate.now(),
                60
        );
        // Query the database to get the latest training ID
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
                .log().all() // Log response for debugging
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