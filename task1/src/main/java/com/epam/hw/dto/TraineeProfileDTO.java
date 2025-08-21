package com.epam.hw.dto;

import com.epam.hw.entity.Trainer;

import java.util.List;
import java.util.Set;

public record TraineeProfileDTO(
        String firstName,
        String lastName,
        String dob,
        String address,
        Boolean isActive,
        Set<TrainersListDTO> trainers
) {
}

