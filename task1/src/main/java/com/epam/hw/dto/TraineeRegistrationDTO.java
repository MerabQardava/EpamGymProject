package com.epam.hw.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record TraineeRegistrationDTO(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank String password,
        String dob,
        String address
) {}