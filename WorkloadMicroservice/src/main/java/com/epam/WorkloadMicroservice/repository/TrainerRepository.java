package com.epam.WorkloadMicroservice.repository;

import com.epam.WorkloadMicroservice.entity.Trainer;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;


public interface TrainerRepository extends MongoRepository<Trainer, String> {
    List<Trainer> findByFirstNameAndLastName(String firstName, String lastName);


}
