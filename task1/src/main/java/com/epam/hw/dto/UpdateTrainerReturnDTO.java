package com.epam.hw.dto;

import com.epam.hw.entity.TrainingType;

import java.util.Set;


public record UpdateTrainerReturnDTO(
        String username,
        String firstName,
        String lastName,
        TrainingType specialization,
        Boolean isActive,
        Set<TraineesListDTO> traineesList
) {
}
