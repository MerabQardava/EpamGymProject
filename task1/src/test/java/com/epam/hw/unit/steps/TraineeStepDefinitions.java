package com.epam.hw.unit.steps;

import com.epam.hw.entity.Trainee;
import com.epam.hw.entity.Trainer;
import com.epam.hw.entity.User;
import com.epam.hw.repository.TraineeRepository;
import com.epam.hw.repository.TrainerRepository;
import com.epam.hw.repository.TrainingRepository;
import com.epam.hw.repository.UserRepository;
import com.epam.hw.service.TraineeService;
import com.epam.hw.storage.Auth;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TraineeStepDefinitions {

    @Mock
    TraineeRepository traineeRepo;
    @Mock
    UserRepository userRepo;
    @Mock
    TrainerRepository trainerRepo;
    @Mock
    TrainingRepository trainingRepo;

    @Mock
    Auth auth;

    @InjectMocks
    TraineeService traineeService;

    private Trainee trainee;
    private User loggedUser;
    private Trainee loggedTrainee;
    private Trainee dbTrainee;
    private Trainer dbTrainer;
    private boolean deleteSuccess=false;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        loggedUser = new User("Logged", "User");
        loggedTrainee = new Trainee();
        loggedUser.setTrainee(loggedTrainee);
        when(auth.getLoggedInUser()).thenReturn(loggedUser);
    }

    @Given("a user with name {string} and surname {string} exists")
    public void a_user_with_name_and_surname_exists(String name, String surname) {
        User user = new User(name, surname);
        trainee = new Trainee(LocalDate.parse("2000-01-01"), "123 Main St", user);

        when(traineeRepo.findByUser_Username(user.getUsername())).thenReturn(Optional.of(trainee));
    }

    @When("I retrieve user {string}")
    public void i_retrieve_user(String username) {
        trainee = traineeService.getTraineeByUsername(username);
    }

    @Then("name should be {string}")
    public void name_should_be(String name) {
        Assertions.assertNotNull(trainee, "Trainee should not be null");
        Assertions.assertEquals(name, trainee.getUser().getFirstName(), "name should match");
    }

    @And("surname should be {string}")
    public void surnameShouldBe(String surname) {
        Assertions.assertNotNull(trainee, "Trainee should not be null");
        Assertions.assertEquals(surname, trainee.getUser().getLastName(), "surname should match");
    }


    @Given("a trainee with username {string} exists in the database")
    public void a_trainee_with_username_exists_in_the_database(String username) {
        User dbUser = new User(username.split("\\.")[0], username.split("\\.")[1]);
        dbTrainee = new Trainee();
        dbUser.setTrainee(dbTrainee);
        when(traineeRepo.findByUser_Username(username)).thenReturn(Optional.of(dbTrainee));
        when(userRepo.findByUsername(username)).thenReturn(Optional.of(dbUser));
    }

    @When("I retrieve the trainee by username {string}")
    public void i_retrieve_the_trainee_by_username(String username) {
        dbTrainee = traineeService.getTraineeByUsername(username);
    }

    @Then("the retrieved trainee should match the existing trainee")
    public void the_retrieved_trainee_should_match_the_existing_trainee() {
        Assertions.assertNotNull(dbTrainee, "Trainee should not be null");
    }

    @Given("a trainee with username {string} exists and is active")
    public void a_trainee_with_username_exists_and_is_active(String username) {
        User user = new User(username.split("\\.")[0], username.split("\\.")[1]);
        user.setActive(true);
        dbTrainee = new Trainee();
        user.setTrainee(dbTrainee);
        dbTrainee.setUser(user);
        when(traineeRepo.findByUser_Username(username)).thenReturn(Optional.of(dbTrainee));
    }

    @When("I toggle the status of trainee with username {string}")
    public void i_toggle_the_status_of_trainee_with_username(String username) {
        traineeService.toggleTraineeStatus(username);
    }

    @Then("the trainee status should be inactive")
    public void the_trainee_status_should_be_inactive() {
        Assertions.assertFalse(dbTrainee.getUser().isActive(), "Trainee should be inactive");
    }

    @Then("the user status should be updated in the repository")
    public void the_user_status_should_be_updated_in_the_repository() {
        verify(userRepo).save(dbTrainee.getUser());
    }

    @Given("a trainer with username {string} exists in the database")
    public void a_trainer_with_username_exists_in_the_database(String username) {
        User trainerUser = new User(username.split("\\.")[0], username.split("\\.")[1]);
        dbTrainer = new Trainer();
        trainerUser.setTrainer(dbTrainer);
        when(trainerRepo.findByUser_Username(username)).thenReturn(Optional.of(dbTrainer));
    }

    @When("I add the trainer {string} to the trainee {string}")
    public void i_add_the_trainer_to_the_trainee(String trainerUsername, String traineeUsername) {
        traineeService.addTrainerToTrainee(traineeUsername, trainerUsername);
    }

    @Then("the trainer should be linked to the trainee")
    public void the_trainer_should_be_linked_to_the_trainee() {
        Assertions.assertTrue(dbTrainee.getTrainers().contains(dbTrainer), "Trainer should be linked");
    }

    @Given("the trainer {string} is linked to the trainee {string}")
    public void the_trainer_is_linked_to_the_trainee(String trainerUsername, String traineeUsername) {
        a_trainer_with_username_exists_in_the_database(trainerUsername);
        a_trainee_with_username_exists_in_the_database(traineeUsername);
        dbTrainee.getTrainers().add(dbTrainer);
    }

    @When("I remove the trainer {string} from the trainee {string}")
    public void i_remove_the_trainer_from_the_trainee(String trainerUsername, String traineeUsername) {
        traineeService.removeTrainerFromTrainee(traineeUsername, trainerUsername);
    }

    @Then("the trainer should no longer be linked to the trainee")
    public void the_trainer_should_no_longer_be_linked_to_the_trainee() {
        Assertions.assertTrue(dbTrainee.getTrainers().isEmpty(), "Trainer should be removed");
    }

    @When("I delete the trainee with username {string}")
    public void i_delete_the_trainee_with_username(String username) {
        deleteSuccess=traineeService.deleteByUsername(username);
        System.out.println("Delete success: " + deleteSuccess);
    }

    @Then("the deletion should be successful")
    public void the_deletion_should_be_successful() {
        Assertions.assertTrue(deleteSuccess);
    }

    @Then("the trainee should be removed from the repository")
    public void the_trainee_should_be_removed_from_the_repository() {
        verify(traineeRepo).delete(dbTrainee);
    }
}