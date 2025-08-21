package com.epam.hw.service;

import com.epam.hw.dto.TraineeRegistrationDTO;
import com.epam.hw.entity.Trainee;
import com.epam.hw.entity.Trainer;
import com.epam.hw.entity.TrainingType;
import com.epam.hw.entity.User;
import com.epam.hw.repository.TraineeRepository;
import com.epam.hw.repository.TrainerRepository;
import com.epam.hw.repository.TrainingTypeRepository;
import com.epam.hw.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@Slf4j
public class UserService{
    @Autowired
    private UserRepository userRepo;
    @Autowired
    public AuthenticationManager authManager;
    @Autowired
    private JWTService JwtService;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);


    private final UserRepository userRepository;
    private final TraineeRepository traineeRepository;
    private final TrainingTypeRepository trainingTypeRepository;
    private final TrainerRepository trainerRepository;

    @Autowired
    public UserService(UserRepository userRepository,
                       TraineeRepository traineeRepository,
                       TrainingTypeRepository trainingTypeRepository,
                       TrainerRepository trainerRepository) {
        this.userRepository = userRepository;
        this.traineeRepository = traineeRepository;
        this.trainingTypeRepository = trainingTypeRepository;
        this.trainerRepository = trainerRepository;
    }


    public Trainee register(String firstName, String lastName, LocalDate dateOfBirth, String address, String password) {

        User user = new User(firstName,lastName);
        user.setPassword(encoder.encode(password));

        String baseUsername = user.getUsername();
        int num = 1;

        log.debug("Creating new trainee with base username: {}", baseUsername);

        while (userRepository.findByUsername(user.getUsername()).isPresent()) {
            user.setUsername(baseUsername + num);
            num++;
        }

        Trainee trainee = new Trainee(dateOfBirth,address,user);


        traineeRepository.save(trainee);
        log.info("Trainee created with username: {}", trainee.getUser().getUsername());
        return trainee;
    }

    public Trainer register(String firstName,String lastName,String trainingTypeName,String password){
        TrainingType trainingType = trainingTypeRepository.findByTrainingTypeName(trainingTypeName)
                .orElseThrow(() -> new IllegalStateException("Training type " + trainingTypeName + " does not exist."));

        User user = new User(firstName,lastName);
        user.setPassword(encoder.encode(password));

        String baseUsername = user.getUsername();
        int num = 1;

        while (userRepository.findByUsername(user.getUsername()).isPresent()) {
            user.setUsername(baseUsername + num);
            num++;
        }

        Trainer trainer = new Trainer(trainingType, user);

        trainerRepository.save(trainer);
        log.info("Trainer created with username: {}", trainer.getUser().getUsername());
        return trainer;
    }

    public String verify(String username, String password) {
        Authentication authentication =
                authManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));


        if(authentication.isAuthenticated()) {
            return JwtService.generateToken(username);
        }

        return "fail";
    }

    public boolean changeTraineePassword(String username,String newPassword) {
        log.info("Changing password for logged-in trainee.");
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));

        if(user.getTrainee()==null) {
            log.warn("User {} is not a trainee, cannot change password.", username);
            throw new EntityNotFoundException("User is not a trainee: " + username);
        }

        user.setPassword(encoder.encode(newPassword));
        userRepository.save(user);
        return true;
    }

    public boolean changeTrainerPassword(String username, String newPassword) {
        log.info("Changing password for logged-in trainer.");
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));

        if(user.getTrainer()==null) {
            log.warn("User {} is not a trainer, cannot change password.", username);
            throw new EntityNotFoundException("User is not a trainer: " + username);
        }

        user.setPassword(encoder.encode(newPassword));
        userRepository.save(user);
        return true;
    }



}
