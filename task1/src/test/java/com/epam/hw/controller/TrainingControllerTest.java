package com.epam.hw.controller;

import com.epam.hw.dto.CreateTrainingDTO;
import com.epam.hw.entity.TrainingType;
import com.epam.hw.monitoring.CustomMetricsService;
import com.epam.hw.service.TrainingService;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrainingControllerTest {

    @Mock
    private TrainingService trainingService;

    @Mock
    private CustomMetricsService metricsService;

    @Mock
    private Timer requestTimer;

    @Mock
    private Timer.Sample timerSample;

    @InjectMocks
    private TrainingController trainingController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup timer mocking
        when(metricsService.getRequestTimer()).thenReturn(requestTimer);
        try (MockedStatic<Timer> timerMock = mockStatic(Timer.class)) {
            timerMock.when(Timer::start).thenReturn(timerSample);
        }
    }

    @Test
    void getTrainingTypesTest() {
        List<TrainingType> mockTypes = List.of(new TrainingType("Java"), new TrainingType("Spring"));
        when(trainingService.getTrainingTypes()).thenReturn(mockTypes);

        try (MockedStatic<Timer> timerMock = mockStatic(Timer.class)) {
            timerMock.when(Timer::start).thenReturn(timerSample);

            ResponseEntity<List<TrainingType>> response = trainingController.getTrainingTypes();

            assertEquals(200, response.getStatusCodeValue());
            assertEquals(2, response.getBody().size());

            verify(trainingService).getTrainingTypes();
            verify(metricsService).recordRequest("GET", "/training/types");
            verify(timerSample).stop(requestTimer);
        }
    }

    @Test
    void addTrainingTest() {
        CreateTrainingDTO dto = new CreateTrainingDTO(
                "Java Training",
                "Java",
                "2025-01-01",
                60
        );

        try (MockedStatic<Timer> timerMock = mockStatic(Timer.class)) {
            timerMock.when(Timer::start).thenReturn(timerSample);

            ResponseEntity<String> response = trainingController.addTraining("trainee.user", "trainer.user", dto);

            assertEquals(201, response.getStatusCodeValue());
            assertEquals("Training added successfully", response.getBody());

            verify(trainingService).addTraining(
                    eq("trainee.user"),
                    eq("trainer.user"),
                    eq("Java Training"),
                    eq("Java"),
                    eq(java.time.LocalDate.of(2025, 1, 1)),
                    eq(60)
            );

            verify(metricsService).recordRequest("POST", "/training/trainee/{traineeUsername}/trainer/{trainerUsername}");
            verify(timerSample).stop(requestTimer);
        }
    }
}