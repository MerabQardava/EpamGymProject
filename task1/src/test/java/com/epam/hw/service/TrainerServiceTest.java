package com.epam.hw.service;

import com.epam.hw.dto.UpdateTrainerDTO;
import com.epam.hw.entity.Trainer;
import com.epam.hw.entity.TrainingType;
import com.epam.hw.entity.User;
import com.epam.hw.repository.*;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TrainerServiceTest {

    @Mock
    TrainerRepository trainerRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    TrainingRepository trainingRepository;

    @Mock
    Auth auth;

    @Mock
    TrainingTypeRepository trainingTypeRepository;

    @InjectMocks
    TrainerService trainerService;

    User loggedUser;
    Trainer loggedTrainer;

    @BeforeEach
    void setUp() {
        loggedUser = new User("Logged", "User");
        loggedTrainer = new Trainer();
        loggedUser.setTrainer(loggedTrainer);

        Mockito.lenient().when(auth.getLoggedInUser()).thenReturn(loggedUser);
    }


    @Test
    void getTrainerByUsernameTest() {
        User dbUser = new User("A", "B");
        Trainer dbTrainer = new Trainer();
        dbUser.setTrainer(dbTrainer);

        when(trainerRepository.findByUser_Username("A.B"))
                .thenReturn(Optional.of(dbTrainer));

        Trainer result = trainerService.getTrainerByUsername("A.B");

        assertSame(dbTrainer, result);
    }


    @Test
    void toggleTrainerStatusTest() {
        String username = "A.B";

        User user = new User("A", "B");
        user.setActive(true);
        Trainer trainer = new Trainer();
        user.setTrainer(trainer);
        trainer.setUser(user);

        when(trainerRepository.findByUser_Username(username)).thenReturn(Optional.of(trainer));

        boolean result = trainerService.toggleTrainerStatus(username);

        assertFalse(result);
        assertFalse(user.isActive());

        verify(userRepository).save(user);
    }

    @Test
    void updateTrainerProfileTest() {
        String username = "John.Doe";
        String newFirstName = "Johnny";
        String newLastName = "Smith";

        User existingUser = new User("John", "Doe");
        existingUser.setActive(true);
        Trainer existingTrainer = new Trainer();
        existingUser.setTrainer(existingTrainer);
        existingTrainer.setUser(existingUser);

        UpdateTrainerDTO updateDTO = new UpdateTrainerDTO(newFirstName, newLastName, "Java",true);

        when(trainerRepository.findByUser_Username(username)).thenReturn(Optional.of(existingTrainer));

        Trainer result = trainerService.updateTrainerProfile(username, updateDTO);

        assertEquals(newFirstName, result.getUser().getFirstName());
        assertEquals(newLastName, result.getUser().getLastName());
        assertTrue(result.getUser().isActive());

        verify(trainerRepository).save(existingTrainer);
    }
}