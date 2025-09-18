package com.epam.hw.controller;

import com.epam.hw.dto.*;
import com.epam.hw.entity.*;
import com.epam.hw.monitoring.CustomMetricsService;

import com.epam.hw.security.BruteForceProtectionService;
import com.epam.hw.service.JWTService;
import com.epam.hw.service.TraineeService;
import com.epam.hw.service.UserService;
import com.epam.hw.storage.LoginResults;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TraineeController.class)
@AutoConfigureMockMvc(addFilters = false)
public class TraineeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TraineeService traineeService;

    @MockBean
    private CustomMetricsService metricsService;

    @MockBean
    private UserService userService;

    @MockBean
    private BruteForceProtectionService bruteForceProtectionService;

    @MockBean
    private JWTService jwtService;


    @MockBean
    private PasswordEncoder passwordEncoder;

//    @Test
//    void testRegisterTrainee() throws Exception {
//        TraineeRegistrationDTO dto = new TraineeRegistrationDTO("John", "Doe", "2000-01-01", "Tbilisi");
//        Trainee trainee = new Trainee();
//        User user = new User();
//        user.setUsername("john.doe");
//        user.setPassword("pass123");
//        trainee.setUser(user);
//
//        Timer requestTimer = mock(Timer.class);
//        Timer.Sample timerSample = mock(Timer.Sample.class);
//
//        when(metricsService.getRequestTimer()).thenReturn(requestTimer);
//        when(traineeService.createTrainee(anyString(), anyString(), any(LocalDate.class), anyString())).thenReturn(trainee);
//
//        try (MockedStatic<Timer> timerMock = mockStatic(Timer.class)) {
//            timerMock.when(Timer::start).thenReturn(timerSample);
//
//            mockMvc.perform(post("/trainee")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(dto)))
//                    .andExpect(status().isCreated())
//                    .andExpect(jsonPath("$.username").value("john.doe"))
//                    .andExpect(jsonPath("$.password").value("pass123"));
//        }
//    }

//    @Test
//    void testLogin_Success() throws Exception {
//        Timer requestTimer = mock(Timer.class);
//        Timer.Sample timerSample = mock(Timer.Sample.class);
//
//        when(metricsService.getRequestTimer()).thenReturn(requestTimer);
//        when(traineeService.logIn("john.doe", "pass123")).thenReturn(LoginResults.SUCCESS);
//
//        try (MockedStatic<Timer> timerMock = mockStatic(Timer.class)) {
//            timerMock.when(Timer::start).thenReturn(timerSample);
//
//            mockMvc.perform(get("/trainee/login")
//                            .param("username", "john.doe")
//                            .param("password", "pass123"))
//                    .andExpect(status().isOk())
//                    .andExpect(content().string("Login successful"));
//        }
//    }
//
//    @Test
//    void testLogin_BadPassword() throws Exception {
//        Timer requestTimer = mock(Timer.class);
//        Timer.Sample timerSample = mock(Timer.Sample.class);
//
//        when(metricsService.getRequestTimer()).thenReturn(requestTimer);
//        when(traineeService.logIn("john.doe", "wrong")).thenReturn(LoginResults.BAD_PASSWORD);
//
//        try (MockedStatic<Timer> timerMock = mockStatic(Timer.class)) {
//            timerMock.when(Timer::start).thenReturn(timerSample);
//
//            mockMvc.perform(get("/trainee/login")
//                            .param("username", "john.doe")
//                            .param("password", "wrong"))
//                    .andExpect(status().isUnauthorized())
//                    .andExpect(content().string("Invalid Credentials"));
//        }
//    }
//
//    @Test
//    void testLoginUser_NotFound() throws Exception {
//        Timer requestTimer = mock(Timer.class);
//        Timer.Sample timerSample = mock(Timer.Sample.class);
//
//        when(metricsService.getRequestTimer()).thenReturn(requestTimer);
//        when(traineeService.logIn("nonexistent", "pass"))
//                .thenReturn(LoginResults.USER_NOT_FOUND);
//
//        try (MockedStatic<Timer> timerMock = mockStatic(Timer.class)) {
//            timerMock.when(Timer::start).thenReturn(timerSample);
//
//            mockMvc.perform(get("/trainee/login")
//                            .param("username", "nonexistent")
//                            .param("password", "pass"))
//                    .andExpect(status().isUnauthorized())
//                    .andExpect(content().string("Trainee with username of nonexistent not found"));
//        }
//    }

    @Test
    void testChangePassword_Success() throws Exception {
        UserPasswordChangeDTO dto = new UserPasswordChangeDTO("oldPass", "newPass");
        Trainee trainee = new Trainee();
        User user = new User();
        user.setUsername("john.doe");
        user.setPassword("$2a$10$exampleHashForOldPass1234567890"); // Simulate a valid BCrypt hash
        trainee.setUser(user);

        Timer requestTimer = mock(Timer.class);
        Timer.Sample timerSample = mock(Timer.Sample.class);

        when(metricsService.getRequestTimer()).thenReturn(requestTimer);
        when(traineeService.getTraineeByUsername("john.doe")).thenReturn(trainee);
        when(passwordEncoder.matches("oldPass", "$2a$10$exampleHashForOldPass1234567890")).thenReturn(true);
        when(userService.changeTraineePassword("john.doe", "newPass")).thenReturn(true);

        try (MockedStatic<Timer> timerMock = mockStatic(Timer.class)) {
            timerMock.when(Timer::start).thenReturn(timerSample);

            mockMvc.perform(put("/trainee/login/john.doe")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Password changed successfully"));

            verify(passwordEncoder, times(1)).matches("oldPass", "$2a$10$exampleHashForOldPass1234567890");
            verify(userService, times(1)).changeTraineePassword("john.doe", "newPass");
        }
    }

    @Test
    void testChangePassword_Invalid() throws Exception {
        UserPasswordChangeDTO dto = new UserPasswordChangeDTO("wrongPass", "newPass");
        Trainee trainee = new Trainee();
        User user = new User();
        user.setPassword("correctPass");
        trainee.setUser(user);

        Timer requestTimer = mock(Timer.class);
        Timer.Sample timerSample = mock(Timer.Sample.class);

        when(metricsService.getRequestTimer()).thenReturn(requestTimer);
        when(traineeService.getTraineeByUsername("john.doe")).thenReturn(trainee);

        try (MockedStatic<Timer> timerMock = mockStatic(Timer.class)) {
            timerMock.when(Timer::start).thenReturn(timerSample);

            mockMvc.perform(put("/trainee/login/john.doe")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().string("Invalid current password"));
        }
    }

    @Test
    void testGetTraineeProfile_Success() throws Exception{
        User traineeUser = new User();
        traineeUser.setFirstName("John");
        traineeUser.setLastName("Doe");
        traineeUser.setUserId(1);
        traineeUser.setUsername("John.Doe");

        Trainee trainee = new Trainee(LocalDate.of(2000, 1, 1), "Tbilisi", traineeUser);

        Timer requestTimer = mock(Timer.class);
        Timer.Sample timerSample = mock(Timer.Sample.class);

        when(metricsService.getRequestTimer()).thenReturn(requestTimer);
        when(traineeService.getTraineeByUsername("John.Doe")).thenReturn(trainee);

        try (MockedStatic<Timer> timerMock = mockStatic(Timer.class)) {
            timerMock.when(Timer::start).thenReturn(timerSample);

            mockMvc.perform(get("/trainee/John.Doe"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.firstName").value("John"))
                    .andExpect(jsonPath("$.lastName").value("Doe"))
                    .andExpect(jsonPath("$.dob").value("2000-01-01"))
                    .andExpect(jsonPath("$.address").value("Tbilisi"));
        }
    }

    @Test
    void testDeleteTrainee_Success() throws Exception {
        String username = "John.Doe";

        Timer requestTimer = mock(Timer.class);
        Timer.Sample timerSample = mock(Timer.Sample.class);

        when(metricsService.getRequestTimer()).thenReturn(requestTimer);
        when(traineeService.deleteByUsername(username)).thenReturn(true);

        try (MockedStatic<Timer> timerMock = mockStatic(Timer.class)) {
            timerMock.when(Timer::start).thenReturn(timerSample);

            mockMvc.perform(delete("/trainee/" + username))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Trainee with username John.Doe deleted successfully"));

            verify(traineeService, times(1)).deleteByUsername(username);
        }
    }

    @Test
    void testDeleteTrainee_NotFound() throws Exception {
        String username = "NonExistent.User";

        Timer requestTimer = mock(Timer.class);
        Timer.Sample timerSample = mock(Timer.Sample.class);

        when(metricsService.getRequestTimer()).thenReturn(requestTimer);
        when(traineeService.deleteByUsername(username)).thenReturn(false);

        try (MockedStatic<Timer> timerMock = mockStatic(Timer.class)) {
            timerMock.when(Timer::start).thenReturn(timerSample);

            mockMvc.perform(delete("/trainee/" + username))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string("Trainee with username NonExistent.User not found"));

            verify(traineeService, times(1)).deleteByUsername(username);
        }
    }

    @Test
    void testToggleTraineeStatus_Success() throws Exception {
        String username = "John.Doe";

        Timer requestTimer = mock(Timer.class);
        Timer.Sample timerSample = mock(Timer.Sample.class);

        when(metricsService.getRequestTimer()).thenReturn(requestTimer);

        try (MockedStatic<Timer> timerMock = mockStatic(Timer.class)) {
            timerMock.when(Timer::start).thenReturn(timerSample);

            mockMvc.perform(patch("/trainee/{username}/status", username))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Trainee status toggled successfully"));

            verify(traineeService, times(1)).toggleTraineeStatus(username);
        }
    }

    @Test
    void testUpdateTraineeProfile_Success() throws Exception {
        String username = "john.doe";
        UpdateTraineeDTO dto = new UpdateTraineeDTO("John", "Doe", "2000-01-01", "Tbilisi", true);
        User user = new User();
        user.setUsername(username);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setActive(true);
        Trainee updated = new Trainee(LocalDate.parse("2000-01-01"), "Tbilisi", user);

        Timer requestTimer = mock(Timer.class);
        Timer.Sample timerSample = mock(Timer.Sample.class);

        when(metricsService.getRequestTimer()).thenReturn(requestTimer);
        when(traineeService.updateTraineeProfile(eq(username), any(UpdateTraineeDTO.class)))
                .thenReturn(updated);

        try (MockedStatic<Timer> timerMock = mockStatic(Timer.class)) {
            timerMock.when(Timer::start).thenReturn(timerSample);

            mockMvc.perform(put("/trainee/" + username)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("john.doe"))
                    .andExpect(jsonPath("$.firstName").value("John"))
                    .andExpect(jsonPath("$.lastName").value("Doe"))
                    .andExpect(jsonPath("$.dob").value("2000-01-01"))
                    .andExpect(jsonPath("$.address").value("Tbilisi"))
                    .andExpect(jsonPath("$.isActive").value(true));
        }
    }

    @Test
    void testUpdateTrainerList_success() throws Exception {
        String username = "john.doe";
        Set<String> trainerUsernames = Set.of("trainer1");

        User trainerUser = new User();
        trainerUser.setUsername("trainer1");
        trainerUser.setFirstName("Trainer");
        trainerUser.setLastName("One");

        TrainingType trainingType = new TrainingType();
        trainingType.setTrainingTypeName("Java");

        Trainer trainer = new Trainer();
        trainer.setUser(trainerUser);
        trainer.setSpecializationId(trainingType);

        Timer requestTimer = mock(Timer.class);
        Timer.Sample timerSample = mock(Timer.Sample.class);

        when(metricsService.getRequestTimer()).thenReturn(requestTimer);
        when(traineeService.updateTraineeTrainers(eq(username), anySet()))
                .thenReturn(Set.of(trainer));

        try (MockedStatic<Timer> timerMock = mockStatic(Timer.class)) {
            timerMock.when(Timer::start).thenReturn(timerSample);

            mockMvc.perform(put("/trainee/" + username + "/trainers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(trainerUsernames)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].username").value("trainer1"))
                    .andExpect(jsonPath("$[0].firstName").value("Trainer"))
                    .andExpect(jsonPath("$[0].lastName").value("One"));
        }
    }

    @Test
    void testGetTraineeTrainings_success() throws Exception {
        String username = "john.doe";

        // Setup Trainee
        User traineeUser = new User();
        traineeUser.setUsername(username);
        Trainee trainee = new Trainee();
        trainee.setUser(traineeUser);

        // Setup Trainer
        User trainerUser = new User();
        trainerUser.setUsername("trainer1");
        Trainer trainer = new Trainer();
        trainer.setUser(trainerUser);

        // Setup TrainingType
        TrainingType trainingType = new TrainingType();
        trainingType.setTrainingTypeName("Java");

        // Setup Training
        Training training = new Training(trainee, trainer, "OOP Basics", trainingType, LocalDate.of(2024, 6, 1), 90);

        Timer requestTimer = mock(Timer.class);
        Timer.Sample timerSample = mock(Timer.Sample.class);

        when(metricsService.getRequestTimer()).thenReturn(requestTimer);
        when(traineeService.getTraineeByUsername(username)).thenReturn(trainee);
        when(traineeService.getTraineeTrainings(eq(username), any(), any(), any(), any()))
                .thenReturn(List.of(training));

        try (MockedStatic<Timer> timerMock = mockStatic(Timer.class)) {
            timerMock.when(Timer::start).thenReturn(timerSample);

            mockMvc.perform(get("/trainee/{username}/trainings", username))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].name").value("OOP Basics"))
                    .andExpect(jsonPath("$[0].date").value("2024-06-01"))
                    .andExpect(jsonPath("$[0].trainingType.trainingTypeName").value("Java"))
                    .andExpect(jsonPath("$[0].duration").value(90))
                    .andExpect(jsonPath("$[0].trainerName").value("trainer1"));
        }
    }
}
