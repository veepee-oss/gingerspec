@rest
Feature: Rest Assured Feature

  This feature was created to test the migration from the old com.ning.async-http-client to
  the new io.rest-assured.rest-assured. The feature files and the steps will remain the same
  and the user will still use the same sintax when creating scenarios, what changes is the
  backend implementation.


  Scenario: Some simple request
    Given I securely send requests to 'jsonplaceholder.typicode.com:443'
    When I send a 'GET' request to '/posts'
    Then the service response status must be '200' and its response must contain the text 'body'
    Then the service response status must be '200' and its response matches the schema in 'schemas/responseSchema.json'
    Then the service response status must be '200' and its response length must be '27520'
    And the service response must contain the text 'body'
    And in less than '10' seconds, checking each '2' seconds, I send a 'GET' request to '/posts' so that the response contains 'body'


  Scenario: A new element is inserted via a POST call
    Given I securely send requests to 'jsonplaceholder.typicode.com:443'
    When I send a 'POST' request to '/posts' based on 'schemas/mytestdata.json' as 'json'
    Then the service response status must be '201'
    And I save element '$.title' in environment variable 'TITLE'
    Then '!{TITLE}' matches 'This is a test'


    @ignore @toocomplex
  Scenario: Setting headers using a datatable
    Given My app is running in 'tms-api-uat20.privalia-test.com:80'
    Given I set headers:
      | x-user  | vente_privee_es                                                  |
      | x-token | 93f44fdfe7c186e354fafbf0ff064eec1e2d6e31df6956cbeb7d3a7b5c112dc4 |
    When I send a 'GET' request to '/api/v1/shipment/1' as 'json'
    Then the service response status must be '200'
    And the service response headers match the following cases:
      | Server            | equal           |  nginx   |
      | Content-Encoding  | equal           |  gzip    |
      | Connection        | exists          |          |
      | test              | does not exists |          |
      | Cache-Control     | length          |  8       |
      | Cache-Control     | contains        |  cache   |
    And I save the response header 'Server' in environment variable 'SERVER'
    And I clear headers from previous request
    When I send a 'GET' request to '/api/v1/shipment/1'
    Then the service response status must be '401'


  Scenario: Data in local file is altered using a datatable before sending
    Given I securely send requests to 'jsonplaceholder.typicode.com:443'
    When I send a 'POST' request to '/posts' based on 'schemas/mytestdata.json' as 'json' with:
      | $.title | UPDATE | This is a test 2 |
    Then the service response status must be '201'
    And I save element '$' in environment variable 'response'
    And 'response' matches the following cases:
      | $.title  | contains  | 2              |
      | $.body   | contains  | This is a test |
      | $.userId | not equal | 2              |