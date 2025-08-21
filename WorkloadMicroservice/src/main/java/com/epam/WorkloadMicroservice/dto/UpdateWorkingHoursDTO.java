package com.epam.WorkloadMicroservice.dto;

import java.time.LocalDate;

public record UpdateWorkingHoursDTO(
        String firstName,
        String lastName,
        boolean isActive,
        LocalDate date,
        int trainingDuration
){

}


