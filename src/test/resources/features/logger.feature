@rest

Feature: Simple logger test

  Scenario: Some simple request
    Given My app is running in 'jsonplaceholder.typicode.com:443'
    When I send a 'GET' request to '/posts'
    Then the service response status must be '200'
    And the service response must contain the text 'body'

  Scenario: Some simple rest request
    Given My app is running in 'jsonplaceholder.typicode.com:443'
    When I send a 'GET' request to '/posts'
    Then the service response status must be '200'
    And I save element '$.[0].id' in environment variable 'VAR'
    When I send a 'GET' request to '/posts/!{VAR}'
    And in less than '5' seconds, checking each '1' seconds, I send a 'GET' request to '/posts/!{VAR}' so that the response contains 'body'
    Then the service response status must be '200' and its response must contain the text 'body'
