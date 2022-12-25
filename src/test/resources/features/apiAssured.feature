Feature: Steps for testing APIs

  This feature provides examples on how to use the steps for testing REST or GRAPHQL APIs. All steps make use of the
  library rest-assured in the background


  Rule: Set up initial base URI for future requests

    Scenario: Setting up base URI for future requests using http
      Given I send requests to '${REST_SERVER_HOST}:3000'

    Scenario: Setting up base URI for future requests using https
      Given I securely send requests to '${REST_SERVER_HOST}:3000'


  Rule: Set up initial base URI for future graphql requests

    Scenario: Setting up base URI for future requests without graphql schema
      Given I send requests to '${GRAPHQL_SERVER_HOST}:3001'

    Scenario: Setting up base URI for future requests with graphql schema
      Given I send requests to '${GRAPHQL_SERVER_HOST}:3001'
      Given I getting the graphql schema from 'schemas/schema.graphql'


  Rule: Set up initial swagger spec for future requests

    @ignore
    Scenario: Setting up swagger spec oas2 for future requests from URI
      Given I getting the swagger spec from 'https://petstore.swagger.io/v2/swagger.json'

    @ignore
    Scenario: Setting up swagger spec oas2 for future requests from URI with server
      Given I getting the swagger spec from 'https://petstore.swagger.io/v2/swagger.json' and choose server with index 0

    Scenario: Setting up swagger spec oas2 for future requests from file
      Given I getting the swagger spec from 'schemas/oas2.yaml'

    Scenario: Setting up swagger spec oas2 for future requests from file with server
      Given I getting the swagger spec from 'schemas/oas2.yaml' and choose server with index 0

    Scenario: Setting up swagger spec oas3 for future requests from file
      Given I getting the swagger spec from 'schemas/oas3.yaml'

    Scenario: Setting up swagger spec oas3 for future requests from file with server
      Given I getting the swagger spec from 'schemas/oas3.yaml' and choose server with index 0


  Rule: Specifying Request Data

    Scenario: Invoking HTTP resources (GET, POST, DELETE, PATCH, UPDATE)
      Given I send requests to '${REST_SERVER_HOST}:3000'
      When I send a 'GET' request to '/posts'

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
    Scenario: Adding headers
      Given I send requests to '${REST_SERVER_HOST}:3000'
      Given I set headers:
        | headerName | value1 |
      When I send a 'GET' request to '/posts'

    @ignore
    Scenario: Adding path parameters
      Given I send requests to '${REST_SERVER_HOST}:3000'
      Given I set url path parameters:
        | id | 1 |
      When I send a 'GET' request to '/posts/{id}'


  Rule: Swagger Spec Specifying Request Data

    Scenario: Invoking requests to swagger operation from spec oas3
      Given I getting the swagger spec from 'schemas/oas3.yaml'
      Given I set url path parameters:
        | id | 1 |
      When I send request to swagger by operation id 'find pet by id'

    Scenario: Invoking requests to swagger operation from spec oas2
      Given I getting the swagger spec from 'schemas/oas2.yaml'
      Given I set url path parameters:
        | id | 1 |
      When I send request to swagger by operation id 'findPetById'

    Scenario: Adding request body from a file for spec oas3
      Given I getting the swagger spec from 'schemas/oas3.yaml'
      When I send request to swagger by operation id 'addPet' based on 'schemas/swagger.testdata.json' as 'json'

    Scenario: Adding request body from a file for spec oas2
      Given I getting the swagger spec from 'schemas/oas2.yaml'
      When I send request to swagger by operation id 'addPet' based on 'schemas/swagger.testdata.json' as 'json'

    Scenario: Adding request body from a file but modifying elements of the json before sending for spec oas3
      Given I getting the swagger spec from 'schemas/oas3.yaml'
      When I send request to swagger by operation id 'addPet' based on 'schemas/swagger.testdata.json' as 'json' with:
        | $.tags | UPDATE | cat |

    Scenario: Adding request body from a file but modifying elements of the json before sending for oas2
      Given I getting the swagger spec from 'schemas/oas2.yaml'
      When I send request to swagger by operation id 'addPet' based on 'schemas/swagger.testdata.json' as 'json' with:
        | $.tags | UPDATE | cat |

    Scenario: Adding request body directly in the gherkin step for spec oas3
      Given I getting the swagger spec from 'schemas/oas3.yaml'
      When I send request to swagger by operation id 'addPet' with body
           """
              {
                "name": "doggie",
                "tags": "dog"
              }
           """

    Scenario: Adding request body directly in the gherkin step for oas2
      Given I getting the swagger spec from 'schemas/oas2.yaml'
      When I send request to swagger by operation id 'addPet' with body
           """
              {
                "name": "doggie",
                "tags": "dog"
              }
           """


  Rule: REST APIs Specifying Request Data

    Scenario: Adding request body from a file
      Given I send requests to '${REST_SERVER_HOST}:3000'
      When I send a 'POST' request to '/posts' based on 'schemas/mytestdata.json' as 'json'

    Scenario: Adding request body from a file but modifying elements of the json before sending
      Given I send requests to '${REST_SERVER_HOST}:3000'
      When I send a 'POST' request to '/posts' based on 'schemas/mytestdata.json' as 'json' with:
        | $.title | UPDATE | This is a test 2 |

    Scenario: Adding request body directly in the gherkin step
      Given I send requests to '${REST_SERVER_HOST}:3000'
      When I send a 'POST' request to '/posts' with body
           """
              {
                "userId": 1,
                "title": "This is a test",
                "body": "This is a test"
              }
           """

    Scenario: Adding request body directly in the gherkin step as json
      Given I send requests to '${REST_SERVER_HOST}:3000'
      When I send a 'POST' request to '/posts' as 'json' with body
           """
              {
                "userId": 1,
                "title": "This is a test",
                "body": "This is a test"
              }
          """

    @ignore
    Scenario: Sending a file
      Given I send requests to '${REST_SERVER_HOST}:3000'
      And I set headers:
        | Content-Type | multipart/form-data |
      And I add the file in 'schemas/mytestdata.json' to the request
      When I send a 'POST' request to '/posts'
      Then the service response status must be '201'


  Rule: GRAPHQL APIs Specifying Request Data

    Scenario: Adding graphql request body from a file
      Given I send requests to '${GRAPHQL_SERVER_HOST}:3001'
      Given I getting the graphql schema from 'schemas/schema.graphql'
      When I send a 'POST' request to '/' based on 'schemas/mytestdata.graphql' as 'graphql'

    Scenario: Adding graphql request body from a file with variables
      Given I send requests to '${GRAPHQL_SERVER_HOST}:3001'
      Given I getting the graphql schema from 'schemas/schema.graphql'
      Given I set graphql variables:
        | perPage | 10 |
      When I send a 'POST' request to '/' based on 'schemas/mytestdatawithvars.graphql' as 'graphql'

    Scenario: Adding graphql request body from a file with variables from file
      Given I send requests to '${GRAPHQL_SERVER_HOST}:3001'
      Given I getting the graphql schema from 'schemas/schema.graphql'
      Given I set graphql variables based on 'schemas/graphql.variables.json'
      When I send a 'POST' request to '/' based on 'schemas/mytestdatawithvars.graphql' as 'graphql'

    Scenario: Adding request body from a file but modifying elements of the graphql before sending
      Given I send requests to '${GRAPHQL_SERVER_HOST}:3001'
      Given I getting the graphql schema from 'schemas/schema.graphql'
      When I send a 'POST' request to '/' based on 'schemas/mytestdata.graphql' as 'graphql' with:
        | id | UPDATE | name |

    Scenario: Adding request body from a file and variables but modifying elements of the graphql before sending
      Given I send requests to '${GRAPHQL_SERVER_HOST}:3001'
      Given I getting the graphql schema from 'schemas/schema.graphql'
      Given I set graphql variables:
        | perPage | 10 |
      When I send a 'POST' request to '/' based on 'schemas/mytestdatawithvars.graphql' as 'graphql' with:
        | id | UPDATE | name |

    Scenario: Adding graphql request body directly in the gherkin step
      Given I send requests to '${GRAPHQL_SERVER_HOST}:3001'
      Given I getting the graphql schema from 'schemas/schema.graphql'
      When I send a 'POST' request to '/' as 'graphql' with body
           """
              {
                  allUsers(perPage: 10) {
                      id
                      name
                  }
              }
          """

    Scenario: Adding graphql request body and variables directly in the gherkin step
      Given I send requests to '${GRAPHQL_SERVER_HOST}:3001'
      Given I getting the graphql schema from 'schemas/schema.graphql'
      Given I set graphql variables:
        | perPage | 10 |
      When I send a 'POST' request to '/' as 'graphql' with body
           """
              query ($perPage: Int = 1) {
                  allUsers(perPage: $perPage) {
                      id
                      name
                  }
              }
          """

  Rule: Verifying Response Data

    Scenario: Verify response status code
      Given I send requests to '${REST_SERVER_HOST}:3000'
      When I send a 'GET' request to '/posts'
      Then the service response status must be '200'

    Scenario: Verify the response body contains specific text
      Given I send requests to '${REST_SERVER_HOST}:3000'
      When I send a 'GET' request to '/posts'
      And the service response must contain the text 'body'

    Scenario: Verify the response body is the specified length
      Given I send requests to '${REST_SERVER_HOST}:3000'
      When I send a 'GET' request to '/posts/1'
      When the service response length must be '292'

    Scenario: Validating the response body matches a predefined json schema
      Given I send requests to '${REST_SERVER_HOST}:3000'
      When I send a 'GET' request to '/posts'
      And the service response matches the schema in 'schemas/responseSchema.json'

    Scenario: Saving an element from the response body in a variable for future use using jsonpath
      Given I send requests to '${REST_SERVER_HOST}:3000'
      When I send a 'GET' request to '/posts'
      Then the service response status must be '200'
      And I save element '$.[0].id' in environment variable 'ID'
      Then '${ID}' matches '1'

    Scenario: Saving the whole response body and evaluating several elements at the same time
      Given I send requests to '${REST_SERVER_HOST}:3000'
      When I send a 'GET' request to '/comments/1'
      Then the service response status must be '200'
      And I save element '$' in environment variable 'response'
      And 'response' matches the following cases:
        | $.postId    | equal            | 1      |
        | $.id        | not equal        | 2      |
        | $.email     | contains         | Eliseo |
        | $.email     | does not contain | foobar |
        | $.body      | exists           |        |
        | $.fakefield | does not exists  |        |

    Scenario: Saving the value of a response header in a variable for future use
      Given I send requests to '${REST_SERVER_HOST}:3000'
      When I send a 'GET' request to '/posts'
      And I save the response header 'Content-Type' in environment variable 'CONTENT-TYPE'
      Then '${CONTENT-TYPE}' matches 'application/json; charset=utf-8'

    Scenario: Verifying several headers using a datatable
      Given I send requests to '${REST_SERVER_HOST}:3000'
      When I send a 'GET' request to '/posts'
      And the service response headers match the following cases:
        | Content-Type | equal  | application/json; charset=utf-8 |
        | Expires      | equal  | -1                              |
        | Expires      | exists |                                 |

    @ignore
    Scenario: Saving the value of a response cookie in a variable for future use
      Given I send requests to '${REST_SERVER_HOST}:3000'
      When I send a 'GET' request to '/posts'
      And I save the response cookie 'cookieName' in environment variable 'COOKIE'
      Then '${COOKIE}' matches 'value'

    @ignore
    Scenario: Verifying several cookies using a datatable
      Given I send requests to '${REST_SERVER_HOST}:3000'
      When I send a 'GET' request to '/posts'
      And the service response cookies match the following cases:
        | cookieName1 | equal  | value1 |
        | cookieName2 | equal  | value2 |
        | cookieName3 | exists |        |

    Scenario: Measuring Response Time
      Given I send requests to '${REST_SERVER_HOST}:3000'
      When I send a 'GET' request to '/posts'
      And the service response time is lower than '1000' milliseconds


  Rule: Authentication

    Scenario: Generating a request using basic authentication
      Given I send requests to '${REST_SERVER_HOST}:3000'
      When I send a 'GET' request to '/posts' with user and password 'user:password'

    Scenario: Generating a request using basic authentication (POST example)
      Given I send requests to '${REST_SERVER_HOST}:3000'
      When I send a 'POST' request to '/posts' with user and password 'user:password' based on 'schemas/mytestdata.json' as 'json'


  Rule: Proxy configuration

    @ignore
    Scenario: Setting a proxy
      Given I send requests to '${REST_SERVER_HOST}:3000'
      And I set the proxy to 'http://localhost:80'

    @ignore
    Scenario: Setting a proxy with credentials
      Given I send requests to '${REST_SERVER_HOST}:3000'
      And I set the proxy to 'http://localhost:80' with username 'myusername' and password 'mypassword'


  Rule: Miscellaneous and examples

    Scenario: Operations that can be done on a json response (using a sample json file as seed json)
      And I save '${file:UTF-8:src/test/resources/schemas/sampleJsonResponse.json}' in variable 'SAMPLE_JSON'
      And 'SAMPLE_JSON' matches the following cases:
        | $.firstName             | equal            | John    |
        | $.firstName             | not equal        | Smith   |
        | $.lastName              | contains         | d       |
        | $.lastName              | does not contain | foo     |
        | $.age                   | length           | 2       |
        | $.phoneNumbers[0].type  | equal            | iPhone  |
        | $.phoneNumbers[0].local | equal            | false   |
        | $.address               | exists           |         |
        | $.fakefield             | does not exists  |         |
        | $.phoneNumbers          | size             | 2       |
        | $.hobbies               | contains         | netflix |