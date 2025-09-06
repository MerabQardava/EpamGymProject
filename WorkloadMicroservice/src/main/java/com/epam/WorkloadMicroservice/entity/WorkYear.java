package com.epam.WorkloadMicroservice.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@NoArgsConstructor
@Setter
public class WorkYear {
    private Integer id;
    private int yearNumber;
    private Set<WorkMonth> workMonths = new HashSet<>();

    public WorkYear(int yearNumber) {
        this.yearNumber = yearNumber;
    }

    public void addWorkMonth(WorkMonth workMonth) {
        workMonths.add(workMonth);
    }
}