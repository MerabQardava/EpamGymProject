package com.epam.WorkloadMicroservice.repository;

import com.epam.WorkloadMicroservice.entity.WorkMonth;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkMonthRepository extends JpaRepository<WorkMonth, Integer> {
}
