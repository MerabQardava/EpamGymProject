package com.epam.WorkloadMicroservice.repository;

import com.epam.WorkloadMicroservice.entity.WorkYear;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkYearRepository extends JpaRepository<WorkYear, Integer> {

}
