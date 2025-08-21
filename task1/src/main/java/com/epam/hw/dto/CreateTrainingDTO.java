package com.epam.hw.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateTrainingDTO(
        @NotBlank String trainingName,
        @NotBlank String trainingTypeName,
        @NotBlank String date,
        @NotNull Integer duration
) {
}
