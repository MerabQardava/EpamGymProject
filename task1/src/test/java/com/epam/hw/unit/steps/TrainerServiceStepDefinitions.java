package com.epam.hw.unit.steps;

import com.epam.hw.dto.UpdateTrainerDTO;
import com.epam.hw.entity.Trainer;
import com.epam.hw.entity.User;
import com.epam.hw.repository.TrainerRepository;
import com.epam.hw.repository.UserRepository;
import com.epam.hw.service.TrainerService;
import com.epam.hw.storage.Auth;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.mockito.Mockito.*;

public class TrainerServiceStepDefinitions {

    @Mock
    TrainerRepository trainerRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    Auth auth;

    @Mock
    com.epam.hw.repository.TrainingRepository trainingRepository;

    @Mock
    com.epam.hw.repository.TrainingTypeRepository trainingTypeRepository;

    @InjectMocks
    TrainerService trainerService;

    private User loggedUser;
    private Trainer loggedTrainer;
    private Trainer dbTrainer;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        loggedUser = new User("Logged", "User");
        loggedTrainer = new Trainer();
        loggedUser.setTrainer(loggedTrainer);
        when(auth.getLoggedInUser()).thenReturn(loggedUser);
    }

    @Given("a trainer with a username of {string} exists in the database")
    public void a_trainer_with_username_exists_in_the_database(String username) {
        User dbUser = new User(username.split("\\.")[0], username.split("\\.")[1]);
        dbTrainer = new Trainer();
        dbUser.setTrainer(dbTrainer);
        when(trainerRepository.findByUser_Username(username)).thenReturn(Optional.of(dbTrainer));
    }

    @When("I retrieve the trainer by username {string}")
    public void i_retrieve_the_trainer_by_username(String username) {
        dbTrainer = trainerService.getTrainerByUsername(username);
    }

    @Then("the retrieved trainer should match the existing trainer")
    public void the_retrieved_trainer_should_match_the_existing_trainer() {
        Assertions.assertNotNull(dbTrainer, "Trainer should not be null");
    }

    @Given("a trainer with username {string} is active in the database")
    public void a_trainer_with_username_exists_and_is_active(String username) {
        User user = new User(username.split("\\.")[0], username.split("\\.")[1]);
        user.setActive(true);
        dbTrainer = new Trainer();
        user.setTrainer(dbTrainer);
        dbTrainer.setUser(user);
        when(trainerRepository.findByUser_Username(username)).thenReturn(Optional.of(dbTrainer));
    }

    @When("I toggle the status of trainer with username {string}")
    public void i_toggle_the_status_of_trainer_with_username(String username) {
        trainerService.toggleTrainerStatus(username);
    }

    @Then("the trainer status should be inactive")
    public void the_trainer_status_should_be_inactive() {
        Assertions.assertFalse(dbTrainer.getUser().isActive(), "Trainer should be inactive");
    }

    @Then("the trainer's user status should be saved in the repository")
    public void the_user_status_should_be_updated_in_the_repository() {
        verify(userRepository).save(dbTrainer.getUser());
    }

    @Given("a trainer with username {string} exists and is active")
    public void a_trainer_with_username_exists_and_is_active_with_username(String username) {
        User user = new User(username.split("\\.")[0], username.split("\\.")[1]);
        user.setActive(true);
        dbTrainer = new Trainer();
        user.setTrainer(dbTrainer);
        dbTrainer.setUser(user);
        when(trainerRepository.findByUser_Username(username)).thenReturn(Optional.of(dbTrainer));
    }

    @When("I update the trainer profile for username {string} with first name {string}, last name {string}, specialization {string}, and active status {string}")
    public void i_update_the_trainer_profile_for_username_with_first_name_last_name_specialization_and_active_status(
            String username, String firstName, String lastName, String specialization, String isActive) {
        UpdateTrainerDTO updateDTO = new UpdateTrainerDTO(firstName, lastName, specialization, Boolean.parseBoolean(isActive));
        dbTrainer = trainerService.updateTrainerProfile(username, updateDTO);
    }

    @Then("the trainer's first name should be {string}")
    public void the_trainers_first_name_should_be(String firstName) {
        Assertions.assertEquals(firstName, dbTrainer.getUser().getFirstName(), "First name should match");
    }

    @Then("the trainer's last name should be {string}")
    public void the_trainers_last_name_should_be(String lastName) {
        Assertions.assertEquals(lastName, dbTrainer.getUser().getLastName(), "Last name should match");
    }

    @Then("the trainer should be active")
    public void the_trainer_should_be_active() {
        Assertions.assertTrue(dbTrainer.getUser().isActive(), "Trainer should be active");
    }

    @Then("the trainer profile should be saved in the repository")
    public void the_trainer_profile_should_be_saved_in_the_repository() {
        verify(trainerRepository).save(dbTrainer);
    }
}