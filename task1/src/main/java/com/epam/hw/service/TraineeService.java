package com.epam.hw.service;

import com.epam.hw.dto.UpdateTraineeDTO;
import com.epam.hw.entity.Trainee;
import com.epam.hw.entity.Trainer;
import com.epam.hw.entity.Training;
import com.epam.hw.entity.User;
import com.epam.hw.repository.TraineeRepository;
import com.epam.hw.repository.TrainerRepository;
import com.epam.hw.repository.TrainingRepository;
import com.epam.hw.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@Transactional
public class TraineeService {

    private static final Logger logger = LoggerFactory.getLogger(TraineeService.class);

    private final TraineeRepository traineeRepository;
    private final UserRepository userRepository;
    private final TrainerRepository trainerRepository;
    private final TrainingRepository trainingRepository;


    @Autowired
    public TraineeService(TraineeRepository traineeRepository,
                          UserRepository userRepository,
                          TrainerRepository trainerRepository,
                          TrainingRepository trainingRepository) {
        this.traineeRepository = traineeRepository;
        this.userRepository = userRepository;
        this.trainerRepository = trainerRepository;
        this.trainingRepository = trainingRepository;
    }


    public Trainee getTraineeByUsername(String username) {
        logger.debug("Fetching trainee by username: {}", username);

        return traineeRepository.findByUser_Username(username).orElseThrow(
                () -> new EntityNotFoundException("Trainee not found for username: " + username));
    }



    public Trainee updateTraineeProfile(String username,UpdateTraineeDTO updatedTraineeData) {

        logger.info("Updating profile for logged-in trainee.");

        Trainee trainee = getTraineeByUsername(username);
        User currentUser = trainee.getUser();

        currentUser.setFirstName(updatedTraineeData.firstName());
        currentUser.setLastName(updatedTraineeData.lastName());

        if(updatedTraineeData.isActive()){
            toggleTraineeStatus(username);
        }

        if (updatedTraineeData.dob() != null) {
            trainee.setDateOfBirth(LocalDate.parse(updatedTraineeData.dob()));
        }

        if (updatedTraineeData.address() != null) {
            trainee.setAddress(updatedTraineeData.address());
        }


        traineeRepository.save(trainee);

        return trainee;


    }

    public boolean toggleTraineeStatus(String username) {
        Trainee trainee = getTraineeByUsername(username);

        User user = trainee.getUser();
        boolean newStatus = !user.isActive();
        user.setActive(newStatus);

        userRepository.save(user);
        logger.info("Trainee status toggled to: {}", newStatus ? "ACTIVE" : "INACTIVE");
        return newStatus;
    }

    public boolean deleteByUsername(String username) {
        logger.warn("Deleting trainee with username: {}", username);

        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            Trainee trainee = user.getTrainee();
            System.out.println("mech");

            if (trainee != null) {
                traineeRepository.delete(trainee);
                logger.info("Trainee and user deleted: {}", username);
                return true;
            } else {
                logger.warn("User {} is not a trainee, delete aborted.", username);
            }
        } else {
            logger.warn("No user found with username: {}", username);
        }

        return false;
    }

    public Set<Trainer> updateTraineeTrainers(String username, Set<String> trainersList) {
        logger.info("Updating trainers for traineeId: {}", username);

        Trainee trainee = getTraineeByUsername(username);

        Set<Trainer> newTrainers = new HashSet<>(trainerRepository.findAllByUser_UsernameIn(trainersList));

        trainee.getTrainers().removeIf(t -> !newTrainers.contains(t));
        newTrainers.forEach(trainee::addTrainer);

        logger.debug("Trainer list updated for traineeId: {}", username);
        return trainee.getTrainers();

    }

    public void addTrainerToTrainee(String traineeUsername, String trainerUsername) {
        logger.info("Assigning trainer {} to trainee {}", trainerUsername, traineeUsername);

        Trainee trainee = traineeRepository.findByUser_Username(traineeUsername)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found: " + traineeUsername));
        Trainer trainer = trainerRepository.findByUser_Username(trainerUsername)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found: " + trainerUsername));

        trainee.addTrainer(trainer);
        logger.debug("Trainer {} assigned to trainee {}", trainerUsername, traineeUsername);
    }

    public void removeTrainerFromTrainee(String traineeUsername, String trainerUsername) {
        logger.info("Removing trainer {} from trainee {}", trainerUsername, traineeUsername);

        Trainee trainee = traineeRepository.findByUser_Username(traineeUsername)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found: " + traineeUsername));
        Trainer trainer = trainerRepository.findByUser_Username(trainerUsername)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found: " + trainerUsername));

        trainee.removeTrainer(trainer);
        logger.debug("Trainer {} removed from trainee {}", trainerUsername, traineeUsername);
    }

    public List<Training> getTraineeTrainings(
            String username,
            LocalDate from,
            LocalDate to,
            String trainerName,
            String trainingTypeName) {


        logger.debug("Fetching trainings for trainee: {} from {} to {}, trainerName={}, trainingType={}",
                username, from, to, trainerName, trainingTypeName);

        return trainingRepository.findTrainingsByTraineeCriteria(
                username, from, to, trainerName, trainingTypeName);
    }





}
