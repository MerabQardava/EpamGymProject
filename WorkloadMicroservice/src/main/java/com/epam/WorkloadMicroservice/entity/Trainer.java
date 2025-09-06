package com.epam.WorkloadMicroservice.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.HashSet;
import java.util.Set;

@Document(collection = "trainers")
@Getter
@NoArgsConstructor
public class Trainer {
    @Id
    private String username;
    private String firstName;
    private String lastName;
    private boolean isActive = true;
    private Set<WorkYear> workYears = new HashSet<>();

    public Trainer(String username, String firstName, String lastName) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
