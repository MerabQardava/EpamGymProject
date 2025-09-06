package com.epam.WorkloadMicroservice.repository;

import com.epam.WorkloadMicroservice.entity.Trainer;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface TrainerRepository extends MongoRepository<Trainer, String> {




}
