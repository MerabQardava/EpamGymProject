package com.epam.WorkloadMicroservice.controller;


import com.epam.WorkloadMicroservice.dto.ActionType;
import com.epam.WorkloadMicroservice.dto.GetWorkingHoursDTO;
import com.epam.WorkloadMicroservice.dto.UpdateWorkingHoursDTO;
import com.epam.WorkloadMicroservice.entity.Trainer;
import com.epam.WorkloadMicroservice.service.TrainerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/trainer")
public class TrainerController {

    private final TrainerService trainerService;

    @Autowired
    public TrainerController(TrainerService trainerService) {
        this.trainerService = trainerService;
    }

    @PostMapping("/{username}/{action}")
    public ResponseEntity<String> updateWorkingHours(
            @PathVariable String username,
            @PathVariable String action,
            @RequestBody UpdateWorkingHoursDTO updateWorkingHoursDTO
    ){
        try {
            if(ActionType.valueOf(action)== ActionType.ADD){
                trainerService.addTraining(username, updateWorkingHoursDTO);
                return ResponseEntity.ok("Training hours added successfully.");
            }else{
                trainerService.removeTraining(username, updateWorkingHoursDTO);
                return ResponseEntity.ok("Training hours removed successfully.");
            }

        }catch (Exception e){
            return ResponseEntity.badRequest().body("Error updating working hours: " + e.getMessage());
        }
    }

    @PostMapping("/{username}")
    public ResponseEntity<String> getTrainer(
            @PathVariable String username,
            @RequestBody GetWorkingHoursDTO getWorkingHoursDTO
    ){
        try{
            int hours = trainerService.getTotalHours(username, getWorkingHoursDTO.YearNumber(), getWorkingHoursDTO.MonthNumber());
            return ResponseEntity.ok("Total hours: " + hours);
        }catch (Exception e){
            return ResponseEntity.badRequest().body("Error retrieving working hours: " + e.getMessage());
        }
    }

}
