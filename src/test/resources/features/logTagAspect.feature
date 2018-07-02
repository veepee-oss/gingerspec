@rest

@ignore @toocomplex
Feature: Simple logger test

  Scenario: Some simple request
    Given I send requests to 'localhost:3000'
    When I send a 'GET' request to '/posts'
    Then the service response status must be '200'
    And the service response must contain the text 'body'
