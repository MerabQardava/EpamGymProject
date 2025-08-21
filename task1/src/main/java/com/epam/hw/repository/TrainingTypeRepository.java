package com.epam.hw.repository;

import com.epam.hw.entity.TrainingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrainingTypeRepository extends JpaRepository<TrainingType, Integer> {
    boolean existsByTrainingTypeName(String trainingTypeName);

    Optional<TrainingType> findByTrainingTypeName(String trainingTypeName);
}
