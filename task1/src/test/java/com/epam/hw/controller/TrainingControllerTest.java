package com.epam.hw.controller;

import com.epam.hw.dto.CreateTrainingDTO;
import com.epam.hw.entity.Training;
import com.epam.hw.entity.TrainingType;
import com.epam.hw.monitoring.CustomMetricsService;
import com.epam.hw.service.JWTService;
import com.epam.hw.service.TrainingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TrainingController.class)
@AutoConfigureMockMvc(addFilters = false)
class TrainingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TrainingService trainingService;

    @MockBean
    private CustomMetricsService metricsService;

    @MockBean
    private JWTService jwtService;


    @BeforeEach
    void setUp() {
        // Reset mocks before each test
        reset(trainingService, metricsService);
    }

    @Test
    void testGetTrainingTypes_Success() throws Exception {
        List<TrainingType> mockTypes = List.of(
                new TrainingType("Java"),
                new TrainingType("Spring")
        );
        Timer requestTimer = mock(Timer.class);
        Timer.Sample timerSample = mock(Timer.Sample.class);

        when(metricsService.getRequestTimer()).thenReturn(requestTimer);
        when(trainingService.getTrainingTypes()).thenReturn(mockTypes);

        try (MockedStatic<Timer> timerMock = mockStatic(Timer.class)) {
            timerMock.when(Timer::start).thenReturn(timerSample);

            mockMvc.perform(get("/training/types")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].trainingTypeName").value("Java"))
                    .andExpect(jsonPath("$[1].trainingTypeName").value("Spring"));

            verify(metricsService, times(1)).recordRequest("GET", "/training/types");
            verify(trainingService, times(1)).getTrainingTypes();
            verify(timerSample, times(1)).stop(requestTimer);
        }
    }

    @Test
    void testGetTrainingTypes_EmptyList() throws Exception {
        Timer requestTimer = mock(Timer.class);
        Timer.Sample timerSample = mock(Timer.Sample.class);

        when(metricsService.getRequestTimer()).thenReturn(requestTimer);
        when(trainingService.getTrainingTypes()).thenReturn(List.of());

        try (MockedStatic<Timer> timerMock = mockStatic(Timer.class)) {
            timerMock.when(Timer::start).thenReturn(timerSample);

            mockMvc.perform(get("/training/types")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());

            verify(metricsService, times(1)).recordRequest("GET", "/training/types");
            verify(trainingService, times(1)).getTrainingTypes();
            verify(timerSample, times(1)).stop(requestTimer);
        }
    }

    @Test
    void testAddTraining_Success() throws Exception {
        CreateTrainingDTO dto = new CreateTrainingDTO(
                "Java Training",
                "Java",
                "2025-01-01",
                60
        );
        Timer requestTimer = mock(Timer.class);
        Timer.Sample timerSample = mock(Timer.Sample.class);

        when(metricsService.getRequestTimer()).thenReturn(requestTimer);
        when(trainingService.addTraining(
                eq("trainee.user"),
                eq("trainer.user"),
                eq("Java Training"),
                eq("Java"),
                eq(LocalDate.of(2025, 1, 1)),
                eq(60)
        )).thenReturn(new Training()); // Mock returning a Training object

        try (MockedStatic<Timer> timerMock = mockStatic(Timer.class)) {
            timerMock.when(Timer::start).thenReturn(timerSample);

            mockMvc.perform(post("/training/trainee/trainee.user/trainer/trainer.user")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isAccepted())
                    .andExpect(content().string("Training creation request accepted"));

            verify(metricsService, times(1)).recordRequest("POST", "/training/trainee/{traineeUsername}/trainer/{trainerUsername}");
            verify(trainingService, times(1)).addTraining(
                    eq("trainee.user"),
                    eq("trainer.user"),
                    eq("Java Training"),
                    eq("Java"),
                    eq(LocalDate.of(2025, 1, 1)),
                    eq(60)
            );
            verify(timerSample, times(1)).stop(requestTimer);
        }
    }

//    @Test
//    void testAddTraining_InvalidInput() throws Exception {
//        CreateTrainingDTO dto = new CreateTrainingDTO(
//                "", // Invalid: empty training name
//                "Java",
//                "2025-01-01",
//                60
//        );
//        Timer requestTimer = mock(Timer.class);
//        Timer.Sample timerSample = mock(Timer.Sample.class);
//
//        when(metricsService.getRequestTimer()).thenReturn(requestTimer);
//
//        try (MockedStatic<Timer> timerMock = mockStatic(Timer.class)) {
//            timerMock.when(Timer::start).thenReturn(timerSample);
//
//            mockMvc.perform(post("/training/trainee/trainee.user/trainer/trainer.user")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(dto)))
//                    .andExpect(status().isBadRequest())
//                    .andExpect(content().string("must not be blank")); // Matches @NotBlank default message
//
//            verify(metricsService, times(1)).recordRequest("POST", "/training/trainee/{traineeUsername}/trainer/{trainerUsername}");
//            verify(trainingService, never()).addTraining(anyString(), anyString(), anyString(), anyString(), any(), anyInt());
//            verify(timerSample, times(1)).stop(requestTimer);
//        }
//    }

    @Test
    void testDeleteTraining_Success() throws Exception {
        int trainingId = 1;
        Timer requestTimer = mock(Timer.class);
        Timer.Sample timerSample = mock(Timer.Sample.class);

        when(metricsService.getRequestTimer()).thenReturn(requestTimer);
        doNothing().when(trainingService).deleteTraining(trainingId);

        try (MockedStatic<Timer> timerMock = mockStatic(Timer.class)) {
            timerMock.when(Timer::start).thenReturn(timerSample);

            mockMvc.perform(delete("/training/" + trainingId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isAccepted())
                    .andExpect(content().string("Training deletion request accepted"));

            verify(metricsService, times(1)).recordRequest("DELETE", "/training/{trainingId}");
            verify(trainingService, times(1)).deleteTraining(trainingId);
            verify(timerSample, times(1)).stop(requestTimer);
        }
    }

    @Test
    void testDeleteTraining_NotFound() throws Exception {
        int trainingId = 999;
        Timer requestTimer = mock(Timer.class);
        Timer.Sample timerSample = mock(Timer.Sample.class);

        when(metricsService.getRequestTimer()).thenReturn(requestTimer);
        doThrow(new EntityNotFoundException("Training not found")).when(trainingService).deleteTraining(trainingId);

        try (MockedStatic<Timer> timerMock = mockStatic(Timer.class)) {
            timerMock.when(Timer::start).thenReturn(timerSample);

            mockMvc.perform(delete("/training/" + trainingId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string("Training not found with ID: " + trainingId));

            verify(metricsService, times(1)).recordRequest("DELETE", "/training/{trainingId}");
            verify(trainingService, times(1)).deleteTraining(trainingId);
            verify(timerSample, times(1)).stop(requestTimer);
        }
    }

    @Test
    void testDeleteTraining_ServiceUnavailable() throws Exception {
        int trainingId = 1;
        String errorMessage = "Database connection failed";
        Timer requestTimer = mock(Timer.class);
        Timer.Sample timerSample = mock(Timer.Sample.class);

        when(metricsService.getRequestTimer()).thenReturn(requestTimer);
        doThrow(new RuntimeException(errorMessage)).when(trainingService).deleteTraining(trainingId);

        try (MockedStatic<Timer> timerMock = mockStatic(Timer.class)) {
            timerMock.when(Timer::start).thenReturn(timerSample);

            mockMvc.perform(delete("/training/" + trainingId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isServiceUnavailable())
                    .andExpect(content().string(errorMessage));

            verify(metricsService, times(1)).recordRequest("DELETE", "/training/{trainingId}");
            verify(trainingService, times(1)).deleteTraining(trainingId);
            verify(timerSample, times(1)).stop(requestTimer);
        }
    }
}