package com.epam.WorkloadMicroservice.service;

import com.epam.WorkloadMicroservice.dto.UpdateWorkingHoursDTO;
import com.epam.WorkloadMicroservice.entity.Trainer;
import com.epam.WorkloadMicroservice.entity.WorkMonth;
import com.epam.WorkloadMicroservice.entity.WorkYear;
import com.epam.WorkloadMicroservice.repository.TrainerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TrainerService {

    private final TrainerRepository trainerRepository;

    @Autowired
    public TrainerService(TrainerRepository trainerRepository) {
        this.trainerRepository = trainerRepository;
    }

    public void addTraining(String username, UpdateWorkingHoursDTO sessionData) {
        log.info("Adding training for username={}, sessionData={}", username, sessionData);

        if (sessionData.trainingDuration() < 0) {
            log.warn("Training duration is negative: {}", sessionData.trainingDuration());
            throw new IllegalArgumentException("Training duration cannot be negative");
        }

        Trainer trainer = trainerRepository.findById(username)
                .orElseGet(() -> {
                    log.info("Trainer not found, creating new: username={}, firstName={}, lastName={}",
                            username, sessionData.firstName(), sessionData.lastName());
                    return addTrainer(username, sessionData.firstName(), sessionData.lastName());
                });

        WorkYear workYear = trainer.getWorkYears().stream()
                .filter(year -> year.getYearNumber() == sessionData.date().getYear())
                .findFirst()
                .orElseGet(() -> {
                    log.info("Work year not found, creating new for year={}", sessionData.date().getYear());
                    return addYear(sessionData.date().getYear(), trainer);
                });

        WorkMonth workMonth = workYear.getWorkMonths().stream()
                .filter(month -> month.getMonthNumber() == sessionData.date().getMonthValue())
                .findFirst()
                .orElseGet(() -> {
                    log.info("Work month not found, creating new for month={}", sessionData.date().getMonthValue());
                    return addMonth(sessionData.date().getMonthValue(), workYear);
                });

        workMonth.setTotalHours(workMonth.getTotalHours() + sessionData.trainingDuration());
        trainerRepository.save(trainer);

        log.info("Training added successfully: username={}, year={}, month={}, totalHours={}",
                username, workYear.getYearNumber(), workMonth.getMonthNumber(), workMonth.getTotalHours());
    }

    public void removeTraining(String username, UpdateWorkingHoursDTO sessionData) {
        log.info("Removing training for username={}, sessionData={}", username, sessionData);

        if (sessionData.trainingDuration() < 0) {
            log.warn("Training duration is negative: {}", sessionData.trainingDuration());
            throw new IllegalArgumentException("Training duration cannot be negative");
        }

        Trainer trainer = trainerRepository.findById(username)
                .orElseThrow(() -> {
                    log.warn("Trainer not found: {}", username);
                    return new IllegalArgumentException("Trainer not found");
                });

        WorkYear workYear = trainer.getWorkYears().stream()
                .filter(year -> year.getYearNumber() == sessionData.date().getYear())
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("Work year not found: {}", sessionData.date().getYear());
                    return new IllegalArgumentException("Work year not found");
                });

        WorkMonth workMonth = workYear.getWorkMonths().stream()
                .filter(month -> month.getMonthNumber() == sessionData.date().getMonthValue())
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("Work month not found: {}", sessionData.date().getMonthValue());
                    return new IllegalArgumentException("Work month not found");
                });

        if (workMonth.getTotalHours() < sessionData.trainingDuration()) {
            log.warn("Cannot remove more hours than available: totalHours={}, requested={}",
                    workMonth.getTotalHours(), sessionData.trainingDuration());
            throw new IllegalArgumentException("Cannot remove more hours than available");
        }

        workMonth.setTotalHours(workMonth.getTotalHours() - sessionData.trainingDuration());
        trainerRepository.save(trainer);

        log.info("Training removed successfully: username={}, year={}, month={}, totalHours={}",
                username, workYear.getYearNumber(), workMonth.getMonthNumber(), workMonth.getTotalHours());
    }

    public int getTotalHours(String username, int year, int month) {
        log.info("Fetching total hours for username={}, year={}, month={}", username, year, month);

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

        int totalHours = workMonth.getTotalHours();
        log.info("Total hours fetched: username={}, year={}, month={}, totalHours={}", username, year, month, totalHours);
        return totalHours;
    }

    // --- private helper methods ---
    private WorkMonth addMonth(int month, WorkYear workYear){
        WorkMonth workMonth = new WorkMonth(month, 0);
        workYear.addWorkMonth(workMonth);
        return workMonth;
    }

    private WorkYear addYear(int year, Trainer trainer){
        WorkYear workYear = new WorkYear(year);
        trainer.getWorkYears().add(workYear);
        return workYear;
    }

    private Trainer addTrainer(String username, String firstName, String lastName) {
        return new Trainer(username, firstName, lastName);
    }
}
