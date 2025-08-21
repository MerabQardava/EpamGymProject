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

//    @Test
//    void createTraineeTest() {
//
//        String first = "John", last = "Doe";
//
//
//        when(userRepo.findByUsername("John.Doe"))
//                .thenReturn(Optional.of(new User()));
//
//        when(userRepo.findByUsername("John.Doe1"))
//                .thenReturn(Optional.empty());
//
//        ArgumentCaptor<Trainee> captor = ArgumentCaptor.forClass(Trainee.class);
//
//
//        traineeService.createTrainee(first, last,
//                LocalDate.of(1995, 1, 1),
//                "Tbilisi");
//
//
//        verify(traineeRepo).save(captor.capture());
//        Trainee saved = captor.getValue();
//
//        assertEquals("John.Doe1", saved.getUser().getUsername());
//        assertEquals(first,  saved.getUser().getFirstName());
//        assertEquals(last,   saved.getUser().getLastName());
//        assertEquals("Tbilisi", saved.getAddress());
//    }

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

//    @Test
//    void testLoginSuccess() {
//        String username = "John.Doe";
//        String password = "password123";
//        Trainee trainee = new Trainee();
//
//
//        when(traineeRepo.findByUser_Username(username)).thenReturn(Optional.of(trainee));
//
//        when(auth.logIn(username, password)).thenReturn(true);
//
//        LoginResults result = traineeService.logIn(username, password);
//
//
//        assertEquals(LoginResults.SUCCESS, result);
//
//
//        verify(auth).logIn(username, password);
//    }
//
//    @Test
//    void testLogout() {
//        when(auth.logOut()).thenReturn(true);
//
//        boolean result = traineeService.logOut();
//
//        assertTrue(result);
//        verify(auth).logOut();
//    }
//
//    @Test
//    void changePasswordTest() {
//        User dbUser = new User("A", "B");
//        dbUser.setPassword("oldPassword");
//        dbUser.setTrainee(new Trainee());
//
//        when(auth.getLoggedInUser()).thenReturn(dbUser);
//
//        when(userRepo.findByUsername("A.B")).thenReturn(Optional.of(dbUser));
//
//        String newPassword = "newPassword123";
//
//        boolean result = traineeService.changePassword("A.B", newPassword);
//
//        assertTrue(result);
//        assertEquals(newPassword, dbUser.getPassword());
//
//        verify(userRepo).save(dbUser);
//    }

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




//    @Test
//    void profileUpdated_withUniqueUsername() {
//        User updatedUser = new User("A", "B");
//        updatedUser.setActive(false);
//
//        Trainee dto = new Trainee();
//        dto.setUser(updatedUser);
//        dto.setAddress("Tbilisi");
//        dto.setDateOfBirth(LocalDate.of(1999, 12, 31));
//
//        when(userRepo.findByUsername("A.B")).thenReturn(Optional.empty());
//
//        boolean result = traineeService.updateTraineeProfile(dto);
//        assertTrue(result);
//
//        ArgumentCaptor<User> userCap = ArgumentCaptor.forClass(User.class);
//        verify(userRepo).save(userCap.capture());
//        User persistedUser = userCap.getValue();
//
//        assertEquals("A.B", persistedUser.getUsername());
//        assertEquals("A",  persistedUser.getFirstName());
//        assertEquals("B", persistedUser.getLastName());
//        assertFalse(persistedUser.isActive());
//
//        ArgumentCaptor<Trainee> trCap = ArgumentCaptor.forClass(Trainee.class);
//        verify(traineeRepo).save(trCap.capture());
//        Trainee persistedTrainee = trCap.getValue();
//
//        assertEquals("Tbilisi", persistedTrainee.getAddress());
//        assertEquals(LocalDate.of(1999,12,31), persistedTrainee.getDateOfBirth());
//    }

//    @Test
//    void profileUpdated_usernameTaken() {
//        when(userRepo.findByUsername("A.B")).thenReturn(
//                Optional.of(new User()),
//                Optional.empty());
//
//        User dtoUser = new User("A","B");
//
//        Trainee dto = new Trainee();
//        dto.setUser(dtoUser);
//
//        traineeService.updateTraineeProfile(dto);
//
//        verify(userRepo).save(argThat(u -> u.getUsername().equals("A.B1")));
//    }


//    @Test
//    void updateTraineeTrainers_updatesTrainerSetCorrectly() {
//        Integer traineeId = 123;
//
//        Trainee trainee = new Trainee();
//        Trainer trainer1 = new Trainer(); trainer1.setId(1);
//        Trainer trainer2 = new Trainer(); trainer2.setId(2);
//        Trainer trainer3 = new Trainer(); trainer3.setId(3);
//
//        trainee.addTrainer(trainer1);
//        trainee.addTrainer(trainer2);
//
//        Set<Integer> newTrainerIds = Set.of(2, 3);
//
//        when(traineeRepo.findById(traineeId)).thenReturn(Optional.of(trainee));
//        when(trainerRepo.findAllById(newTrainerIds)).thenReturn(List.of(trainer2, trainer3));
//
//        boolean result = traineeService.updateTraineeTrainers(traineeId, newTrainerIds);
//
//        assertTrue(result);
//        assertEquals(Set.of(trainer2, trainer3), trainee.getTrainers());
//    }



}


