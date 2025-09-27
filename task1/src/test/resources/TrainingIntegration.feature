Feature: Training Management

  Background:
    Given the application is running

  Scenario: Retrieve all available training types
    When I send a GET request to "/training/types"
    Then the response status should be 200
    And the response body should contain a list of training types

  Scenario: Create a new training session
    When I send a POST request to "/training/trainee/John.Doe/trainer/Jane.Smith" with body:
      """
      {
        "trainingName": "Yoga Session",
        "trainingTypeName": "YOGA",
        "date": "2025-09-20",
        "duration": 60
      }
      """
    Then the response status should be 202
    And the response body should be "Training creation request accepted"

  Scenario: Delete a training session by ID
    Given a training with ID 1 exists
    When I send a DELETE request to "/training/1"
    Then the response status should be 202
    And the response body should be "Training deletion request accepted"