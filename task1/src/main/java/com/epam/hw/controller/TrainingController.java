package com.epam.hw.controller;

import com.epam.hw.dto.CreateTrainingDTO;
import com.epam.hw.entity.TrainingType;
import com.epam.hw.monitoring.CustomMetricsService;
import com.epam.hw.service.TrainingService;
import io.micrometer.core.instrument.Timer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Training Controller", description = "Endpoints for managing trainings")
@RestController
@RequestMapping("/training")
@Slf4j
public class TrainingController {
    private final TrainingService trainingService;
    private final CustomMetricsService metricsService;

    @Autowired
    public TrainingController(TrainingService trainingService, CustomMetricsService metricsService) {
        this.trainingService = trainingService;
        this.metricsService = metricsService;
    }

    @Operation(summary = "Get list of all available training types")
    @GetMapping("/types")
    public ResponseEntity<List<TrainingType>> getTrainingTypes() {
        metricsService.recordRequest("GET", "/training/types");
        Timer.Sample sample = Timer.start();
        log.info("GET /training - Fetching all training types");
        List<TrainingType> types = trainingService.getTrainingTypes();
        log.info("Retrieved {} training types", types.size());
        sample.stop(metricsService.getRequestTimer());
        return ResponseEntity.ok(types);
    }

    @Operation(summary = "Create a new training session between trainee and trainer")
    @PostMapping("/trainee/{traineeUsername}/trainer/{trainerUsername}")
    public ResponseEntity<String> addTraining(
            @PathVariable String traineeUsername,
            @PathVariable String trainerUsername,
            @RequestBody @Valid CreateTrainingDTO dto) {
        metricsService.recordRequest("POST", "/training/trainee/{traineeUsername}/trainer/{trainerUsername}");
        Timer.Sample sample = Timer.start();
        log.info("POST /training/trainee/{}/trainer/{} - Creating training: type={}, date={}, duration={}",
                traineeUsername, trainerUsername, dto.trainingTypeName(), dto.date(), dto.duration());

        try {
            trainingService.addTraining(traineeUsername, trainerUsername, dto.trainingName(),
                    dto.trainingTypeName(), LocalDate.parse(dto.date()), dto.duration());
            log.info("Training created successfully for trainee {} and trainer {}", traineeUsername, trainerUsername);
            sample.stop(metricsService.getRequestTimer());
            return ResponseEntity.status(HttpStatus.ACCEPTED).body("Training creation request accepted");
        } catch (EntityNotFoundException e) {
            log.warn("Failed to create training for trainee: {}, trainer: {} - {}", traineeUsername, trainerUsername, e.getMessage());
            sample.stop(metricsService.getRequestTimer());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RuntimeException e) {
            log.warn("Failed to create training for trainee: {}, trainer: {} - {}", traineeUsername, trainerUsername, e.getMessage());
            sample.stop(metricsService.getRequestTimer());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(e.getMessage());
        }
    }

    @Operation(summary = "Delete a training session by ID")
    @DeleteMapping("/{trainingId}")
    public ResponseEntity<String> deleteTraining(@PathVariable int trainingId) {
        metricsService.recordRequest("DELETE", "/training/{trainingId}");
        Timer.Sample sample = Timer.start();
        log.info("DELETE /training/{} - Deleting training", trainingId);

        try {
            trainingService.deleteTraining(trainingId);
            log.info("Training deleted successfully with ID: {}", trainingId);
            sample.stop(metricsService.getRequestTimer());
            return ResponseEntity.status(HttpStatus.ACCEPTED).body("Training deletion request accepted");
        } catch (EntityNotFoundException e) {
            log.warn("Failed to delete training with ID: {} - {}", trainingId, e.getMessage());
            sample.stop(metricsService.getRequestTimer());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Training not found with ID: " + trainingId);
        } catch (RuntimeException e) {
            log.warn("Failed to delete training with ID: {} - {}", trainingId, e.getMessage());
            sample.stop(metricsService.getRequestTimer());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(e.getMessage());
        }
    }
}