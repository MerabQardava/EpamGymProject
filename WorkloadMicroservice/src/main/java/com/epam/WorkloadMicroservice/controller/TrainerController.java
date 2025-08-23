package com.epam.WorkloadMicroservice.controller;

import com.epam.WorkloadMicroservice.dto.ActionType;
import com.epam.WorkloadMicroservice.dto.GetWorkingHoursDTO;
import com.epam.WorkloadMicroservice.dto.UpdateWorkingHoursDTO;
import com.epam.WorkloadMicroservice.service.TrainerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Trainer Controller", description = "Endpoints for managing trainer working hours")
@RestController
@RequestMapping("/trainer")
@Slf4j
public class TrainerController {

    private final TrainerService trainerService;

    public TrainerController(TrainerService trainerService) {
        this.trainerService = trainerService;
    }

    @Operation(summary = "Add or remove training hours for a trainer")
    @PostMapping("/{username}/{action}")
    public ResponseEntity<String> updateWorkingHours(
            @PathVariable String username,
            @PathVariable String action,
            @RequestBody @Valid UpdateWorkingHoursDTO updateWorkingHoursDTO
    ){
        log.info("Request received: action={} for trainer={}, sessionData={}", action, username, updateWorkingHoursDTO);
        try {
            if(ActionType.valueOf(action) == ActionType.ADD){
                trainerService.addTraining(username, updateWorkingHoursDTO);
                log.info("Training hours added successfully for trainer={}", username);
                return ResponseEntity.ok("Training hours added successfully.");
            } else {
                trainerService.removeTraining(username, updateWorkingHoursDTO);
                log.info("Training hours removed successfully for trainer={}", username);
                return ResponseEntity.ok("Training hours removed successfully.");
            }
        } catch (Exception e){
            log.warn("Error updating working hours for trainer={} - {}", username, e.getMessage());
            return ResponseEntity.badRequest().body("Error updating working hours: " + e.getMessage());
        }
    }

    @Operation(summary = "Get total working hours for a trainer in a specific month/year")
    @PostMapping("/{username}")
    public ResponseEntity<String> getTrainer(
            @PathVariable String username,
            @RequestBody @Valid GetWorkingHoursDTO getWorkingHoursDTO
    ){
        log.info("Request received: get total hours for trainer={} in year={}, month={}",
                username, getWorkingHoursDTO.YearNumber(), getWorkingHoursDTO.MonthNumber());
        try {
            int hours = trainerService.getTotalHours(username, getWorkingHoursDTO.YearNumber(), getWorkingHoursDTO.MonthNumber());
            log.info("Total hours fetched for trainer={} : {}", username, hours);
            return ResponseEntity.ok("Total hours: " + hours);
        } catch (Exception e){
            log.warn("Error retrieving working hours for trainer={} - {}", username, e.getMessage());
            return ResponseEntity.badRequest().body("Error retrieving working hours: " + e.getMessage());
        }
    }
}
