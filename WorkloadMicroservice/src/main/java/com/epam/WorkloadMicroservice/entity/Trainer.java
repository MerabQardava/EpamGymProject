package com.epam.WorkloadMicroservice.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.HashSet;
import java.util.Set;

@Document(collection = "trainers")
@Getter
@NoArgsConstructor
@CompoundIndexes({
        @CompoundIndex(name = "trainer_name_idx", def = "{'firstName': 1, 'lastName': 1}")
})
@Setter
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
