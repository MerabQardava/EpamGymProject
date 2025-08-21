package com.epam.hw.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateTraineeDTO(
        @NotBlank String firstName,
        @NotBlank String lastName,
        String dob,
        String address,
        @NotNull Boolean isActive
) {
}

