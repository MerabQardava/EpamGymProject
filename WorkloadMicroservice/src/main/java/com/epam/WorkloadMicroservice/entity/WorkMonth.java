package com.epam.WorkloadMicroservice.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@Setter
public class WorkMonth {
    private Integer id;
    private int monthNumber;
    private int totalHours;

    public WorkMonth(int monthNumber, int totalHours) {
        this.monthNumber = monthNumber;
        this.totalHours = totalHours;
    }
}