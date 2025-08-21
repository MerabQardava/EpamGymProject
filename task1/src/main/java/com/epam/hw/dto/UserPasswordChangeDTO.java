package com.epam.hw.dto;

import jakarta.validation.constraints.NotBlank;

public record UserPasswordChangeDTO(
        @NotBlank String oldPassword,
        @NotBlank String newPassword
) { }
