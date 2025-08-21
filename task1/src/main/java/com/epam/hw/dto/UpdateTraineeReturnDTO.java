package com.epam.hw.dto;

import java.util.Set;

public record UpdateTraineeReturnDTO(
        String username,
        String firstName,
        String lastName,
        String dob,
        String address,
        Boolean isActive,
        Set<TrainersListDTO> trainers
) {
}
