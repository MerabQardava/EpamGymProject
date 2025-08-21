package com.epam.hw.entity;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
@Entity
public class Trainer{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name="specializationId")
    private  TrainingType specializationId;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="user_id", nullable=false, unique = true)
    private User user;

    @ManyToMany(mappedBy = "trainers")
    private Set<Trainee> trainees = new HashSet<>();

    @OneToMany(mappedBy = "trainer", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Training> trainings = new ArrayList<>();


    public Trainer(TrainingType specializationId, User user) {
        this.specializationId = specializationId;
        this.user = user;
    }


    public void addTrainee(Trainee trainee) {
        this.trainees.add(trainee);
        trainee.getTrainers().add(this);
    }

    public void removeTrainee(Trainee trainee) {
        this.trainees.remove(trainee);
        trainee.getTrainers().remove(this);
    }


    @Override
    public String toString() {
        return "Trainer{" +
                "specializationId=" + specializationId +
                ", userId=" + user.getUserId()+
                ", firstName='" + user.getFirstName() + '\'' +
                ", lastName='" + user.getLastName() + '\'' +
                ", username='" + user.getUsername() + '\'' +
                ", password='" + user.getPassword() + '\'' +
                ", isActive=" + user.isActive() +
                '}';
    }

}
