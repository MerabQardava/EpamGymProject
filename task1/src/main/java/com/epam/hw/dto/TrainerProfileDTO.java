package com.epam.hw.dto;

import com.epam.hw.entity.TrainingType;

import java.util.Set;

public record TrainerProfileDTO(
        String firstName,
        String lastName,
        TrainingType specialization,
        Boolean isActive,
        Set<TraineesListDTO> trainees
) {
}
