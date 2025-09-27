Feature: Trainer Controller Workload Management

  Background:
    Given the workload microservice is running
    And a trainer with username "Jane.Smith" exists

  Scenario: Add training hours for a trainer
    When I send a POST request to "/trainer/Jane.Smith/ADD" with body:
      """
      {
        "date": "2025-09-20",
        "trainingDuration": 60,
        "firstName": "Jane",
        "lastName": "Smith"
      }
      """
    Then the response status should be 200
    And the response body should be "Training hours added successfully."

  Scenario: Remove training hours for a trainer
    Given a training session exists for trainer "Jane.Smith" on "2025-09-20" with duration 60
    When I send a POST request to "/trainer/Jane.Smith/REMOVE" with body:
      """
      {
        "date": "2025-09-20",
        "trainingDuration": 60,
        "firstName": "Jane",
        "lastName": "Smith"
      }
      """
    Then the response status should be 200
    And the response body should be "Training hours removed successfully."

  Scenario: Retrieve total working hours for a trainer
    Given a training session exists for trainer "Jane.Smith" on "2025-09-20" with duration 60
    When I send a POST request to "/trainer/Jane.Smith" with body:
      """
      {
        "YearNumber": 2025,
        "MonthNumber": 9
      }
      """
    Then the response status should be 200
    And the response body should be "Total hours: 60"