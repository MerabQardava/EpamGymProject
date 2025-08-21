package com.epam.WorkloadMicroservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

import java.util.Objects;

@Entity
@Table(name = "work_month")
@NoArgsConstructor
@Getter
@Setter
public class WorkMonth {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "month_number", nullable = false)
    @Min(value = 1)
    @Max(value = 12)
    private int monthNumber;

    @Column(name = "total_hours", nullable = false)
    private int totalHours;

    @ManyToOne
    @JoinColumn(name = "work_year_id", nullable = false)
    private WorkYear workYear;

    public WorkMonth(int monthNumber, WorkYear workYear) {
        this.monthNumber = monthNumber;
        this.workYear = workYear;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkMonth workMonth = (WorkMonth) o;
        return monthNumber == workMonth.monthNumber && workYear.equals(workMonth.workYear);
    }

    @Override
    public int hashCode() {
        return Objects.hash(monthNumber, workYear);
    }
}
