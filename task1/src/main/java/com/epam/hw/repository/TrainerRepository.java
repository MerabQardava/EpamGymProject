package com.epam.hw.repository;

import com.epam.hw.entity.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface TrainerRepository extends JpaRepository<Trainer, Integer> {

    Optional<Trainer> findByUser_Username(String username);

    Set<Trainer> findAllByUser_UsernameIn(Set<String> username);
}
