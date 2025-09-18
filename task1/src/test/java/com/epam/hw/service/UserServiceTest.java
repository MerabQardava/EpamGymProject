package com.epam.hw.service;

import com.epam.hw.entity.Trainee;
import com.epam.hw.entity.Trainer;
import com.epam.hw.entity.TrainingType;
import com.epam.hw.entity.User;
import com.epam.hw.repository.TraineeRepository;
import com.epam.hw.repository.TrainerRepository;
import com.epam.hw.repository.TrainingTypeRepository;
import com.epam.hw.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private TrainingTypeRepository trainingTypeRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private AuthenticationManager authManager;

    @Mock
    private JWTService jwtService;

    @InjectMocks
    private UserService userService;

    private User user;
    private Trainee trainee;
    private Trainer trainer;
    private TrainingType trainingType;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12); // For password verification in tests

    @BeforeEach
    void setUp() {
        // Initialize test objects
        user = new User("John", "Doe");
        user.setUsername("John.Doe");

        trainee = new Trainee(LocalDate.of(1990, 1, 1), "123 Main St", user);
        user.setTrainee(trainee);

        trainingType = new TrainingType("Java");

        trainer = new Trainer(trainingType, user);
        user.setTrainer(trainer);

        // Inject mocked dependencies into UserService
        ReflectionTestUtils.setField(userService, "authManager", authManager);
        ReflectionTestUtils.setField(userService, "JwtService", jwtService); // Note the capitalization
    }

    @Test
    void testRegisterTrainee_Success() {
        // Arrange
        when(userRepository.findByUsername("John.Doe")).thenReturn(Optional.empty());
        when(traineeRepository.save(any(Trainee.class))).thenReturn(trainee);

        // Act
        Trainee result = userService.register("John", "Doe", LocalDate.of(1990, 1, 1), "123 Main St", "password");

        // Assert
        assertNotNull(result);
        assertEquals("John.Doe", result.getUser().getUsername());
        assertEquals("John", result.getUser().getFirstName());
        assertEquals("Doe", result.getUser().getLastName());
        assertTrue(encoder.matches("password", result.getUser().getPassword()));
        assertEquals(LocalDate.of(1990, 1, 1), result.getDateOfBirth());
        assertEquals("123 Main St", result.getAddress());

        verify(userRepository, times(1)).findByUsername("John.Doe");
        verify(traineeRepository, times(1)).save(any(Trainee.class));
    }

    @Test
    void testRegisterTrainee_UsernameConflict() {
        // Arrange
        when(userRepository.findByUsername("John.Doe")).thenReturn(Optional.of(user));
        when(userRepository.findByUsername("John.Doe1")).thenReturn(Optional.empty());
        when(traineeRepository.save(any(Trainee.class))).thenReturn(trainee);

        // Act
        Trainee result = userService.register("John", "Doe", LocalDate.of(1990, 1, 1), "123 Main St", "password");

        // Assert
        assertNotNull(result);
        assertEquals("John.Doe1", result.getUser().getUsername());
        assertEquals("John", result.getUser().getFirstName());
        assertEquals("Doe", result.getUser().getLastName());
        assertTrue(encoder.matches("password", result.getUser().getPassword()));

        verify(userRepository, times(1)).findByUsername("John.Doe");
        verify(userRepository, times(1)).findByUsername("John.Doe1");
        verify(traineeRepository, times(1)).save(any(Trainee.class));
    }

    @Test
    void testRegisterTrainer_Success() {
        // Arrange
        when(trainingTypeRepository.findByTrainingTypeName("Java")).thenReturn(Optional.of(trainingType));
        when(userRepository.findByUsername("John.Doe")).thenReturn(Optional.empty());
        when(trainerRepository.save(any(Trainer.class))).thenReturn(trainer);

        // Act
        Trainer result = userService.register("John", "Doe", "Java", "password");

        // Assert
        assertNotNull(result);
        assertEquals("John.Doe", result.getUser().getUsername());
        assertEquals("John", result.getUser().getFirstName());
        assertEquals("Doe", result.getUser().getLastName());
        assertTrue(encoder.matches("password", result.getUser().getPassword()));
        assertEquals(trainingType, result.getSpecializationId());

        verify(trainingTypeRepository, times(1)).findByTrainingTypeName("Java");
        verify(userRepository, times(1)).findByUsername("John.Doe");
        verify(trainerRepository, times(1)).save(any(Trainer.class));
    }

    @Test
    void testRegisterTrainer_TrainingTypeNotFound() {
        // Arrange
        when(trainingTypeRepository.findByTrainingTypeName("Java")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            userService.register("John", "Doe", "Java", "password");
        });
        assertEquals("Training type Java does not exist.", exception.getMessage());

        verify(trainingTypeRepository, times(1)).findByTrainingTypeName("Java");
        verify(userRepository, never()).findByUsername(anyString());
        verify(trainerRepository, never()).save(any());
    }

    @Test
    void testRegisterTrainer_UsernameConflict() {
        // Arrange
        when(trainingTypeRepository.findByTrainingTypeName("Java")).thenReturn(Optional.of(trainingType));
        when(userRepository.findByUsername("John.Doe")).thenReturn(Optional.of(user));
        when(userRepository.findByUsername("John.Doe1")).thenReturn(Optional.empty());
        when(trainerRepository.save(any(Trainer.class))).thenReturn(trainer);

        // Act
        Trainer result = userService.register("John", "Doe", "Java", "password");

        // Assert
        assertNotNull(result);
        assertEquals("John.Doe1", result.getUser().getUsername());
        assertEquals("John", result.getUser().getFirstName());
        assertEquals("Doe", result.getUser().getLastName());
        assertTrue(encoder.matches("password", result.getUser().getPassword()));

        verify(trainingTypeRepository, times(1)).findByTrainingTypeName("Java");
        verify(userRepository, times(1)).findByUsername("John.Doe");
        verify(userRepository, times(1)).findByUsername("John.Doe1");
        verify(trainerRepository, times(1)).save(any(Trainer.class));
    }

    @Test
    void testVerify_Success() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(jwtService.generateToken("John.Doe")).thenReturn("jwt.token");

        // Act
        String result = userService.verify("John.Doe", "password");

        // Assert
        assertEquals("jwt.token", result);

        ArgumentCaptor<UsernamePasswordAuthenticationToken> captor = ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authManager, times(1)).authenticate(captor.capture());
        assertEquals("John.Doe", captor.getValue().getPrincipal());
        assertEquals("password", captor.getValue().getCredentials());
        verify(jwtService, times(1)).generateToken("John.Doe");
    }

    @Test
    void testVerify_Failure() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        // Act
        String result = userService.verify("John.Doe", "wrongPassword");

        // Assert
        assertEquals("fail", result);

        ArgumentCaptor<UsernamePasswordAuthenticationToken> captor = ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authManager, times(1)).authenticate(captor.capture());
        assertEquals("John.Doe", captor.getValue().getPrincipal());
        assertEquals("wrongPassword", captor.getValue().getCredentials());
        verify(jwtService, never()).generateToken(anyString());
    }

    @Test
    void testChangeTraineePassword_Success() {
        // Arrange
        when(userRepository.findByUsername("John.Doe")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        boolean result = userService.changeTraineePassword("John.Doe", "newPassword");

        // Assert
        assertTrue(result);
        assertTrue(encoder.matches("newPassword", user.getPassword()));
        verify(userRepository, times(1)).findByUsername("John.Doe");
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testChangeTraineePassword_UserNotFound() {
        // Arrange
        when(userRepository.findByUsername("John.Doe")).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            userService.changeTraineePassword("John.Doe", "newPassword");
        });
        assertEquals("User not found: John.Doe", exception.getMessage());

        verify(userRepository, times(1)).findByUsername("John.Doe");
        verify(userRepository, never()).save(any());
    }

    @Test
    void testChangeTraineePassword_NotTrainee() {
        // Arrange
        User nonTraineeUser = new User("John", "Doe");
        nonTraineeUser.setUsername("John.Doe");
        nonTraineeUser.setTrainer(trainer); // User is a trainer, not a trainee
        when(userRepository.findByUsername("John.Doe")).thenReturn(Optional.of(nonTraineeUser));

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            userService.changeTraineePassword("John.Doe", "newPassword");
        });
        assertEquals("User is not a trainee: John.Doe", exception.getMessage());

        verify(userRepository, times(1)).findByUsername("John.Doe");
        verify(userRepository, never()).save(any());
    }

    @Test
    void testChangeTrainerPassword_Success() {
        // Arrange
        when(userRepository.findByUsername("John.Doe")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        boolean result = userService.changeTrainerPassword("John.Doe", "newPassword");

        // Assert
        assertTrue(result);
        assertTrue(encoder.matches("newPassword", user.getPassword()));
        verify(userRepository, times(1)).findByUsername("John.Doe");
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testChangeTrainerPassword_UserNotFound() {
        // Arrange
        when(userRepository.findByUsername("John.Doe")).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            userService.changeTrainerPassword("John.Doe", "newPassword");
        });
        assertEquals("User not found: John.Doe", exception.getMessage());

        verify(userRepository, times(1)).findByUsername("John.Doe");
        verify(userRepository, never()).save(any());
    }

    @Test
    void testChangeTrainerPassword_NotTrainer() {
        // Arrange
        User nonTrainerUser = new User("John", "Doe");
        nonTrainerUser.setUsername("John.Doe");
        nonTrainerUser.setTrainee(trainee); // User is a trainee, not a trainer
        when(userRepository.findByUsername("John.Doe")).thenReturn(Optional.of(nonTrainerUser));

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            userService.changeTrainerPassword("John.Doe", "newPassword");
        });
        assertEquals("User is not a trainer: John.Doe", exception.getMessage());

        verify(userRepository, times(1)).findByUsername("John.Doe");
        verify(userRepository, never()).save(any());
    }
}