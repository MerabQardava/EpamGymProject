Feature: Training Service Tests

  Scenario: Successfully add a training
    Given a training service with a trainee username "trainee.user" exists
    And a training service with a trainer username "trainer.user" exists
    And a training type "Java" exists for the training service
    When I add a training with name "Java Training", type "Java", date "2025-01-01", and duration 60
    Then the training should be successfully added with ID 1
    And the training name should be "Java Training"
    And the training type should be "Java"
    And the training trainee should be "trainee.user"
    And the training trainer should be "trainer.user"
    And the training date should be "2025-01-01"
    And the training duration should be 60
    And a working hours update message should be sent to "trainer.user" with first name "Trainer", last name "User", active status "true", date "2025-01-01", and duration 60

  Scenario: Fail to add training due to missing trainee
    Given a training service with a trainee username "trainee.user" does not exist
    When I add a training with name "Java Training", type "Java", date "2025-01-01", and duration 60
    Then an exception "Trainee not found" should be thrown

  Scenario: Fail to add training due to missing trainer
    Given a training service with a trainee username "trainee.user" exists
    And a training service with a trainer username "trainer.user" does not exist
    When I add a training with name "Java Training", type "Java", date "2025-01-01", and duration 60
    Then an exception "Trainer not found" should be thrown

  Scenario: Fail to add training due to missing training type
    Given a training service with a trainee username "trainee.user" exists
    And a training service with a trainer username "trainer.user" exists
    And a training type "Java" does not exist for the training service
    When I add a training with name "Java Training", type "Java", date "2025-01-01", and duration 60
    Then an exception "TrainingType not found" should be thrown

  Scenario: Fail to add training due to database error
    Given a training service with a trainee username "trainee.user" fails to connect to the database
    When I add a training with name "Java Training", type "Java", date "2025-01-01", and duration 60
    Then an exception "Database unavailable" should be thrown

  Scenario: Successfully delete a training
    Given a training with ID 1 exists in the training service
    When I delete the training with ID 1
    Then the training should be deleted
    And a working hours removal message should be sent to "trainer.user" with first name "Trainer", last name "User", active status "true", date "2025-01-01", and duration 60

  Scenario: Successfully get training types
    Given the training service has training types "Java" and "Spring"
    When I retrieve the training types
    Then the training types list should contain 2 items
    And the first training type should be "Java"
    And the second training type should be "Spring"

  Scenario: Get empty training types list
    Given the training service has no training types
    When I retrieve the training types
    Then the training types list should be empty