package com.epam.hw.feign;

import com.epam.hw.dto.GetWorkingHoursDTO;
import com.epam.hw.dto.UpdateWorkingHoursDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class WorkloadFallback implements WorkloadInterface {

    private static final Logger logger = LoggerFactory.getLogger(WorkloadFallback.class);

    @Override
    public ResponseEntity<String> updateWorkingHours(String username, String action, UpdateWorkingHoursDTO updateWorkingHoursDTO) {
        logger.warn("Fallback for updateWorkingHours, username: {}, action: {}", username, action);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Service unavailable: cannot " + action.toLowerCase() + " hours");
    }

    @Override
    public ResponseEntity<String> getWorkingHours(String username, GetWorkingHoursDTO getWorkingHoursDTO) {
        logger.warn("Fallback for getWorkingHours, username: {}", username);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Service unavailable: cannot retrieve hours");
    }
}