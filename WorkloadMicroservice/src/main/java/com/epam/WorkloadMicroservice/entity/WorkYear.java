package com.epam.WorkloadMicroservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "work_year", uniqueConstraints = @UniqueConstraint(columnNames = {"year_number", "trainer_username"}))
@NoArgsConstructor
@Getter
@Setter
public class WorkYear {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "year_number", nullable = false)
    @Min(value = 2000)
    @Max(value = 9999)
    private int yearNumber;

    @ManyToOne
    @JoinColumn(name = "trainer_username", nullable = false)
    private Trainer trainer;

    @OneToMany(mappedBy = "workYear", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<WorkMonth> workMonths = new HashSet<>();

    public void addWorkMonth(WorkMonth workMonth) {
        workMonths.add(workMonth);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkYear workYear = (WorkYear) o;
        return yearNumber == workYear.yearNumber && trainer.equals(workYear.trainer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(yearNumber, trainer);
    }
}