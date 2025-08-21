package com.epam.WorkloadMicroservice.repository;

import com.epam.WorkloadMicroservice.entity.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;

public interface TrainerRepository extends JpaRepository<Trainer, String> {




}
