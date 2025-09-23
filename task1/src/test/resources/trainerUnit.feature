Feature: Trainer Service Tests

  Scenario: Retrieve trainer by username
    Given a trainer with a username of "A.B" exists in the database
    When I retrieve the trainer by username "A.B"
    Then the retrieved trainer should match the existing trainer

  Scenario: Toggle trainer status
    Given a trainer with username "A.B" is active in the database
    When I toggle the status of trainer with username "A.B"
    Then the trainer status should be inactive
    And the trainer's user status should be saved in the repository

  Scenario: Update trainer profile
    Given a trainer with username "John.Doe" exists and is active
    When I update the trainer profile for username "John.Doe" with first name "Johnny", last name "Smith", specialization "Java", and active status "true"
    Then the trainer's first name should be "Johnny"
    And the trainer's last name should be "Smith"
    And the trainer should be active
    And the trainer profile should be saved in the repository