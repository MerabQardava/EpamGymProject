//package com.epam.hw.controller;
//
//import com.epam.hw.entity.Trainee;
//import com.epam.hw.entity.Trainer;
//import com.epam.hw.repository.TraineeRepository;
//import com.epam.hw.repository.TrainerRepository;
//import com.epam.hw.repository.UserRepository;
//import com.epam.hw.service.TraineeService;
//import com.epam.hw.service.TrainerService;
//import com.epam.hw.storage.Auth;
//import io.restassured.RestAssured;
//import io.restassured.http.ContentType;
//import org.junit.jupiter.api.AfterAll;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.TestInstance;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.web.server.LocalServerPort;
//
//import java.time.LocalDate;
//import java.util.HashMap;
//import java.util.Map;
//
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//public class TrainingControllerIntegrationTest {
//
//    @LocalServerPort
//    private int port;
//
//    @Autowired
//    private Auth auth;
//
//    @Autowired
//    private TraineeService traineeService;
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private TrainerService trainerService;
//
//    @Autowired
//    private TraineeRepository traineeRepository;
//    @Autowired
//    private TrainerRepository trainerRepository;
//
//    private static final String TRAINEE_USERNAME = "testTrainee.user";
//    private static final String TRAINER_USERNAME = "testTrainer.user";
//    private static final String PASSWORD = "testPassword";
//
//    @BeforeAll
//    public void setUpTestData() {
//        cleanupTestData();
//
//        Trainee trainee = traineeService.createTrainee("testTrainee", "user", LocalDate.of(2000, 1, 1), "testaddress");
//        userRepository.findByUsername(TRAINEE_USERNAME).ifPresent(user -> {
//            user.setPassword(PASSWORD);
//            userRepository.save(user);
//        });
//
//
//        Trainer trainer = trainerService.createTrainer("testTrainer", "user", "Java");
//        userRepository.findByUsername(TRAINER_USERNAME).ifPresent(user -> {
//            user.setPassword(PASSWORD);
//            userRepository.save(user);
//        });
//
//        System.out.println("Test data created and persisted");
//    }
//
//    @AfterAll
//    public void cleanupTestData() {
//        userRepository.findByUsername(TRAINEE_USERNAME).ifPresent(user -> {
//            traineeRepository.findByUser_Username(TRAINEE_USERNAME)
//                    .ifPresent(traineeRepository::delete);
//
//        });
//
//        userRepository.findByUsername(TRAINER_USERNAME).ifPresent(user -> {
//            trainerRepository.findByUser_Username(TRAINER_USERNAME)
//                    .ifPresent(trainerRepository::delete);
//
//        });
//
//        System.out.println("Test data cleaned up");
//    }
//
//    @BeforeEach
//    public void setUp() {
//        RestAssured.port = port;
//        RestAssured.baseURI = "http://localhost";
//        auth.logOut();
//    }
//
//    @Test
//    void testGetTrainingTypes_WithAuth() {
//        auth.logIn(TRAINEE_USERNAME, PASSWORD);
//        RestAssured.given()
//                .contentType(ContentType.JSON)
//                .when().get("/training")
//                .then().statusCode(200);
//    }
//
//    @Test
//    void testGetTrainingTypes_NoAuth() {
//        RestAssured.given()
//                .contentType(ContentType.JSON)
//                .when().get("/training")
//                .then().statusCode(401);
//    }
//
//    @Test
//    void testAddTraining_NoAuth() {
//        String requestBody = """
//            {
//                "traineeUsername": "%s",
//                "trainerUsername": "%s",
//                "trainingName": "JavaTest",
//                "trainingTypeName": "Java",
//                "date": "2024-12-15",
//                "duration": 60
//            }
//            """.formatted(TRAINEE_USERNAME, TRAINER_USERNAME);
//
//        RestAssured.given()
//                .contentType(ContentType.JSON)
//                .body(requestBody)
//                .when().post("/training")
//                .then().statusCode(401);
//    }
//
//    @Test
//    void testAddTraining_WithAuth() {
//        Map<String, Object> requestBody = new HashMap<>();
//        requestBody.put("traineeUsername", TRAINEE_USERNAME);
//        requestBody.put("trainerUsername", TRAINER_USERNAME);
//        requestBody.put("trainingName", "JavaTest");
//        requestBody.put("trainingTypeName", "Java");
//        requestBody.put("date", "2024-12-15");
//        requestBody.put("duration", 60);
//
//        auth.logIn(TRAINEE_USERNAME, PASSWORD);
//        RestAssured.given()
//                .contentType(ContentType.JSON)
//                .body(requestBody)
//                .when().post("/training")
//                .then().statusCode(201);
//    }
//}