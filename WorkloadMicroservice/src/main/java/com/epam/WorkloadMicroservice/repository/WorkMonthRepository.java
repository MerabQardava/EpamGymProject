package com.epam.WorkloadMicroservice.repository;

import com.epam.WorkloadMicroservice.entity.WorkMonth;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface WorkMonthRepository extends MongoRepository<WorkMonth, String> {
}
