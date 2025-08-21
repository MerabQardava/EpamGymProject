package com.epam.hw.controller;

import com.epam.hw.dto.*;
import com.epam.hw.entity.Trainee;
import com.epam.hw.monitoring.CustomMetricsService;
import com.epam.hw.repository.UserRepository;
import com.epam.hw.security.BruteForceProtectionService;
import com.epam.hw.service.TraineeService;
import com.epam.hw.service.UserService;
import com.epam.hw.storage.LoginResults;
import io.micrometer.core.instrument.Timer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Tag(name = "Trainee Controller", description = "Endpoints for managing trainees")
@RestController
@RequestMapping("/trainee")
@Slf4j
public class TraineeController {
    private final TraineeService traineeService;
    private final CustomMetricsService metricsService;
    private final UserService userService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);
    private final BruteForceProtectionService bruteForceProtectionService;

    @Autowired
    public TraineeController(TraineeService traineeService,
                             CustomMetricsService metricsService,
                             UserService userService,
                             BruteForceProtectionService bruteForceProtectionService) {
        this.traineeService = traineeService;
        this.metricsService = metricsService;
        this.userService= userService;
        this.bruteForceProtectionService = bruteForceProtectionService;
    }


    @Operation(summary = "Register a new trainee and return generated credentials")
    @PostMapping()
    public ResponseEntity<CreateUserReturnDTO> registerTrainee(@RequestBody @Valid TraineeRegistrationDTO dto){
        metricsService.recordRequest("POST", "/trainee");
        Timer.Sample sample = Timer.start();

        log.info("POST /trainee - Registering new trainee: {} {}", dto.firstName(), dto.lastName());
        Trainee saved = userService.register(dto.firstName(), dto.lastName(),LocalDate.parse(dto.dob()),dto.address(),dto.password());
        log.info("Trainee registered successfully with username: {}", saved.getUser().getUsername());

        sample.stop(metricsService.getRequestTimer());
        return ResponseEntity.status(HttpStatus.CREATED).body(new CreateUserReturnDTO(
                saved.getUser().getUsername(),
                saved.getUser().getPassword()));
    }



    @Operation(summary = "Login a trainee with username and password")
    @GetMapping("/login")
    public ResponseEntity<String> loginTrainee(@RequestParam String username,
                                               @RequestParam String password){
        metricsService.recordRequest("GET", "/trainee/login");
        Timer.Sample sample = Timer.start();
        log.info("GET /trainee/login - Attempting login for username: {}", username);

        if(bruteForceProtectionService.isBlocked(username)){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Too many failed attempts. Please try again later.");
        }

        try {
            String token = userService.verify(username, password);
            log.info("Login successful for {}", username);
            bruteForceProtectionService.recordSuccess(username);
            sample.stop(metricsService.getRequestTimer());
            return ResponseEntity.ok(token);
        }catch (Exception e){
            bruteForceProtectionService.recordFailedAttempt(username);
            sample.stop(metricsService.getRequestTimer());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }


    }

    @Operation(summary = "Change trainee password after verifying current credentials")
    @PutMapping("/login/{username}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<String> changeLogin(@PathVariable String username,
                                              @RequestBody @Valid UserPasswordChangeDTO dto) {
        metricsService.recordRequest("PUT", "/trainee/login/{username}");
        Timer.Sample sample = Timer.start();
        log.info("PUT /trainee/login/{} - Changing password", username);
        Trainee trainee = traineeService.getTraineeByUsername(username);
        if (!passwordEncoder.matches(dto.oldPassword(), trainee.getUser().getPassword())) {
            log.warn("Password change failed for {}: invalid current password", username);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid current password");
        }

        userService.changeTraineePassword(username, dto.newPassword());
        log.info("Password changed successfully for {}", username);
        sample.stop(metricsService.getRequestTimer());
        return ResponseEntity.ok("Password changed successfully");
    }

    @Operation(summary = "Fetch full profile of the trainee by username")
    @GetMapping("/{username}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<TraineeProfileDTO> getTraineeProfile(@PathVariable String username) {
        metricsService.recordRequest("GET", "/trainee/{username}");
        Timer.Sample sample = Timer.start();
        log.info("GET /trainee/{} - Fetching profile", username);

        Trainee trainee = traineeService.getTraineeByUsername(username);

        Set<TrainersListDTO> trainersSet = trainee.getTrainers().stream()
                .map(trainer -> new TrainersListDTO(
                        trainer.getUser().getUsername(),
                        trainer.getUser().getFirstName(),
                        trainer.getUser().getLastName(),
                        trainer.getSpecializationId()
                )).collect(Collectors.toSet());

        TraineeProfileDTO profileDTO = new TraineeProfileDTO(
                trainee.getUser().getFirstName(),
                trainee.getUser().getLastName(),
                trainee.getDateOfBirth().toString(),
                trainee.getAddress(),
                trainee.getUser().isActive(),
                trainersSet
        );

        log.info("Profile fetched for {}", username);
        sample.stop(metricsService.getRequestTimer());
        return ResponseEntity.ok(profileDTO);
    }


    @Operation(summary = "Delete a trainee by username")
    @DeleteMapping("/{username}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<String> deleteTrainee(@PathVariable String username) {
        metricsService.recordRequest("DELETE", "/trainee/{username}");
        Timer.Sample sample = Timer.start();

        log.info("DELETE /trainee/{} - Deleting trainee", username);
        boolean deleted = traineeService.deleteByUsername(username);

        if (deleted) {
            log.info("Trainee {} deleted successfully", username);
            sample.stop(metricsService.getRequestTimer());
            return ResponseEntity.ok("Trainee with username " + username + " deleted successfully");
        } else {
            log.warn("Trainee {} not found", username);
            sample.stop(metricsService.getRequestTimer());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Trainee with username " + username + " not found");
        }
    }

    @Operation(summary = "Update the profile of an existing trainee")
    @PutMapping("/{username}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UpdateTraineeReturnDTO> updateTraineeProfile(@PathVariable String username,
                                                                 @RequestBody @Valid UpdateTraineeDTO dto){
        metricsService.recordRequest("PUT", "/trainee/{username}");
        Timer.Sample sample = Timer.start();
        log.info("PUT /trainee/{} - Updating trainee profile", username);
        Trainee updatedTrainee = traineeService.updateTraineeProfile(username,new UpdateTraineeDTO(
                dto.firstName(),
                dto.lastName(),
                dto.dob(),
                dto.address(),
                dto.isActive()
        ));

        Set<TrainersListDTO> trainersSet = updatedTrainee.getTrainers().stream()
                .map(trainer -> new TrainersListDTO(
                        trainer.getUser().getUsername(),
                        trainer.getUser().getFirstName(),
                        trainer.getUser().getLastName(),
                        trainer.getSpecializationId()
                )).collect(Collectors.toSet());

        log.info("Profile updated for {}", username);
        sample.stop(metricsService.getRequestTimer());
        return ResponseEntity.ok(new UpdateTraineeReturnDTO(
                updatedTrainee.getUser().getUsername(),
                updatedTrainee.getUser().getFirstName(),
                updatedTrainee.getUser().getLastName(),
                updatedTrainee.getDateOfBirth().toString(),
                updatedTrainee.getAddress(),
                updatedTrainee.getUser().isActive(),
                trainersSet
        ));


    }


    @Operation(summary = "Toggle active/inactive status of the trainee")
    @PatchMapping("/{username}/status")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<String> toggleActivity(@PathVariable String username){
        metricsService.recordRequest("PATCH", "/trainee/{username}/status");
        Timer.Sample sample = Timer.start();
        log.info("PATCH /trainee/{}/status - Toggling status", username);
        traineeService.toggleTraineeStatus(username);
        log.info("Trainee {} status toggled", username);
        sample.stop(metricsService.getRequestTimer());
        return ResponseEntity.status(HttpStatus.OK).body("Trainee status toggled successfully");

    }


    @Operation(summary = "Update the list of trainers assigned to a trainee")
    @PutMapping("/{username}/trainers")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<TrainersListDTO>> updateTrainerList(
            @PathVariable String username,
            @RequestBody Set<String> trainerUsernames
    ){
        metricsService.recordRequest("PUT", "/trainee/{username}/trainers");
        Timer.Sample sample = Timer.start();
        log.info("PUT /trainee/{}/trainers - Updating trainers list", username);

        List<TrainersListDTO> updatedTrainers = traineeService.updateTraineeTrainers(username, trainerUsernames)
                .stream()
                .map(trainer -> new TrainersListDTO(
                        trainer.getUser().getUsername(),
                        trainer.getUser().getFirstName(),
                        trainer.getUser().getLastName(),
                        trainer.getSpecializationId()
                )).collect(Collectors.toList());

        log.info("Trainer list updated for {}", username);
        sample.stop(metricsService.getRequestTimer());
        return ResponseEntity.ok(updatedTrainers);


    }

    @Operation(summary = "Get filtered training sessions of a trainee by date range, trainer or type")
    @GetMapping("/{username}/trainings")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<TrainingDTO>> getTraineeTrainings(@PathVariable String username,
                                                                 @ModelAttribute GetTrainingOptionsDTO options
    ){
        metricsService.recordRequest("GET", "/trainee/{username}/trainings");
        Timer.Sample sample = Timer.start();
        log.info("GET /trainee/{}/trainings - Fetching trainings with filters: {}", username, options);
        Trainee trainee = traineeService.getTraineeByUsername(username);


        List<TrainingDTO> trainings = traineeService.getTraineeTrainings(
                        username,
                        options.startDate(),
                        options.endDate(),
                        options.trainerUsername(),
                        options.trainingTypeName()
                ).stream()
                .map(training -> new TrainingDTO(
                        training.getTrainingName(),
                        training.getDate(),
                        training.getTrainingType(),
                        training.getDuration(),
                        training.getTrainer().getUser().getUsername()
                ))
                .collect(Collectors.toList());
        log.info("Fetched {} trainings for {}", trainings.size(), username);
        sample.stop(metricsService.getRequestTimer());
        return ResponseEntity.ok(trainings);


    }




}
