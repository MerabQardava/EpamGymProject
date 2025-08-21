package com.epam.hw.dto;


import java.time.LocalDate;

public record GetTrainerTrainingOptionsDTO(
        LocalDate startDate,
        LocalDate endDate,
        String traineeUsername
) {
}
