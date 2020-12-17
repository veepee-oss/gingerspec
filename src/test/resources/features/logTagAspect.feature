@rest
Feature: Testing the #log comments

  This feature provides examples on how to use the #log tag. This tag allows you to print comments during
  the execution of your tests. All comments that starts with the word #log will be printed. You can even
  use variables inside comments. The only condition is that #log must be placed right on top of a step, in
  that way, GingerSpec will recognize it as a comment of that step.

  Scenario: Testing comments via CLI
    #log this is a log comment, SHOULD be printed
    Given I send requests to '${REST_SERVER_HOST}:3000'
    #log comments can make use of variable replacement: REST_SERVER_HOST -> ${REST_SERVER_HOST}
    When I send a 'GET' request to '/posts'
    #This is a regular comment, SHOULD NOT be printed
    Then the service response status must be '200'
