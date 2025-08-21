package com.epam.hw.dto;

import com.epam.hw.entity.TrainingType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public record UpdateTrainerDTO(
        @NotBlank String firstName,
        @NotBlank String lastName,
        String specialization,
        @NotNull Boolean isActive
        ) {
}
