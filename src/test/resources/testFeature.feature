@ignore
# text
Feature: test feature

  First line
  Second line

  #comment
  @tag
  Rule: Specifying Request Data

  @rest
  Scenario: Invoking HTTP resources (GET, POST, DELETE, PATCH, UPDATE)
    Given I send requests to '${REST_SERVER_HOST}:3000'
    When I send a 'GET' request to '/posts'

  @rest
  Scenario: Adding URL parameters (i.e /posts?userId=3)
    Given I send requests to '${REST_SERVER_HOST}:3000'
    Given I set url parameters:
      | userId | 3 |
    When I send a 'GET' request to '/posts'

  @ignore
  Scenario: Adding cookies
    Given I send requests to '${REST_SERVER_HOST}:3000'
    Given I set cookies:
      | cookieName | value1 |
    When I send a 'GET' request to '/posts'

  @ignore
  Scenario Outline: With scenarios outlines in examples table
    Given I save '<file>' in variable 'VAR'
    Then I run '[ "${VAR}" = "<content>" ]' locally

    Examples:
      | content     | file                                                  |
      | {}          | ${file:UTF-8:src/test/resources/schemas/simple0.json} |
      | {"a": true} | ${file:UTF-8:src/test/resources/schemas/simple1.json} |

  #comment
  @tag
  Rule: Verifying Response Data

  @rest
  @ignore
  Scenario: Verify response status code
    Given I send requests to '${REST_SERVER_HOST}:3000'
    When I send a 'GET' request to '/posts'
    Then the service response status must be '200'

  #comment
  #comment2
  @rest
  Scenario: Verify the response body contains specific text
    Given I send requests to '${REST_SERVER_HOST}:3000'
    When I send a 'GET' request to '/posts'
    And the service response must contain the text 'body'