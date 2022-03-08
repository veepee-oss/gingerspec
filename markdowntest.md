# Feature Testing variable replacements 1.0

This feature provides an example on how to use variables in GingerSpec. The default definition of a variable
is ${variableName}. GingerSpec will try to get the value of 'variableName' before executing the step. if GingerSpec
is unable to find the value, the test will fail. You can use variables at any point in your feature file: feature title,
description, scenario description steps, datatables, docstrings and even comments.

There are many ways of creating new variables: you can pass variables via the CLI using -DvariableName=variableValue,
or they can be defined in a previous step. You can also use variables to automatically generate fake data.

1.- Using variables in Scenario names, titles and log messages
2.- Creating local variables
3.- Injecting system variables via CLI
4.- Using default values
5.- Using interpolation
6.- Recursive variable replacements
7.- Using environment-dependent variables
8.- Generating fake data
9.- Resolving mathematical expressions
10.- Resolving variables in files
11.- Miscellaneous and examples


## Rule Using variables in Scenario names, titles, log messages, datatables, docStrings

### Scenario: Replacements scenario title and log comments. 1.0

    Scenario: Replacements scenario title and log comments. 1.0
      #log The value of VERSION in the title should be ${VERSION}
      And I wait '1' seconds

    ### Scenario: Simplest read {} on scenario name
      Given I run 'ls' locally

    ### Scenario: Replacements in datatables
      Given this is a datatable:
        | $.title  | contains  | ${faker:number.number_between '1','10'}    |
        | $.body   | contains  | This is a test: ${envProperties:wait.time} |
        | $.userId | not equal | ${VERSION}                                 |

    ### Scenario: Replacements in a DocString
      Given This is a DocString
        """
            {
              "userId": ${faker:number.number_between '1','10'},
              "title": "This is a test: ${envProperties:wait.time}",
              "body": "${VERSION}"
              "price": "${math:3 * sin(90) - 2 / (5 - 2)}"
            }
         """


## Rule Creating local variables

    ### Scenario: Saving and replacing local variables
      Given I save '2' in variable 'MY_LOCAL_VAR'
      Then '${MY_LOCAL_VAR}' is '2'
      And I wait '${MY_LOCAL_VAR}' seconds

    Scenario Outline: Replacements in an scenario outline
      Given I save '2' in variable 'SO_ENV_VAR'
      And I wait '${SO_ENV_VAR}' seconds
      And I wait '${SO_ENV_VAR}' seconds
      And I wait '<other>' seconds
      And I wait '${envProperties:wait.time}' seconds
      And I wait '${envProperties:wait.time}' seconds

      Examples:
        | other                      |
        | 1                          |
        | ${envProperties:wait.time} |


## Rule Injecting system variables via CLI

    ### Scenario: Passing a variable via CLI using -DVERSION=1.0
      Then '${VERSION}' is '1.0'


## Rule Using default values

    ### Scenario: Using a default value when the variable is not found
      Then '${INVALID:-bb}' matches 'bb'


## Rule Using interpolation (StringSubstitutor library)

    ### Scenario: Using default interpolators (this is just a short sample, there are many more)
      Then '${base64Encoder:HelloWorld!}' matches 'SGVsbG9Xb3JsZCE='
      Then '${const:java.awt.event.KeyEvent.VK_ESCAPE}' matches '27'
      Then '${date:yyyy-MM-dd}' contains '-'
      Then '${properties:src/test/resources/configuration/common.properties::wait.time}' matches '1'

    ### Scenario: Changing variables value to upper or lower case
      Then '${toUpperCase:${VARNAME}}' matches 'FOO'
      Then '${toLowerCase:${VARNAME}}' matches 'foo'


## Rule Recursive variable replacements

    ### Scenario: use a variable inside an interpolator
      Given I save 'Foo' in variable 'VARNAME'
      Then '${toUpperCase:${VARNAME}}' matches 'FOO'
      Then '${toLowerCase:${VARNAME}}' matches 'foo'


## Rule Using environment-dependent variables

    ### Scenario: Using variables from one properties file or another
      Then '${envProperties:wait.time}' matches '1'
      Given I save 'pre' in variable 'env'
      Then '${envProperties:wait.time}' matches '2'


## Rule Generating fake data

    ### Scenario: Using the faker library
      Given I save '${faker:number.number_between '1','10'}' in variable 'RANDOM_NUMBER'
      Then '${RANDOM_NUMBER}' is higher than '0'
      And '${RANDOM_NUMBER}' is lower than '11'
      Given I save '${faker:Internet.emailAddress}' in variable 'RANDOM_EMAIL'
      Then '${RANDOM_EMAIL}' contains '@'
      Given I save '${faker:Name.first_name}' in variable 'RANDOM_NAME'


## Rule Resolving mathematical expressions

    ### Scenario: Doing a simple calculation
      Given I save '2' in variable 'X'
      Given I save '2' in variable 'Y'
      Given '${math:${X} + ${Y}}' is '4.0'

    ### Scenario: Doing an advanced calculation
      Given '${math:3 * sin(90) - 2 / (5 - 2)}' is '2.015323324135007'


## Rule Miscellaneous and examples

    Scenario Outline: With scenarios outlines in examples table
      Given I save '<file>' in variable 'VAR'
      Then I run '[ "${VAR}" = "<content>" ]' locally

      Examples:
        | content     | file                                                  |
        | {}          | ${file:UTF-8:src/test/resources/schemas/simple0.json} |
        | {"a": true} | ${file:UTF-8:src/test/resources/schemas/simple1.json} |

    Scenario Outline: With scenarios outlines and datatables
      Given I create file 'testSOATtag.json' based on 'schemas/simple1.json' as 'json' with:
        | $.a | REPLACE | <file> | object |
      Given I save '${file:UTF-8:target/test-classes/testSOATtag.json}' in variable 'VAR'
      Then I run '[ "${VAR}" = "<content>" ]' locally

      Examples:
        | content  | file                                                |
        | {"a":{}} | ${file:UTF-8:src/test/resources/schemas/empty.json} |

    Scenario Outline: With scenarios outlines and datatables2
      Given I run 'ls' locally
      Given I create file 'testSOATtag.json' based on 'schemas/simple1.json' as 'json' with:
        | $.a     | REPLACE | ${file:UTF-8:src/test/resources/schemas/<file>.json} | object |
        | b       | ADD     | ${WAIT}                                              | N/A    |
        | ${WAIT} | ADD     | ${file:UTF-8:src/test/resources/schemas/<file>.json} | object |
      Given I save '${file:UTF-8:target/test-classes/testSOATtag.json}' in variable 'VAR'
      Then I run '[ "${VAR}" = "<content>" ]' locally

      Examples:
        | content                 | file    |
        | {"a":{},"1":{},"b":"1"} | empty   |
        | {"a":{},"1":{},"b":"1"} | simple0 |

    ### Scenario: Reading a file and saving it in a variable
      Given I save '${file:UTF-8:src/test/resources/schemas/empty.json}' in variable 'VAR'
      Then I run '[ "${VAR}" = "{}" ]' locally
      #log The same operation can be done with $variables
      Given I save '${file:UTF-8:src/test/resources/schemas/empty.json}' in variable 'VAR'
      Then I run '[ "${VAR}" = "{}" ]' locally