package com.epam.hw.service;

import com.epam.hw.entity.Trainee;
import com.epam.hw.entity.Trainer;
import com.epam.hw.entity.User;
import com.epam.hw.repository.TraineeRepository;
import com.epam.hw.repository.TrainerRepository;
import com.epam.hw.repository.TrainingRepository;
import com.epam.hw.repository.UserRepository;
import com.epam.hw.storage.Auth;
import com.epam.hw.storage.LoginResults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TraineeServiceTest {

    @Mock
    TraineeRepository traineeRepo;
    @Mock
    UserRepository userRepo;
    @Mock
    TrainerRepository trainerRepo;
    @Mock
    TrainingRepository trainingRepo;
    @Mock
    Auth auth;


    @InjectMocks
    TraineeService traineeService;


    User loggedUser;
    Trainee loggedTrainee;

    @BeforeEach
    void setUp() {
        loggedUser = new User("Logged", "User");
        loggedTrainee = new Trainee();
        loggedUser.setTrainee(loggedTrainee);


        Mockito.lenient().when(auth.getLoggedInUser()).thenReturn(loggedUser);
    }


    @Test

    void getTraineeByUsernameTest() {
        User dbUser = new User("A","B");
        Trainee dbTrainee = new Trainee();
        dbUser.setTrainee(dbTrainee);

        when(traineeRepo.findByUser_Username("A.B"))
                .thenReturn(Optional.of(dbTrainee));

        Trainee result = traineeService.getTraineeByUsername("A.B");

        assertSame(dbTrainee, result);
    }


    @Test
    void toggleTraineeStatusTest(){
        String username = "A.B";

        User user = new User("A", "B");
        user.setActive(true);
        Trainee trainee = new Trainee();
        user.setTrainee(trainee);
        trainee.setUser(user);

        when(traineeRepo.findByUser_Username(username)).thenReturn(Optional.of(trainee));

        boolean result = traineeService.toggleTraineeStatus(username);

        assertFalse(result);
        assertFalse(user.isActive());

        verify(userRepo).save(user);
    }

    @Test
    void deleteByUsernameTest(){
        String username = "A.B";

        User userFound = new User("A", "B");
        Trainee traineeFound = new Trainee();
        userFound.setTrainee(traineeFound);

        when(userRepo.findByUsername(username)).thenReturn(Optional.of(userFound));

        boolean result = traineeService.deleteByUsername(username);

        assertTrue(result);
        verify(traineeRepo).delete(traineeFound);
    }

    @Test
    void addTrainerToTrainee() {
        String traineeUsername = "A.B";
        String trainerUsername = "C.D";

        Trainee trainee = new Trainee();
        Trainer trainer = new Trainer();

        when(traineeRepo.findByUser_Username(traineeUsername))
                .thenReturn(Optional.of(trainee));
        when(trainerRepo.findByUser_Username(trainerUsername))
                .thenReturn(Optional.of(trainer));

        traineeService.addTrainerToTrainee(traineeUsername, trainerUsername);

        assertTrue(trainee.getTrainers().contains(trainer),
                "Trainer should be linked to trainee");
    }



    @Test
    void removeTrainerToTrainee() {
        String traineeUsername = "A.B";
        String trainerUsername = "C.D";

        Trainee trainee = new Trainee();
        Trainer trainer = new Trainer();

        when(traineeRepo.findByUser_Username(traineeUsername))
                .thenReturn(Optional.of(trainee));
        when(trainerRepo.findByUser_Username(trainerUsername))
                .thenReturn(Optional.of(trainer));

        traineeService.addTrainerToTrainee(traineeUsername, trainerUsername);

        assertTrue(trainee.getTrainers().contains(trainer),
                "Trainer should be linked to trainee");

        traineeService.removeTrainerFromTrainee(traineeUsername,trainerUsername);
        assertTrue(trainee.getTrainers().isEmpty());
    }



}


