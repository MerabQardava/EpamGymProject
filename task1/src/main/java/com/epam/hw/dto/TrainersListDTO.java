package com.epam.hw.dto;

import com.epam.hw.entity.TrainingType;

public record TrainersListDTO(
        String username,
        String firstName,
        String lastName,
        TrainingType specialization
) {
}
