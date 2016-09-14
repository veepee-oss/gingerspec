@rest

  Feature: Simple logger test

    Scenario: Some simple request
      Given My app is running in 'jenkins.stratio.com:80'
      When I send a 'GET' request to '/'
      Then the service response status must be '200'.
      And the service response must contain the text 'Jenkins'

    Scenario: Some simple rest request
      Given My app is running in 'jenkins.stratio.com:80'
      When I send a 'GET' request to '/api/json'
      Then the service response status must be '200'.
      And I save element '$.views[0].url' in environment variable 'VAR'
      When I send a 'GET' request to '/!{VAR}'
      Then the service response status must be '404'.
