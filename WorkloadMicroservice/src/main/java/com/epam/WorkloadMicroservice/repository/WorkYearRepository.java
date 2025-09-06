package com.epam.WorkloadMicroservice.repository;

import com.epam.WorkloadMicroservice.entity.WorkYear;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface WorkYearRepository extends MongoRepository<WorkYear, String> {

}
