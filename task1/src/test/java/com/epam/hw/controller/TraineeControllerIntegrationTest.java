//package com.epam.hw.controller;
//
//import com.epam.hw.dto.UserPasswordChangeDTO;
//import com.epam.hw.entity.Trainee;
//import com.epam.hw.repository.TraineeRepository;
//import com.epam.hw.repository.UserRepository;
//import com.epam.hw.service.TraineeService;
//import com.epam.hw.storage.Auth;
//import io.restassured.RestAssured;
//import io.restassured.http.ContentType;
//import jakarta.transaction.Transactional;
//import org.junit.jupiter.api.*;
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
//public class TraineeControllerIntegrationTest {
//
//    @LocalServerPort
//    private int port;
//
//    @Autowired
//    private Auth auth;
//
//    @Autowired
//    private TraineeService traineeService;
//
//    @Autowired
//    private UserRepository userRepository;
//    @Autowired
//    private TraineeRepository traineeRepository;
//
//    private static final String TRAINEE_USERNAME = "test.trainee";
//    private static final String PASSWORD = "testPassword";
//
//    @BeforeEach
//    public void setUp() {
//        RestAssured.port = port;
//        RestAssured.baseURI = "http://localhost";
//        auth.logOut();
//    }
//
//    @BeforeAll
//    public void setUpTestData() {
//        cleanupTestData();
//
//        Trainee trainee = traineeService.createTrainee("test", "trainee", LocalDate.of(2000, 1, 1), "testaddress");
//        userRepository.findByUsername(TRAINEE_USERNAME).ifPresent(user -> {
//            user.setPassword(PASSWORD);
//            userRepository.save(user);
//        });
//    }
//
//    @AfterAll
//    public void cleanupTestData() {
//        userRepository.findByUsername(TRAINEE_USERNAME).ifPresent(user -> {
//            traineeRepository.findByUser_Username(TRAINEE_USERNAME)
//                    .ifPresent(traineeRepository::delete);
//        });
//    }
//
//    @Test
//    @Transactional
//    public void registerTraineeTest(){
//        Map<String, Object> requestBody = new HashMap<>();
//        requestBody.put("firstName", "test");
//        requestBody.put("lastName", "trainee");
//        requestBody.put("dob", "2000-01-01");
//        requestBody.put("address", "test address");
//
//
//        RestAssured.given()
//                .contentType("application/json")
//                .body(requestBody)
//                .when()
//                .post("/trainee")
//                .then()
//                .statusCode(201);
//    }
//
//    @Test
//    public void loginTest() {
//        RestAssured.given()
//                .queryParam("username", TRAINEE_USERNAME)
//                .queryParam("password", PASSWORD)
//                .when()
//                .get("/trainee/login")
//                .then()
//                .statusCode(200);
//    }
//
//    @Test
//    public void loginTestFalseUser() {
//        RestAssured.given()
//                .queryParam("username", "fasdfas")
//                .queryParam("password", PASSWORD)
//                .when()
//                .get("/trainee/login")
//                .then()
//                .statusCode(401);
//    }
//
//    @Test
//    public void loginTestFalsePassword() {
//        RestAssured.given()
//                .queryParam("username", TRAINEE_USERNAME)
//                .queryParam("password", "wrongPassword")
//                .when()
//                .get("/trainee/login")
//                .then()
//                .statusCode(401);
//    }
//
//    @Test
//    public void changePasswordTest() {
//        auth.logIn(TRAINEE_USERNAME, PASSWORD);
//        RestAssured.given()
//                .contentType(ContentType.JSON)
//                .body(new UserPasswordChangeDTO(PASSWORD, "newTestPassword"))
//                .when()
//                .put("trainee/login/" + TRAINEE_USERNAME)
//                .then()
//                .statusCode(200);
//    }
//
//    @Test
//    public void changePasswordTestNoAuth() {
//        RestAssured.given()
//                .contentType(ContentType.JSON)
//                .body(new UserPasswordChangeDTO(PASSWORD, "newTestPassword"))
//                .when()
//                .put("trainee/login/" + TRAINEE_USERNAME)
//                .then()
//                .statusCode(401);
//    }
//
//
//}
