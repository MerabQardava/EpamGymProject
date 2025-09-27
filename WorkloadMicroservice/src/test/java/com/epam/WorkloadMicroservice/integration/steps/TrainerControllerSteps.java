package com.epam.WorkloadMicroservice.integration.steps;

import com.epam.WorkloadMicroservice.dto.UpdateWorkingHoursDTO;
import com.epam.WorkloadMicroservice.entity.Trainer;
import com.epam.WorkloadMicroservice.repository.TrainerRepository;
import com.epam.WorkloadMicroservice.service.TrainerService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TrainerControllerSteps {

    @LocalServerPort
    private int port;

    private Response response;
    private String trainerUsername = "Jane.Smith";

    @Autowired
    private TrainerService trainerService;

    @Autowired
    private TrainerRepository trainerRepository;

    @Given("the workload microservice is running")
    public void workloadMicroserviceIsRunning() {
        RestAssured.port = port;
    }

    @Given("a trainer with username {string} exists")
    public void trainerWithUsernameExists(String username) {
        Trainer trainer = new Trainer(username, "Jane", "Smith");
        trainerRepository.save(trainer);
    }

    @Given("a training session exists for trainer {string} on {string} with duration {int}")
    public void trainingSessionExistsForTrainer(String username, String date, int duration) {
        trainerService.addTraining(username, new UpdateWorkingHoursDTO("Jane", "Smith",true, LocalDate.parse(date), duration));
    }

    @When("I send a POST request to {string} with body:")
    public void iSendAPOSTRequestToWithBody(String endpoint, String body) {
        response = given()
                .contentType("application/json")
                .body(body.replace("Jane.Smith", trainerUsername))
                .when()
                .post(endpoint)
                .then()
                .log().all()
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
}