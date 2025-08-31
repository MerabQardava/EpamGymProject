package com.epam.hw.service;

import com.epam.hw.dto.ActionType;
import com.epam.hw.dto.UpdateWorkingHoursDTO;
import com.epam.hw.entity.Trainee;
import com.epam.hw.entity.Trainer;
import com.epam.hw.entity.Training;
import com.epam.hw.entity.TrainingType;
import com.epam.hw.feign.WorkloadInterface;
import com.epam.hw.messaging.MessageProducer;
import com.epam.hw.repository.TraineeRepository;
import com.epam.hw.repository.TrainerRepository;
import com.epam.hw.repository.TrainingRepository;
import com.epam.hw.repository.TrainingTypeRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class TrainingService {

    private static final Logger logger = LoggerFactory.getLogger(TrainingService.class);

    private final TrainingRepository trainingRepo;
    private final TraineeRepository traineeRepo;
    private final TrainerRepository trainerRepo;
    private final TrainingTypeRepository trainingTypeRepo;
    private final MessageProducer messageProducer;


    private WorkloadInterface workloadInterface;

    @Autowired
    public TrainingService(TrainingRepository trainingRepo,
                           TraineeRepository traineeRepo,
                           TrainerRepository trainerRepo,
                           TrainingTypeRepository trainingTypeRepo,
                           WorkloadInterface workloadInterface,
                           MessageProducer messageProducer) {
        this.trainingRepo = trainingRepo;
        this.traineeRepo = traineeRepo;
        this.trainerRepo = trainerRepo;
        this.trainingTypeRepo = trainingTypeRepo;
        this.workloadInterface = workloadInterface;
        this.messageProducer = messageProducer;
    }

    @Transactional
    @CircuitBreaker(name = "workloadService", fallbackMethod = "addTrainingFallback")
    public Training addTraining(String traineeUsername, String trainerUsername, String trainingName, String trainingTypeName, LocalDate date, Integer duration) {
        logger.debug("Attempting to add training for trainee={}, trainer={}, trainingType={}, date={}, duration={}",
                traineeUsername, trainerUsername, trainingTypeName, date, duration);

        Trainee trainee = traineeRepo.findByUser_Username(traineeUsername).orElseThrow(() -> {
            logger.warn("Trainee not found with ID: {}", traineeUsername);
            return new EntityNotFoundException("Trainee not found");
        });

        Trainer trainer = trainerRepo.findByUser_Username(trainerUsername).orElseThrow(() -> {
            logger.warn("Trainer not found with ID: {}", trainerUsername);
            return new EntityNotFoundException("Trainer not found");
        });

        TrainingType trainingType = trainingTypeRepo.findByTrainingTypeName(trainingTypeName).orElseThrow(() -> {
            logger.warn("TrainingType not found with name: {}", trainingTypeName);
            return new EntityNotFoundException("TrainingType not found");
        });

        Training training = new Training(trainee, trainer, trainingName, trainingType, date, duration);
        Training savedTraining = trainingRepo.save(training);

        logger.info("Training created with ID: {} for trainee: {}, trainer: {}", savedTraining.getId(), traineeUsername, trainerUsername);
        UpdateWorkingHoursDTO requestBody = new UpdateWorkingHoursDTO(
                trainer.getUser().getFirstName(),
                trainer.getUser().getLastName(),
                true,
                date,
                duration
        );

        ResponseEntity<String> response = workloadInterface.updateWorkingHours(trainerUsername, "ADD", requestBody);
        messageProducer.sendMessage(requestBody, ActionType.ADD);
        if (!response.getStatusCode().is2xxSuccessful()) {
            logger.warn("Failed to update workload for trainer: {}, status: {}", trainerUsername, response.getStatusCode());
            throw new RuntimeException("Failed to update workload: " + response.getStatusCode());
        }
        logger.info("Workload updated for trainer: {} with action: ADD, duration: {}", trainerUsername, duration);

        return savedTraining;
    }

    public Training addTrainingFallback(String traineeUsername, String trainerUsername, String trainingName,
                                        String trainingTypeName, LocalDate date, Integer duration, Throwable t) {
        logger.error("Circuit breaker fallback for addTraining, trainer: {} - {}", trainerUsername, t.getMessage());
        throw new RuntimeException("Workload service unavailable: cannot add training for " + trainerUsername);
    }

    @Transactional
    @CircuitBreaker(name = "workloadService", fallbackMethod = "deleteTrainingFallback")
    public void deleteTraining(int trainingId) {
        logger.debug("Attempting to delete training with ID: {}", trainingId);

        Training training = trainingRepo.findById(trainingId).orElseThrow(() -> {
            logger.warn("Training not found with ID: {}", trainingId);
            return new EntityNotFoundException("Training not found");
        });

        Trainer trainer = training.getTrainer();
        String trainerUsername = trainer.getUser().getUsername();
        LocalDate date = training.getDate();
        Integer duration = training.getDuration();

        trainingRepo.delete(training);
        messageProducer.sendMessage(training, ActionType.REMOVE);
        logger.info("Training deleted with ID: {} for trainer: {}", trainingId, trainerUsername);

        UpdateWorkingHoursDTO requestBody = new UpdateWorkingHoursDTO(
                trainer.getUser().getFirstName(),
                trainer.getUser().getLastName(),
                true,
                date,
                duration
        );

        ResponseEntity<String> response = workloadInterface.updateWorkingHours(trainerUsername, "REMOVE", requestBody);
        if (!response.getStatusCode().is2xxSuccessful()) {
            logger.warn("Failed to update workload for trainer: {}, status: {}", trainerUsername, response.getStatusCode());
            throw new RuntimeException("Failed to update workload: " + response.getStatusCode());
        }
        logger.info("Workload updated for trainer: {} with action: REMOVE, duration: {}", trainerUsername, duration);
    }

    public void deleteTrainingFallback(int trainingId, Throwable t) {
        logger.error("Circuit breaker fallback for deleteTraining, trainingId: {} - {}", trainingId, t.getMessage());
        throw new RuntimeException("Workload service unavailable: cannot delete training " + trainingId);
    }

    public List<TrainingType> getTrainingTypes() {
        return trainingTypeRepo.findAll();
    }
}