@rest

Feature: Simple logger test

  This feature provides examples on how to use the steps for testing REST APIs. All feature files that make use of
  the steps for testing REST APIs (such as this one) must include the "@rest" annotation at the beginning if the file.
  This is necessary, since it signals the library that it should bootstrap some necessary components for testing REST APIs

  Scenario: Some simple request
    Given I send requests to '${REST_SERVER_HOST}:3000'
    When I send a 'GET' request to '/posts'
    Then the service response status must be '200'
    And the service response must contain the text 'body'

  Scenario: How to save values from the response and using the value later in the scenario
    Given I send requests to '${REST_SERVER_HOST}:3000'
    When I send a 'GET' request to '/posts'
    Then the service response status must be '200'
    And I save element '$.[0].id' in environment variable 'VAR'
    When I send a 'GET' request to '/posts/!{VAR}'
    And in less than '5' seconds, checking each '1' seconds, I send a 'GET' request to '/posts/!{VAR}' so that the response contains 'body'
    Then the service response status must be '200' and its response must contain the text 'body'
