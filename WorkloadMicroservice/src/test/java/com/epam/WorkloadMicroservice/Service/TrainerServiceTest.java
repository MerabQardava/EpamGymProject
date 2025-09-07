package com.epam.WorkloadMicroservice.Service;

import com.epam.WorkloadMicroservice.dto.UpdateWorkingHoursDTO;
import com.epam.WorkloadMicroservice.entity.Trainer;
import com.epam.WorkloadMicroservice.entity.WorkMonth;
import com.epam.WorkloadMicroservice.entity.WorkYear;
import com.epam.WorkloadMicroservice.repository.TrainerRepository;
import com.epam.WorkloadMicroservice.service.TrainerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TrainerServiceTest {

    @Mock
    private TrainerRepository trainerRepository;

    @InjectMocks
    private TrainerService trainerService;

    private UpdateWorkingHoursDTO sessionData;
    private Trainer trainer;
    private WorkYear workYear;
    private WorkMonth workMonth;

    @BeforeEach
    void setUp() {
        sessionData = new UpdateWorkingHoursDTO(
                "John", "Doe",false, LocalDate.of(2025, 10, 1), 5
        );
        trainer = new Trainer("john.doe", "John", "Doe");
        workYear = new WorkYear(2025);
        workMonth = new WorkMonth(10, 0);
        trainer.setWorkYears(new HashSet<>());
        trainer.getWorkYears().add(workYear);
        workYear.setWorkMonths(new HashSet<>());
        workYear.getWorkMonths().add(workMonth);
    }

    @Test
    void testAddTraining_newTrainer() {
        when(trainerRepository.findById("john.doe")).thenReturn(Optional.empty());
        when(trainerRepository.save(any(Trainer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        trainerService.addTraining("john.doe", sessionData);

        verify(trainerRepository, times(1)).findById("john.doe");
        verify(trainerRepository, times(1)).save(any(Trainer.class));

        ArgumentCaptor<Trainer> trainerCaptor = ArgumentCaptor.forClass(Trainer.class);
        verify(trainerRepository).save(trainerCaptor.capture());
        Trainer savedTrainer = trainerCaptor.getValue();

        assertNotNull(savedTrainer, "Saved trainer should not be null");
        WorkYear savedYear = savedTrainer.getWorkYears().stream()
                .filter(year -> year.getYearNumber() == 2025)
                .findFirst()
                .orElse(null);
        assertNotNull(savedYear, "WorkYear should be created");
        WorkMonth savedMonth = savedYear.getWorkMonths().stream()
                .filter(month -> month.getMonthNumber() == 10)
                .findFirst()
                .orElse(null);
        assertNotNull(savedMonth, "WorkMonth should be created");
        assertEquals(5, savedMonth.getTotalHours(), "Total hours should be updated to 5");
    }

    @Test
    void testAddTraining_existingTrainer() {
        when(trainerRepository.findById("john.doe")).thenReturn(Optional.of(trainer));
        when(trainerRepository.save(any(Trainer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        trainerService.addTraining("john.doe", sessionData);

        verify(trainerRepository, times(1)).findById("john.doe");
        verify(trainerRepository, times(1)).save(trainer);
        assertEquals(5, workMonth.getTotalHours(), "Total hours should be updated to 5");
    }

    @Test
    void testAddTraining_negativeDuration_throwsException() {
        UpdateWorkingHoursDTO invalidSessionData = new UpdateWorkingHoursDTO(
                "John", "Doe", true,LocalDate.of(2025, 10, 1), -5
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> trainerService.addTraining("john.doe", invalidSessionData),
                "Expected IllegalArgumentException for negative duration"
        );
        assertEquals("Training duration cannot be negative", exception.getMessage());
        verify(trainerRepository, never()).save(any(Trainer.class));
    }
}