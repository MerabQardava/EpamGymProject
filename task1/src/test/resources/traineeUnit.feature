Feature: Trainee Service Tests

  Scenario: test scenario
    Given a user with name "John" and surname "Doe" exists
    When I retrieve user "John.Doe"
    Then name should be "John"
    And surname should be "Doe"

  Scenario: Retrieve trainee by username
    Given a trainee with username "A.B" exists in the database
    When I retrieve the trainee by username "A.B"
    Then the retrieved trainee should match the existing trainee

  Scenario: Toggle trainee status
    Given a trainee with username "A.B" exists and is active
    When I toggle the status of trainee with username "A.B"
    Then the trainee status should be inactive
    And the user status should be updated in the repository

  Scenario: Delete trainee by username
    Given a trainee with username "A.B" exists in the database
    When I delete the trainee with username "A.B"
    Then the deletion should be successful
    And the trainee should be removed from the repository

  Scenario: Add trainer to trainee
    Given a trainee with username "A.B" exists in the database
    And a trainer with username "C.D" exists in the database
    When I add the trainer "C.D" to the trainee "A.B"
    Then the trainer should be linked to the trainee

  Scenario: Remove trainer from trainee
    Given a trainee with username "A.B" exists in the database
    And a trainer with username "C.D" exists in the database
    And the trainer "C.D" is linked to the trainee "A.B"
    When I remove the trainer "C.D" from the trainee "A.B"
    Then the trainer should no longer be linked to the trainee
