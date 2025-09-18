package com.epam.hw.service;

import com.epam.hw.dto.ActionType;
import com.epam.hw.dto.UpdateWorkingHoursDTO;
import com.epam.hw.entity.Trainee;
import com.epam.hw.entity.Trainer;
import com.epam.hw.entity.Training;
import com.epam.hw.entity.TrainingType;
import com.epam.hw.entity.User;
import com.epam.hw.messaging.MessageProducer;
import com.epam.hw.repository.TraineeRepository;
import com.epam.hw.repository.TrainerRepository;
import com.epam.hw.repository.TrainingRepository;
import com.epam.hw.repository.TrainingTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

    @Mock
    private TrainingRepository trainingRepo;

    @Mock
    private TraineeRepository traineeRepo;

    @Mock
    private TrainerRepository trainerRepo;

    @Mock
    private TrainingTypeRepository trainingTypeRepo;

    @Mock
    private MessageProducer messageProducer;

    @InjectMocks
    private TrainingService trainingService;

    private Trainee trainee;
    private Trainer trainer;
    private TrainingType trainingType;
    private Training training;
    private User traineeUser;
    private User trainerUser;

    @BeforeEach
    void setUp() {
        traineeUser = new User();
        traineeUser.setUsername("trainee.user");
        traineeUser.setFirstName("Trainee");
        traineeUser.setLastName("User");

        trainerUser = new User();
        trainerUser.setUsername("trainer.user");
        trainerUser.setFirstName("Trainer");
        trainerUser.setLastName("User");

        trainee = new Trainee();
        trainee.setUser(traineeUser);

        trainer = new Trainer();
        trainer.setUser(trainerUser);

        trainingType = new TrainingType("Java");

        training = new Training(trainee, trainer, "Java Training", trainingType, LocalDate.of(2025, 1, 1), 60);
        training.setId(1);
    }

    @Test
    void testAddTraining_Success() {
        // Arrange
        when(traineeRepo.findByUser_Username("trainee.user")).thenReturn(Optional.of(trainee));
        when(trainerRepo.findByUser_Username("trainer.user")).thenReturn(Optional.of(trainer));
        when(trainingTypeRepo.findByTrainingTypeName("Java")).thenReturn(Optional.of(trainingType));
        when(trainingRepo.save(any(Training.class))).thenReturn(training);

        // Act
        Training result = trainingService.addTraining(
                "trainee.user",
                "trainer.user",
                "Java Training",
                "Java",
                LocalDate.of(2025, 1, 1),
                60
        );

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Java Training", result.getTrainingName());
        assertEquals(trainingType, result.getTrainingType());
        assertEquals(trainee, result.getTrainee());
        assertEquals(trainer, result.getTrainer());
        assertEquals(LocalDate.of(2025, 1, 1), result.getDate());
        assertEquals(60, result.getDuration());

        verify(traineeRepo, times(1)).findByUser_Username("trainee.user");
        verify(trainerRepo, times(1)).findByUser_Username("trainer.user");
        verify(trainingTypeRepo, times(1)).findByTrainingTypeName("Java");
        verify(trainingRepo, times(1)).save(any(Training.class));

        ArgumentCaptor<UpdateWorkingHoursDTO> captor = ArgumentCaptor.forClass(UpdateWorkingHoursDTO.class);
        verify(messageProducer, times(1)).sendMessage(eq("trainer.user"), captor.capture(), eq(ActionType.ADD));
        UpdateWorkingHoursDTO sentDto = captor.getValue();
        assertEquals("Trainer", sentDto.firstName());
        assertEquals("User", sentDto.lastName());
        assertTrue(sentDto.isActive());
        assertEquals(LocalDate.of(2025, 1, 1), sentDto.date());
        assertEquals(60, sentDto.trainingDuration());
    }

    @Test
    void testAddTraining_TraineeNotFound() {
        // Arrange
        when(traineeRepo.findByUser_Username("trainee.user")).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            trainingService.addTraining(
                    "trainee.user",
                    "trainer.user",
                    "Java Training",
                    "Java",
                    LocalDate.of(2025, 1, 1),
                    60
            );
        });
        assertEquals("Trainee not found", exception.getMessage());

        verify(traineeRepo, times(1)).findByUser_Username("trainee.user");
        verify(trainerRepo, never()).findByUser_Username(anyString());
        verify(trainingTypeRepo, never()).findByTrainingTypeName(anyString());
        verify(trainingRepo, never()).save(any());
        verify(messageProducer, never()).sendMessage(anyString(), any(), any());
    }

    @Test
    void testAddTraining_TrainerNotFound() {
        // Arrange
        when(traineeRepo.findByUser_Username("trainee.user")).thenReturn(Optional.of(trainee));
        when(trainerRepo.findByUser_Username("trainer.user")).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            trainingService.addTraining(
                    "trainee.user",
                    "trainer.user",
                    "Java Training",
                    "Java",
                    LocalDate.of(2025, 1, 1),
                    60
            );
        });
        assertEquals("Trainer not found", exception.getMessage());

        verify(traineeRepo, times(1)).findByUser_Username("trainee.user");
        verify(trainerRepo, times(1)).findByUser_Username("trainer.user");
        verify(trainingTypeRepo, never()).findByTrainingTypeName(anyString());
        verify(trainingRepo, never()).save(any());
        verify(messageProducer, never()).sendMessage(anyString(), any(), any());
    }

    @Test
    void testAddTraining_TrainingTypeNotFound() {
        // Arrange
        when(traineeRepo.findByUser_Username("trainee.user")).thenReturn(Optional.of(trainee));
        when(trainerRepo.findByUser_Username("trainer.user")).thenReturn(Optional.of(trainer));
        when(trainingTypeRepo.findByTrainingTypeName("Java")).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            trainingService.addTraining(
                    "trainee.user",
                    "trainer.user",
                    "Java Training",
                    "Java",
                    LocalDate.of(2025, 1, 1),
                    60
            );
        });
        assertEquals("TrainingType not found", exception.getMessage());

        verify(traineeRepo, times(1)).findByUser_Username("trainee.user");
        verify(trainerRepo, times(1)).findByUser_Username("trainer.user");
        verify(trainingTypeRepo, times(1)).findByTrainingTypeName("Java");
        verify(trainingRepo, never()).save(any());
        verify(messageProducer, never()).sendMessage(anyString(), any(), any());
    }

    @Test
    void testAddTraining_Fallback() {
        // Arrange
        when(traineeRepo.findByUser_Username("trainee.user")).thenThrow(new RuntimeException("Database unavailable"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            trainingService.addTraining(
                    "trainee.user",
                    "trainer.user",
                    "Java Training",
                    "Java",
                    LocalDate.of(2025, 1, 1),
                    60
            );
        });
        assertEquals("Database unavailable", exception.getMessage());

        verify(traineeRepo, times(1)).findByUser_Username("trainee.user");
        verify(trainerRepo, never()).findByUser_Username(anyString());
        verify(trainingTypeRepo, never()).findByTrainingTypeName(anyString());
        verify(trainingRepo, never()).save(any());
        verify(messageProducer, never()).sendMessage(anyString(), any(), any());
    }

    @Test
    void testDeleteTraining_Success() {
        // Arrange
        when(trainingRepo.findById(1)).thenReturn(Optional.of(training));
        doNothing().when(trainingRepo).delete(training);

        // Act
        trainingService.deleteTraining(1);

        // Assert
        verify(trainingRepo, times(1)).findById(1);
        verify(trainingRepo, times(1)).delete(training);

        ArgumentCaptor<UpdateWorkingHoursDTO> captor = ArgumentCaptor.forClass(UpdateWorkingHoursDTO.class);
        verify(messageProducer, times(1)).sendMessage(eq("trainer.user"), captor.capture(), eq(ActionType.REMOVE));
        UpdateWorkingHoursDTO sentDto = captor.getValue();
        assertEquals("Trainer", sentDto.firstName());
        assertEquals("User", sentDto.lastName());
        assertTrue(sentDto.isActive());
        assertEquals(LocalDate.of(2025, 1, 1), sentDto.date());
        assertEquals(60, sentDto.trainingDuration());
    }

    @Test
    void testDeleteTraining_NotFound() {
        // Arrange
        when(trainingRepo.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            trainingService.deleteTraining(999);
        });
        assertEquals("Training not found", exception.getMessage());

        verify(trainingRepo, times(1)).findById(999);
        verify(trainingRepo, never()).delete(any());
        verify(messageProducer, never()).sendMessage(anyString(), any(), any());
    }

    @Test
    void testDeleteTraining_Fallback() {
        // Arrange
        when(trainingRepo.findById(1)).thenThrow(new RuntimeException("Database unavailable"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            trainingService.deleteTraining(1);
        });
        assertEquals("Database unavailable", exception.getMessage());

        verify(trainingRepo, times(1)).findById(1);
        verify(trainingRepo, never()).delete(any());
        verify(messageProducer, never()).sendMessage(anyString(), any(), any());
    }

    @Test
    void testGetTrainingTypes_Success() {
        // Arrange
        List<TrainingType> trainingTypes = List.of(
                new TrainingType("Java"),
                new TrainingType("Spring")
        );
        when(trainingTypeRepo.findAll()).thenReturn(trainingTypes);

        // Act
        List<TrainingType> result = trainingService.getTrainingTypes();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Java", result.get(0).getTrainingTypeName());
        assertEquals("Spring", result.get(1).getTrainingTypeName());

        verify(trainingTypeRepo, times(1)).findAll();
    }

    @Test
    void testGetTrainingTypes_EmptyList() {
        // Arrange
        when(trainingTypeRepo.findAll()).thenReturn(List.of());

        // Act
        List<TrainingType> result = trainingService.getTrainingTypes();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(trainingTypeRepo, times(1)).findAll();
    }
}