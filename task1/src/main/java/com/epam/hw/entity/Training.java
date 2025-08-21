package com.epam.hw.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Training {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "trainee_id", nullable = false)
    private Trainee trainee;

    @ManyToOne
    @JoinColumn(name = "trainer_id", nullable = false)
    private Trainer trainer;

    @Column(nullable = false)
    private String trainingName;

    @ManyToOne
    private TrainingType trainingType;

    @Column(nullable = false)
    private LocalDate date;
    @Column(nullable = false)
    private Integer duration;

    public Training(Trainee traineeId, Trainer trainerId, String trainingName, TrainingType trainingType, LocalDate date, Integer duration) {
        this.trainee = traineeId;
        this.trainer = trainerId;
        this.trainingName = trainingName;
        this.trainingType= trainingType;
        this.date = date;
        this.duration = duration;
    }


    @Override
    public String toString() {
        return "Training{" +
                "traineeId=" + trainee +
                ", trainerId=" + trainer +
                ", trainingName='" + trainingName + '\'' +
                ", trainingType='" + trainingType + '\'' +
                ", date=" + date +
                ", duration='" + duration + '\'' +
                '}';
    }
}
