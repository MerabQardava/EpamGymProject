package com.epam.hw.storage;

import com.epam.hw.entity.User;
import com.epam.hw.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthTest {

    private UserRepository userRepository;
    private Auth auth;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        auth = new Auth(userRepository);
    }

    @Test
    void successfulLoginAsTrainee() {
        User user = new User("John", "Doe");
        user.setUsername("john.doe");
        user.setPassword("pass123");
        user.setTrainee(new com.epam.hw.entity.Trainee());

        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(user));

        boolean result = auth.logIn("john.doe", "pass123");

        assertTrue(result);
        assertEquals(user, auth.getLoggedInUser());
    }

    @Test
    void successfulLoginAsTrainer() {
        User user = new User("Anna", "Smith");
        user.setUsername("anna.smith");
        user.setPassword("abc123");
        user.setTrainer(new com.epam.hw.entity.Trainer());

        when(userRepository.findByUsername("anna.smith")).thenReturn(Optional.of(user));

        boolean result = auth.logIn("anna.smith", "abc123");

        assertTrue(result);
        assertEquals(user, auth.getLoggedInUser());
    }

    @Test
    void loginFailsForIncorrectPassword() {
        User user = new User("User", "One");
        user.setUsername("user.one");
        user.setPassword("secret");
        user.setTrainee(new com.epam.hw.entity.Trainee());

        when(userRepository.findByUsername("user.one")).thenReturn(Optional.of(user));

        boolean result = auth.logIn("user.one", "wrong");

        assertFalse(result);
        assertNull(auth.getLoggedInUser());
    }

    @Test
    void loginFailsWhenUserNotFound() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        boolean result = auth.logIn("ghost", "any");

        assertFalse(result);
        assertNull(auth.getLoggedInUser());
    }

    @Test
    void loginFailsWhenUserIsNeitherTrainerNorTrainee() {
        User user = new User("Admin", "Only");
        user.setUsername("admin");
        user.setPassword("admin");

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        boolean result = auth.logIn("admin", "admin");

        assertFalse(result);
        assertNull(auth.getLoggedInUser());
    }

    @Test
    void logoutWorksCorrectly() {
        User user = new User("Logout", "Test");
        user.setUsername("logout.test");
        user.setPassword("out123");
        user.setTrainee(new com.epam.hw.entity.Trainee());

        when(userRepository.findByUsername("logout.test")).thenReturn(Optional.of(user));

        auth.logIn("logout.test", "out123");

        assertTrue(auth.logOut());
        assertNull(auth.getLoggedInUser());
    }

    @Test
    void logoutFailsIfNoUserLoggedIn() {
        assertFalse(auth.logOut());
    }
}