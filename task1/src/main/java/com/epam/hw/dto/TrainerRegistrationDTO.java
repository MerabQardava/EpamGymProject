package com.epam.hw.dto;

import jakarta.validation.constraints.NotBlank;

public record TrainerRegistrationDTO(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank String password,
        @NotBlank String specialization
) {}
