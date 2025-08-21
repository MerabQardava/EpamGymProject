package com.epam.hw.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.List;
import java.util.Arrays;

import static org.mockito.Mockito.when;

import com.epam.hw.entity.Trainee;
import com.epam.hw.entity.Trainer;
import com.epam.hw.entity.Training;
import com.epam.hw.entity.TrainingType;
import com.epam.hw.entity.User;
import com.epam.hw.repository.TraineeRepository;
import com.epam.hw.repository.TrainerRepository;
import com.epam.hw.repository.TrainingRepository;
import com.epam.hw.repository.TrainingTypeRepository;
import com.epam.hw.storage.Auth;
import org.mockito.ArgumentCaptor;

import jakarta.persistence.EntityNotFoundException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TrainingServiceTest {

    @Mock TrainingRepository trainingRepo;
    @Mock TraineeRepository traineeRepo;
    @Mock TrainerRepository trainerRepo;
    @Mock TrainingTypeRepository trainingTypeRepo;
    @Mock Auth auth;

    @InjectMocks
    TrainingService trainingService;

    @Test
    void addTraining_success() {
        // Arrange
        String traineeUsername = "john.doe";
        String trainerUsername = "jane.smith";
        String trainingTypeName = "Java";
        String trainingName = "Java Basics";
        LocalDate date = LocalDate.of(2025, 6, 15);
        Integer duration = 60;

        User mockUser = new User();
        when(auth.getLoggedInUser()).thenReturn(mockUser);

        Trainee trainee = new Trainee();
        Trainer trainer = new Trainer();
        TrainingType type = new TrainingType(trainingTypeName);

        when(traineeRepo.findByUser_Username(traineeUsername)).thenReturn(Optional.of(trainee));
        when(trainerRepo.findByUser_Username(trainerUsername)).thenReturn(Optional.of(trainer));
        when(trainingTypeRepo.findByTrainingTypeName(trainingTypeName)).thenReturn(Optional.of(type));

        when(trainingRepo.save(any(Training.class))).thenAnswer(invocation -> {
            Training training = invocation.getArgument(0);
            training.setId(10);
            return training;
        });


        Training result = trainingService.addTraining(
                traineeUsername, trainerUsername, trainingName,
                trainingTypeName, date, duration);

        ArgumentCaptor<Training> captor = ArgumentCaptor.forClass(Training.class);
        verify(trainingRepo).save(captor.capture());
        Training savedTraining = captor.getValue();

        assertEquals(trainee, savedTraining.getTrainee());
        assertEquals(trainer, savedTraining.getTrainer());
        assertEquals(type, savedTraining.getTrainingType());
        assertEquals(trainingName, savedTraining.getTrainingName());
        assertEquals(date, savedTraining.getDate());
        assertEquals(duration, savedTraining.getDuration());
        assertEquals(10, result.getId());
    }

    @Test
    void addTraining_throwsWhenNotLoggedIn() {
        when(auth.getLoggedInUser()).thenReturn(null);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> trainingService.addTraining(
                        "john.doe", "jane.smith", "Java Basics",
                        "Java", LocalDate.now(), 60));

        assertEquals("No user is logged in.", ex.getMessage());
        verify(trainingRepo, never()).save(any());
        verify(traineeRepo, never()).findByUser_Username(any());
    }

    @Test
    void addTraining_throwsWhenTraineeNotFound() {
        User mockUser = new User();
        when(auth.getLoggedInUser()).thenReturn(mockUser);
        when(traineeRepo.findByUser_Username("nonexistent")).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> trainingService.addTraining(
                        "nonexistent", "jane.smith", "Java Basics",
                        "Java", LocalDate.now(), 60));

        assertEquals("Trainee not found", ex.getMessage());
        verify(trainingRepo, never()).save(any());
    }

    @Test
    void addTraining_throwsWhenTrainerNotFound() {
        User mockUser = new User();
        when(auth.getLoggedInUser()).thenReturn(mockUser);

        Trainee trainee = new Trainee();
        when(traineeRepo.findByUser_Username("john.doe")).thenReturn(Optional.of(trainee));
        when(trainerRepo.findByUser_Username("nonexistent")).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> trainingService.addTraining(
                        "john.doe", "nonexistent", "Java Basics",
                        "Java", LocalDate.now(), 60));

        assertEquals("Trainer not found", ex.getMessage());
        verify(trainingRepo, never()).save(any());
    }

    @Test
    void addTraining_throwsWhenTrainingTypeNotFound() {
        User mockUser = new User();
        when(auth.getLoggedInUser()).thenReturn(mockUser);

        Trainee trainee = new Trainee();
        Trainer trainer = new Trainer();
        when(traineeRepo.findByUser_Username("john.doe")).thenReturn(Optional.of(trainee));
        when(trainerRepo.findByUser_Username("jane.smith")).thenReturn(Optional.of(trainer));
        when(trainingTypeRepo.findByTrainingTypeName("NonexistentType")).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> trainingService.addTraining(
                        "john.doe", "jane.smith", "Java Basics",
                        "NonexistentType", LocalDate.now(), 60));

        assertEquals("TrainingType not found", ex.getMessage());
        verify(trainingRepo, never()).save(any());
    }

    @Test
    void getTrainingTypes_success() {
        User mockUser = new User();
        when(auth.getLoggedInUser()).thenReturn(mockUser);

        List<TrainingType> expectedTypes = Arrays.asList(
                new TrainingType("Java"),
                new TrainingType("Python"),
                new TrainingType("Spring")
        );
        when(trainingTypeRepo.findAll()).thenReturn(expectedTypes);

        List<TrainingType> result = trainingService.getTrainingTypes();

        assertEquals(expectedTypes, result);
        assertEquals(3, result.size());
        verify(trainingTypeRepo).findAll();
    }

    @Test
    void getTrainingTypes_throwsWhenNotLoggedIn() {
        when(auth.getLoggedInUser()).thenReturn(null);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> trainingService.getTrainingTypes());

        assertEquals("No user is logged in.", ex.getMessage());
        verify(trainingTypeRepo, never()).findAll();
    }
}