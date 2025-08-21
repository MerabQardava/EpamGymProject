package com.epam.WorkloadMicroservice.service;

import com.epam.WorkloadMicroservice.dto.UpdateWorkingHoursDTO;
import com.epam.WorkloadMicroservice.entity.Trainer;
import com.epam.WorkloadMicroservice.entity.WorkMonth;
import com.epam.WorkloadMicroservice.entity.WorkYear;
import com.epam.WorkloadMicroservice.repository.TrainerRepository;
import com.epam.WorkloadMicroservice.repository.WorkMonthRepository;
import com.epam.WorkloadMicroservice.repository.WorkYearRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class TrainerService {
    private final TrainerRepository trainerRepository;
    private final WorkMonthRepository workMonthRepository;
    private final WorkYearRepository workYearRepository;

    @Autowired
    public TrainerService(TrainerRepository trainerRepository, WorkMonthRepository workMonthRepository, WorkYearRepository workYearRepository) {
        this.trainerRepository = trainerRepository;
        this.workMonthRepository = workMonthRepository;
        this.workYearRepository = workYearRepository;
    }

    @Transactional
    public void addTraining(String username, UpdateWorkingHoursDTO sessionData) {
        if (sessionData.trainingDuration() < 0) {
            throw new IllegalArgumentException("Training duration cannot be negative");
        }

        System.out.println(sessionData.trainingDuration());

        Trainer trainer = trainerRepository.findById(username)
                .orElseGet(() -> addTrainer(username, sessionData.firstName(), sessionData.lastName()));

        WorkYear workYear = trainer.getWorkYears().stream()
                .filter(year -> year.getYearNumber() == sessionData.date().getYear())
                .findFirst()
                .orElseGet(() -> addYear(sessionData.date().getYear(), trainer));

        WorkMonth workMonth = workYear.getWorkMonths().stream()
                .filter(month -> month.getMonthNumber() == sessionData.date().getMonthValue())
                .findFirst()
                .orElseGet(() -> addMonth(sessionData.date().getMonthValue(), workYear));

        workMonth.setTotalHours(workMonth.getTotalHours() + sessionData.trainingDuration());
        trainerRepository.save(trainer);
    }

    public void removeTraining(String username, UpdateWorkingHoursDTO sessionData) {
        if (sessionData.trainingDuration() < 0) {
            throw new IllegalArgumentException("Training duration cannot be negative");
        }

        Trainer trainer = trainerRepository.findById(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found"));

        WorkYear workYear = trainer.getWorkYears().stream()
                .filter(year -> year.getYearNumber() == sessionData.date().getYear())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Work year not found"));

        WorkMonth workMonth = workYear.getWorkMonths().stream()
                .filter(month -> month.getMonthNumber() == sessionData.date().getMonthValue())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Work month not found"));

        if (workMonth.getTotalHours() < sessionData.trainingDuration()) {
            throw new IllegalArgumentException("Cannot remove more hours than available");
        }

        workMonth.setTotalHours(workMonth.getTotalHours() - sessionData.trainingDuration());
        trainerRepository.save(trainer);
    }

    public int getTotalHours(String username, int year, int month) {
        Trainer trainer = trainerRepository.findById(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found"));

        WorkYear workYear = trainer.getWorkYears().stream()
                .filter(y -> y.getYearNumber() == year)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Work year not found"));

        WorkMonth workMonth = workYear.getWorkMonths().stream()
                .filter(m -> m.getMonthNumber() == month)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Work month not found"));

        return workMonth.getTotalHours();
    }

    private WorkMonth addMonth(int month, WorkYear workYear){
        WorkMonth workMonth = new WorkMonth(month, workYear);
        workYear.addWorkMonth(workMonth);
        return workMonth;
    }

    private WorkYear addYear(int Year,Trainer trainer){
        WorkYear workYear = new WorkYear();
        workYear.setYearNumber(Year);
        workYear.setTrainer(trainer);
        trainer.getWorkYears().add(workYear);
        return workYear;
    }

    private Trainer addTrainer(String username, String firstName, String lastName) {
        return new Trainer(username, firstName, lastName);
    }






}
