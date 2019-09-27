@rest
Feature: Rest Assured Feature

  This feature provides examples on how to use the steps for testing REST APIs. All feature files that make use of
  the steps for testing REST APIs (such as this one) must include the "@rest" annotation at the beginning of the file.
  This is necessary, since it signals the library that it should bootstrap some necessary components for testing REST APIs


  Scenario: How to perform a simple request, verify the HTTP code returned, and check the body and its schema
    Given I send requests to '${REST_SERVER_HOST}:3000'
    When I send a 'GET' request to '/posts'
    Then the service response status must be '200' and its response must contain the text 'body'
    Then the service response status must be '200' and its response matches the schema in 'schemas/responseSchema.json'
    When I send a 'GET' request to '/comments/1'
    Then the service response status must be '200' and its response length must be '268'
    And the service response must contain the text 'body'
    And in less than '10' seconds, checking each '2' seconds, I send a 'GET' request to '/posts' so that the response contains 'body'


  Scenario: A new element is inserted via a POST call
    Given I send requests to '${REST_SERVER_HOST}:3000'
    When I send a 'POST' request to '/posts' based on 'schemas/mytestdata.json' as 'json'
    Then the service response status must be '201'
    And I save element '$.title' in environment variable 'TITLE'
    Then '!{TITLE}' matches 'This is a test'


  Scenario: Data in local file is altered using a datatable before sending
    Given I send requests to '${REST_SERVER_HOST}:3000'
    When I send a 'POST' request to '/posts' based on 'schemas/mytestdata.json' as 'json' with:
      | $.title | UPDATE | This is a test 2 |
    Then the service response status must be '201'
    And I save element '$' in environment variable 'response'
    And 'response' matches the following cases:
      | $.title  | contains  | 2              |
      | $.body   | contains  | This is a test |
      | $.userId | not equal | 2              |


  Scenario: URL parameters are added to the request (i.e /posts?userId=3)
    Given I send requests to '${REST_SERVER_HOST}:3000'
    Given I set url parameters:
      | userId | 3 |
    When I send a 'GET' request to '/posts'
    Then the service response status must be '200'
    And I save element '$.[0]' in environment variable 'response'
    And 'response' matches the following cases:
      | $.userId | equal | 3 |
    Then I clear the url parameters from previous request
    Given I set url parameters:
      | userId | 4 |
    When I send a 'GET' request to '/posts'
    Then the service response status must be '200'
    And I save element '$.[0]' in environment variable 'response'
    And 'response' matches the following cases:
      | $.userId | equal | 4 |


  @ignore @toocomplex
  Scenario: Setting headers using a datatable and verifying the returned headers
    Given I send requests to 'dummy-test.com:80'
    Given I set headers:
      | x-user  | vente_privee_es                                                  |
      | x-token | 93f44fdfe7c186e354fafbf0ff064eec1e2d6e31df6956cbeb7d3a7b5c112dc4 |
    When I send a 'GET' request to '/api/v1/shipment/1' as 'json'
    Then the service response status must be '200'
    And the service response headers match the following cases:
      | Server           | equal           | nginx |
      | Content-Encoding | equal           | gzip  |
      | Connection       | exists          |       |
      | test             | does not exists |       |
      | Cache-Control    | length          | 8     |
      | Cache-Control    | contains        | cache |
    And I save the response header 'Server' in environment variable 'SERVER'
    And I clear headers from previous request
    When I send a 'GET' request to '/api/v1/shipment/1'
    Then the service response status must be '401'