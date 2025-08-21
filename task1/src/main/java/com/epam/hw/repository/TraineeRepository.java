package com.epam.hw.repository;

import com.epam.hw.entity.Trainee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TraineeRepository extends JpaRepository<Trainee, Integer> {

    Optional<Trainee> findByUser_Username(String username);

}
