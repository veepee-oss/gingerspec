@rest

Feature: Test print comments via CLI

  Scenario: Testing comments via CLI
    #log this is a log comment, SHOULD be printed
    Given I send requests to '${REST_SERVER_HOST}:3000'
    #log comments can make use of variable replacement: REST_SERVER_HOST -> ${REST_SERVER_HOST}
    When I send a 'GET' request to '/posts'
    #This is a regular comment, SHOULD NOT be printed
    Then the service response status must be '200'
