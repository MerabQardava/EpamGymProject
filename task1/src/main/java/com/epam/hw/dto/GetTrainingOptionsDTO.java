package com.epam.hw.dto;


import java.time.LocalDate;

public record GetTrainingOptionsDTO(
        LocalDate startDate,
        LocalDate endDate,
        String trainerUsername,
        String trainingTypeName
) {
}
