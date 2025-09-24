package com.epam.WorkloadMicroservice.unit.steps;

import com.epam.WorkloadMicroservice.dto.UpdateWorkingHoursDTO;
import com.epam.WorkloadMicroservice.entity.Trainer;
import com.epam.WorkloadMicroservice.entity.WorkMonth;
import com.epam.WorkloadMicroservice.entity.WorkYear;
import com.epam.WorkloadMicroservice.repository.TrainerRepository;
import com.epam.WorkloadMicroservice.service.TrainerService;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class TrainerWorkloadStepDefinitions {

    @Mock
    private TrainerRepository trainerRepository;

    @InjectMocks
    private TrainerService trainerService;

    private UpdateWorkingHoursDTO sessionData;
    private Trainer trainer;
    private WorkYear workYear;
    private WorkMonth workMonth;
    private Exception lastException;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        sessionData = null;
        trainer = null;
        workYear = null;
        workMonth = null;
        lastException = null;
    }

    @Given("a workload service with a trainer username {string} does not exist")
    public void a_workload_service_with_a_trainer_username_does_not_exist(String username) {
        when(trainerRepository.findById(username)).thenReturn(Optional.empty());
    }

    @Given("a workload service with a trainer username {string} exists")
    public void a_workload_service_with_a_trainer_username_exists(String username) {
        trainer = new Trainer(username, "John", "Doe");
        when(trainerRepository.findById(username)).thenReturn(Optional.of(trainer));
    }

    @Given("a workload service with a trainer username {string} exists with work year {int} and work month {int} with {int} hours")
    public void a_workload_service_with_a_trainer_username_exists_with_work_year_and_work_month_with_hours(String username, int year, int month, int hours) {
        trainer = new Trainer(username, "John", "Doe");
        workYear = new WorkYear(year);
        workMonth = new WorkMonth(month, hours);
        trainer.setWorkYears(new HashSet<>());
        trainer.getWorkYears().add(workYear);
        workYear.setWorkMonths(new HashSet<>());
        workYear.getWorkMonths().add(workMonth);
        when(trainerRepository.findById(username)).thenReturn(Optional.of(trainer));
    }

    @When("I add a training session with first name {string}, last name {string}, active status {string}, date {string}, and duration {int}")
    public void i_add_a_training_session_with_first_name_last_name_active_status_date_and_duration(String firstName, String lastName, String active, String date, int duration) {
        try {
            sessionData = new UpdateWorkingHoursDTO(firstName, lastName, Boolean.parseBoolean(active), LocalDate.parse(date), duration);
            trainerService.addTraining("john.doe", sessionData);
            ArgumentCaptor<Trainer> trainerCaptor = ArgumentCaptor.forClass(Trainer.class);
            verify(trainerRepository).save(trainerCaptor.capture());
            trainer = trainerCaptor.getValue();
            lastException = null;
        } catch (Exception e) {
            lastException = e;
        }
    }

    @Then("the trainer should be created and saved")
    public void the_trainer_should_be_created_and_saved() {
        verify(trainerRepository, times(1)).save(any(Trainer.class));
        assertNotNull(trainer, "Saved trainer should not be null");
        WorkYear savedYear = trainer.getWorkYears().stream()
                .filter(y -> y.getYearNumber() == 2025)
                .findFirst()
                .orElse(null);
        assertNotNull(savedYear, "WorkYear should be created");
        WorkMonth savedMonth = savedYear.getWorkMonths().stream()
                .filter(m -> m.getMonthNumber() == 10)
                .findFirst()
                .orElse(null);
        assertNotNull(savedMonth, "WorkMonth should be created");
        assertEquals(5, savedMonth.getTotalHours(), "Total hours should be updated to 5");
    }

    @Then("a work year for {int} should be created for the trainer")
    public void a_work_year_for_should_be_created_for_the_trainer(int year) {
        assertNotNull(trainer, "Trainer should not be null");
        WorkYear savedYear = trainer.getWorkYears().stream()
                .filter(y -> y.getYearNumber() == year)
                .findFirst()
                .orElse(null);
        assertNotNull(savedYear, "WorkYear should be created");
    }

    @Then("a work month for {int} should be created with total hours {int}")
    public void a_work_month_for_should_be_created_with_total_hours(int month, int hours) {
        assertNotNull(trainer, "Trainer should not be null");
        WorkYear savedYear = trainer.getWorkYears().stream()
                .filter(y -> y.getYearNumber() == 2025)
                .findFirst()
                .orElse(null);
        assertNotNull(savedYear, "WorkYear should not be null");
        WorkMonth savedMonth = savedYear.getWorkMonths().stream()
                .filter(m -> m.getMonthNumber() == month)
                .findFirst()
                .orElse(null);
        assertNotNull(savedMonth, "WorkMonth should be created");
        assertEquals(hours, savedMonth.getTotalHours(), "Total hours should be updated to " + hours);
    }

    @Then("the trainer should be saved")
    public void the_trainer_should_be_saved() {
        verify(trainerRepository, times(1)).save(trainer);
    }

    @Then("the work month {int} total hours should be updated to {int}")
    public void the_work_month_total_hours_should_be_updated_to(int month, int hours) {
        assertNotNull(trainer, "Trainer should not be null");
        WorkYear savedYear = trainer.getWorkYears().stream()
                .filter(y -> y.getYearNumber() == 2025)
                .findFirst()
                .orElse(null);
        assertNotNull(savedYear, "WorkYear should not be null");
        WorkMonth updatedMonth = savedYear.getWorkMonths().stream()
                .filter(m -> m.getMonthNumber() == month)
                .findFirst()
                .orElse(null);
        assertNotNull(updatedMonth, "WorkMonth should not be null");
        assertEquals(hours, updatedMonth.getTotalHours(), "Total hours should be updated to " + hours);
    }

    @Then("an exception {string} should be thrown")
    public void an_exception_should_be_thrown(String expectedMessage) {
        assertNotNull(lastException, "An exception should have been thrown");
        assertEquals(expectedMessage, lastException.getMessage(), "Exception message should match");
    }

    @Then("the trainer should not be saved")
    public void the_trainer_should_not_be_saved() {
        verify(trainerRepository, never()).save(any(Trainer.class));
    }
}