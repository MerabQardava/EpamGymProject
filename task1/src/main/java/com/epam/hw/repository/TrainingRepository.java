package com.epam.hw.repository;

import com.epam.hw.entity.Training;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TrainingRepository extends JpaRepository<Training, Integer> {

        @Query("""
        SELECT tr
        FROM Training tr
        JOIN tr.trainee t
        JOIN tr.trainer trn
        JOIN trn.user u
        WHERE t.user.username = :traineeUsername
        AND (:trainerUsername IS NULL OR u.username = :trainerUsername)
        AND (COALESCE(:from, CAST('1900-01-01' AS date)) IS NULL OR tr.date >= COALESCE(:from, CAST('1900-01-01' AS date)))
        AND (COALESCE(:to, CAST('2100-12-31' AS date)) IS NULL OR tr.date <= COALESCE(:to, CAST('2100-12-31' AS date)))
        AND (:trainingTypeName IS NULL OR tr.trainingType.trainingTypeName = :trainingTypeName)
        """)
        List<Training> findTrainingsByTraineeCriteria(
                @Param("traineeUsername") String traineeUsername,
                @Param("from") LocalDate from,
                @Param("to") LocalDate to,
                @Param("trainerUsername") String trainerUsername,
                @Param("trainingTypeName") String trainingTypeName
        );

        @Query("""
        SELECT tr
        FROM Training tr
        JOIN tr.trainee t
        JOIN tr.trainer trn
        JOIN trn.user u
        WHERE u.username = :trainerUsername
        AND (:traineeUsername IS NULL OR t.user.username = :traineeUsername)
        AND (COALESCE(:from, CAST('1900-01-01' AS date)) IS NULL OR tr.date >= COALESCE(:from, CAST('1900-01-01' AS date)))
        AND (COALESCE(:to, CAST('2100-12-31' AS date)) IS NULL OR tr.date <= COALESCE(:to, CAST('2100-12-31' AS date)))
        """)
        List<Training> findTrainingsByTrainerCriteria(
                @Param("trainerUsername") String trainerUsername,
                @Param("from") LocalDate from,
                @Param("to") LocalDate to,
                @Param("traineeUsername") String traineeUsername
        );
}

