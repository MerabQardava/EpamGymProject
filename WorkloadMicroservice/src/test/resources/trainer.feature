Feature: Trainer Workload Service Tests

  Scenario: Add training for new trainer
    Given a workload service with a trainer username "john.doe" does not exist
    When I add a training session with first name "John", last name "Doe", active status "false", date "2025-10-01", and duration 5
    Then the trainer should be created and saved
    And a work year for 2025 should be created for the trainer
    And a work month for 10 should be created with total hours 5

  Scenario: Add training for existing trainer
    Given a workload service with a trainer username "john.doe" exists with work year 2025 and work month 10 with 0 hours
    When I add a training session with first name "John", last name "Doe", active status "false", date "2025-10-01", and duration 5
    Then the trainer should be saved
    And the work month 10 total hours should be updated to 5

  Scenario: Add training with negative duration throws exception
    Given a workload service with a trainer username "john.doe" exists
    When I add a training session with first name "John", last name "Doe", active status "true", date "2025-10-01", and duration -5
    Then an exception "Training duration cannot be negative" should be thrown
    And the trainer should not be saved