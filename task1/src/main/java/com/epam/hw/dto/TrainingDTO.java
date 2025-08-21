package com.epam.hw.dto;

import com.epam.hw.entity.TrainingType;

import java.time.LocalDate;

public record TrainingDTO(
        String name,
        LocalDate date,
        TrainingType trainingType,
        Integer duration,
        String trainerName
) {
}
