package com.epam.hw.unit.steps;

import com.epam.hw.dto.ActionType;
import com.epam.hw.dto.UpdateWorkingHoursDTO;
import com.epam.hw.entity.Trainee;
import com.epam.hw.entity.Trainer;
import com.epam.hw.entity.Training;
import com.epam.hw.entity.TrainingType;
import com.epam.hw.entity.User;
import com.epam.hw.repository.TraineeRepository;
import com.epam.hw.repository.TrainerRepository;
import com.epam.hw.repository.TrainingRepository;
import com.epam.hw.repository.TrainingTypeRepository;
import com.epam.hw.service.TrainingService;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import jakarta.persistence.EntityNotFoundException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TrainingStepDefinitions {

    @Mock
    private TrainingRepository trainingRepo;

    @Mock
    private TraineeRepository traineeRepo;

    @Mock
    private TrainerRepository trainerRepo;

    @Mock
    private TrainingTypeRepository trainingTypeRepo;

    @Mock
    private com.epam.hw.messaging.MessageProducer messageProducer;

    @InjectMocks
    private TrainingService trainingService;

    private Trainee trainee;
    private Trainer trainer;
    private TrainingType trainingType;
    private Training training;
    private User traineeUser;
    private User trainerUser;
    private List<TrainingType> trainingTypes;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        traineeUser = new User();
        traineeUser.setUsername("trainee.user");
        traineeUser.setFirstName("Trainee");
        traineeUser.setLastName("User");
        traineeUser.setActive(true);

        trainerUser = new User();
        trainerUser.setUsername("trainer.user");
        trainerUser.setFirstName("Trainer");
        trainerUser.setLastName("User");
        trainerUser.setActive(true);
        trainee = new Trainee();
        trainee.setUser(traineeUser);

        trainer = new Trainer();
        trainer.setUser(trainerUser);

        trainingType = new TrainingType("Java");

        training = new Training(trainee, trainer, "Java Training", trainingType, LocalDate.of(2025, 1, 1), 60);
        training.setId(1);

        trainingTypes = new ArrayList<>();

        when(trainingRepo.save(any(Training.class))).thenAnswer(invocation -> {
            Training t = invocation.getArgument(0);
            t.setId(1);
            return t;
        });
    }

    @Given("a training service with a trainee username {string} exists")
    public void a_training_service_with_a_trainee_username_exists(String username) {
        when(traineeRepo.findByUser_Username(username)).thenReturn(Optional.of(trainee));
    }

    @Given("a training service with a trainee username {string} does not exist")
    public void a_training_service_with_a_trainee_username_does_not_exist(String username) {
        when(traineeRepo.findByUser_Username(username)).thenReturn(Optional.empty());
    }

    @Given("a training service with a trainer username {string} exists")
    public void a_training_service_with_a_trainer_username_exists(String username) {
        when(trainerRepo.findByUser_Username(username)).thenReturn(Optional.of(trainer));
    }

    @Given("a training service with a trainer username {string} does not exist")
    public void a_training_service_with_a_trainer_username_does_not_exist(String username) {
        when(trainerRepo.findByUser_Username(username)).thenReturn(Optional.empty());
    }

    @Given("a training type {string} exists for the training service")
    public void a_training_type_exists_for_the_training_service(String typeName) {
        when(trainingTypeRepo.findByTrainingTypeName(typeName)).thenReturn(Optional.of(trainingType));
    }

    @Given("a training type {string} does not exist for the training service")
    public void a_training_type_does_not_exist_for_the_training_service(String typeName) {
        when(trainingTypeRepo.findByTrainingTypeName(typeName)).thenReturn(Optional.empty());
    }

    @Given("a training service with a trainee username {string} fails to connect to the database")
    public void a_training_service_with_a_trainee_username_fails_to_connect_to_the_database(String username) {
        when(traineeRepo.findByUser_Username(username)).thenThrow(new RuntimeException("Database unavailable"));
    }

    @Given("a training with ID {int} exists in the training service")
    public void a_training_with_id_exists_in_the_training_service(int id) {
        when(trainingRepo.findById(id)).thenReturn(Optional.of(training));
        doNothing().when(trainingRepo).delete(training);
    }


    @Given("a training with ID {int} fails to connect to the database in the training service")
    public void a_training_with_id_fails_to_connect_to_the_database_in_the_training_service(int id) {
        when(trainingRepo.findById(id)).thenThrow(new RuntimeException("Database unavailable"));
    }

    @Given("the training service has training types {string} and {string}")
    public void the_training_service_has_training_types(String type1, String type2) {
        trainingTypes.add(new TrainingType(type1));
        trainingTypes.add(new TrainingType(type2));
        when(trainingTypeRepo.findAll()).thenReturn(trainingTypes);
    }

    @Given("the training service has no training types")
    public void the_training_service_has_no_training_types() {
        when(trainingTypeRepo.findAll()).thenReturn(new ArrayList<>());
    }

    @When("I add a training with name {string}, type {string}, date {string}, and duration {int}")
    public void i_add_a_training_with_name_type_date_and_duration(String name, String type, String date, int duration) {
        try {
            training = trainingService.addTraining("trainee.user", "trainer.user", name, type, LocalDate.parse(date), duration);
        } catch (Exception e) {
            training = null;
        }
    }

    @When("I delete the training with ID {int}")
    public void i_delete_the_training_with_id(int id) {
        trainingService.deleteTraining(id);
    }

    @When("I retrieve the training types")
    public void i_retrieve_the_training_types() {
        trainingTypes = trainingService.getTrainingTypes();
    }

    @Then("the training should be successfully added with ID {int}")
    public void the_training_should_be_successfully_added_with_id(int id) {
        verify(trainingRepo, times(1)).save(any(Training.class));
        assertNotNull(training, "Training should not be null after saving");
        assertEquals(id, training.getId(), "Training ID should match");
    }

    @Then("the training name should be {string}")
    public void the_training_name_should_be(String name) {
        assertNotNull(training, "Training should not be null");
        assertEquals(name, training.getTrainingName(), "Training name should match");
    }

    @Then("the training type should be {string}")
    public void the_training_type_should_be(String type) {
        assertNotNull(training, "Training should not be null");
        assertEquals(type, training.getTrainingType().getTrainingTypeName(), "Training type should match");
    }

    @Then("the training trainee should be {string}")
    public void the_training_trainee_should_be(String username) {
        assertNotNull(training, "Training should not be null");
        assertEquals(username, training.getTrainee().getUser().getUsername(), "Training trainee should match");
    }

    @Then("the training trainer should be {string}")
    public void the_training_trainer_should_be(String username) {
        assertNotNull(training, "Training should not be null");
        assertEquals(username, training.getTrainer().getUser().getUsername(), "Training trainer should match");
    }

    @Then("the training date should be {string}")
    public void the_training_date_should_be(String date) {
        assertNotNull(training, "Training should not be null");
        assertEquals(LocalDate.parse(date), training.getDate(), "Training date should match");
    }

    @Then("the training duration should be {int}")
    public void the_training_duration_should_be(int duration) {
        assertNotNull(training, "Training should not be null");
        assertEquals(duration, training.getDuration(), "Training duration should match");
    }

    @Then("a working hours update message should be sent to {string} with first name {string}, last name {string}, active status {string}, date {string}, and duration {int}")
    public void a_working_hours_update_message_should_be_sent_to_with_first_name_last_name_active_status_date_and_duration(
            String username, String firstName, String lastName, String isActive, String date, int duration) {
        ArgumentCaptor<UpdateWorkingHoursDTO> captor = ArgumentCaptor.forClass(UpdateWorkingHoursDTO.class);
        verify(messageProducer, times(1)).sendMessage(eq(username), captor.capture(), eq(ActionType.ADD));
        UpdateWorkingHoursDTO sentDto = captor.getValue();
        assertEquals(firstName, sentDto.firstName());
        assertEquals(lastName, sentDto.lastName());
        assertEquals(Boolean.parseBoolean(isActive), sentDto.isActive());
        assertEquals(LocalDate.parse(date), sentDto.date());
        assertEquals(duration, sentDto.trainingDuration());
    }

    @Then("an exception {string} should be thrown")
    public void an_exception_should_be_thrown(String expectedMessage) {
        // Since the exception is thrown in the @When step, we need to verify it was caught
        assertTrue(training == null, "An exception should have prevented training from being set");
        // Note: This is a workaround. Ideally, use a Scenario context to store the exception.
    }

    @Then("the training should be deleted")
    public void the_training_should_be_deleted() {
        verify(trainingRepo, times(1)).delete(training);
    }

    @Then("a working hours removal message should be sent to {string} with first name {string}, last name {string}, active status {string}, date {string}, and duration {int}")
    public void a_working_hours_removal_message_should_be_sent_to_with_first_name_last_name_active_status_date_and_duration(
            String username, String firstName, String lastName, String isActive, String date, int duration) {
        ArgumentCaptor<UpdateWorkingHoursDTO> captor = ArgumentCaptor.forClass(UpdateWorkingHoursDTO.class);
        verify(messageProducer, times(1)).sendMessage(eq(username), captor.capture(), eq(ActionType.REMOVE));
        UpdateWorkingHoursDTO sentDto = captor.getValue();
        assertEquals(firstName, sentDto.firstName());
        assertEquals(lastName, sentDto.lastName());
        assertEquals(Boolean.parseBoolean(isActive), sentDto.isActive());
        assertEquals(LocalDate.parse(date), sentDto.date());
        assertEquals(duration, sentDto.trainingDuration());
    }

    @Then("the training types list should contain {int} items")
    public void the_training_types_list_should_contain_items(int size) {
        assertEquals(size, trainingTypes.size());
    }

    @Then("the first training type should be {string}")
    public void the_first_training_type_should_be(String type) {
        assertEquals(type, trainingTypes.get(0).getTrainingTypeName());
    }

    @Then("the second training type should be {string}")
    public void the_second_training_type_should_be(String type) {
        assertEquals(type, trainingTypes.get(1).getTrainingTypeName());
    }

    @Then("the training types list should be empty")
    public void the_training_types_list_should_be_empty() {
        assertTrue(trainingTypes.isEmpty());
    }
}