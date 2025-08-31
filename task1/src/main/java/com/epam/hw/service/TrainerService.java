package com.epam.hw.service;

import com.epam.hw.dto.ActionType;
import com.epam.hw.dto.GetWorkingHoursDTO;
import com.epam.hw.dto.UpdateTrainerDTO;
import com.epam.hw.entity.Trainee;
import com.epam.hw.entity.Trainer;
import com.epam.hw.entity.Training;
import com.epam.hw.entity.User;

import com.epam.hw.messaging.MessageProducer;
import com.epam.hw.repository.TrainerRepository;
import com.epam.hw.repository.TrainingRepository;
import com.epam.hw.repository.UserRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TrainerService {

    private static final Logger logger = LoggerFactory.getLogger(TrainerService.class);

    private final TrainerRepository trainerRepository;
    private final UserRepository userRepository;
    private final TrainingRepository trainingRepository;
    private final MessageProducer messageProducer;



    public TrainerService(TrainerRepository trainerRepository,
                          UserRepository userRepository,
                          TrainingRepository trainingRepository,
                          MessageProducer messageProducer) {
        this.trainerRepository = trainerRepository;
        this.userRepository = userRepository;
        this.trainingRepository = trainingRepository;
        this.messageProducer = messageProducer;
    }

    public Trainer getTrainerByUsername(String username) {
        logger.debug("Fetching trainer by username: {}", username);

        return trainerRepository.findByUser_Username(username).orElseThrow(() -> {
            logger.warn("No trainer found for username: {}", username);
            return new EntityNotFoundException("Trainer not found for username: " + username);
        });
    }

    public boolean toggleTrainerStatus(String username) {
        Trainer trainer = getTrainerByUsername(username);

        User user = trainer.getUser();
        boolean newStatus = !user.isActive();
        user.setActive(newStatus);

        userRepository.save(user);
        logger.info("Trainer status toggled to: {}", newStatus ? "ACTIVE" : "INACTIVE");
        return newStatus;
    }

    public Trainer updateTrainerProfile(String username, UpdateTrainerDTO dto) {
        Trainer trainer = getTrainerByUsername(username);

        User user = trainer.getUser();
        user.setFirstName(dto.firstName());
        user.setLastName(dto.lastName());

        if (dto.isActive() != user.isActive()) {
            toggleTrainerStatus(username);
        }

        trainerRepository.save(trainer);
        logger.info("Updated profile for trainer: {}", username);
        return trainer;
    }

    public List<Training> getTrainerTrainings(String username, LocalDate from, LocalDate to, String traineeName) {
        logger.debug("Fetching trainings for trainer: {} from {} to {} (trainee filter: {})",
                username, from, to, traineeName);

        return trainingRepository.findTrainingsByTrainerCriteria(username, from, to, traineeName);
    }

    public List<Trainer> getUnassignedTraineeTrainers(String username) {
        logger.debug("Fetching unassigned trainers for trainee: {}", username);

        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            Trainee trainee = user.get().getTrainee();

            if (trainee == null) {
                logger.warn("No trainee found for user: {}", username);
                throw new EntityNotFoundException("Trainee not found: " + username);
            }

            List<Trainer> allTrainers = trainerRepository.findAll();
            List<Trainer> unassignedTrainers = new ArrayList<>(allTrainers);
            unassignedTrainers.removeAll(trainee.getTrainers());
            unassignedTrainers.removeIf(trainer -> !trainer.getUser().isActive());

            logger.info("Unassigned trainers returned for trainee: {}", username);
            return unassignedTrainers;
        }

        logger.warn("No trainee found with username: {}", username);
        throw new EntityNotFoundException("Trainee not found: " + username);
    }

    @CircuitBreaker(name = "workloadService", fallbackMethod = "getTrainerWorkingHoursFallback")
    public String getTrainerWorkingHours(String trainerUsername, GetWorkingHoursDTO getWorkingHoursDTO) {
        logger.debug("Attempting to retrieve working hours for trainer: {}, with params: {}", trainerUsername, getWorkingHoursDTO);

        Trainer trainer = trainerRepository.findByUser_Username(trainerUsername).orElseThrow(() -> {
            logger.warn("Trainer not found with username: {}", trainerUsername);
            return new EntityNotFoundException("Trainer not found");
        });



        String str = messageProducer.sendAndReceive(trainerUsername,getWorkingHoursDTO, ActionType.HOURS);
        logger.info("Successfully retrieved working hours for trainer: {}", trainerUsername);
        return str;
    }

    public String getTrainerWorkingHoursFallback(String trainerUsername, GetWorkingHoursDTO getWorkingHoursDTO, Throwable t) {
        logger.error("Circuit breaker fallback for getTrainerWorkingHours, trainer: {} - {}", trainerUsername, t.getMessage());
        throw new RuntimeException("Workload service unavailable: cannot retrieve working hours for " + trainerUsername);
    }
}