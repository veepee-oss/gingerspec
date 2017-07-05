[![Coverage Status](https://coveralls.io/repos/github/Stratio/bdt/badge.svg?branch=master)](https://coveralls.io/github/Stratio/bdt?branch=master)
[![Maven central](https://maven-badges.herokuapp.com/maven-central/com.stratio.qa/bdt/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.stratio.qa/bdt)

Stratio bdt
===========

Stratio Acceptance Test library

 * Cucumber for test definition 
 * TestNG for execution
 * AsyncHTTP Client
 * Selenium 
 * JSONPath
 * AssertJ

Testing runtime to rule over Stratio's acceptance tests


[Javadoc always live](http://stratiodocs.s3.amazonaws.com/bdt/0.6.0-SNAPSHOT/index.html)



## EXECUTION

These tests are suposed to be executed as follows:

` mvn verify [-D\<ENV_VAR>=\<VALUE>] [-Dit.test=\<TEST_TO_EXECUTE>|-Dgroups=\<GROUP_TO_EXECUTE>] `

Examples:

_**single class execution**_

` mvn verify -DSECS=AGENT_LIST=1,2 -Dit.test=com.stratio.qa.ATests.LoopTagAspectIT `

_**group execution**_

` mvn verify -DSECS=5 -Dgroups=hol `

## ASPECTS

As part of BDT implementation, there are a couple of AspectJ aspects which may be useful for your scenarios:

- **RunOnTagAspect**:

` @runOnEnv(METRIC_HOST) `

` @skipOnEnv(METRIC_HOST) `

An AspectJ aspect could handle cucumber tags so that the annotated scenario will be overseen if no environment variable METRIC_HOST exists and has value (so that no traces of it execution show up)

_Example @runOnEnv:_

```
    @runOnEnv(SECS)
	Scenario: Dummy scenario
      And I wait '${SECS}' seconds
```

This scenario will ONLY be executed if environment vairable SECS is defined.


_Example @skipOnEnv:_

```
    @skipOnEnv(SECS_2)
	Scenario: Dummy scenario
      And I wait '${SECS}' seconds
```

This scenario will be omitted if environment vairable SECS_2 is defined.

- **IgnoreTagAspect**

An AspectJ aspect that allows the skipping of an scenario or a feature. To do so a tag must be used before the scenario or the feature. Additionally an ignored reason can be set.

```
 @ignore @manual
 @ignore @unimplemented
 @ignore @toocomplex
 @ignore @tillfixed(DCS-XXX)
```

This last ignored reason is associated to a ticket in Jira. After executing the test class the ticket link is shown as execution result.

- **IncludeTagAspect**

An AspectJ aspect that includes an scenario before the taged one. It manages parameters as well.

` @include(feature:<feature>,scenario:<scenario>)`

` @include(feature:<feature>,scenario:<scenario>,params:<params>)`


_Examples:_

```
    @include(feature:sample.feature,scenario:Not so dummy scenario)
    Scenario: Dummy scenario
          And I wait '${SECS}' seconds
```

```
    @include(feature:sample.feature,scenario:Not so dummy scenario,params:param1=1)
    Scenario: Dummy scenario
          And I wait '${SECS}' seconds
```

- **LoopTagAspect**

An AspectJ aspect that allows looping over scenarios. Using this tag before an scenario will convert this scenario into an scenario outline, changing parameter defined "NAME" for every element in the environment variable list received.

` @loop(LIST_PARAM,NAME)`

Being LIST_PARAM: `-DLIST_PARAM=elem1,elem2,elem3`

_Examples:_

```
  @loop(AGENT_LIST,VAR_NAME)
  Scenario: write <VAR_NAME> a file the final result of the scenario.
    Given I run 'echo <VAR_NAME> >> testOutput.txt' locally
```

```
  @loop(AGENT_LIST,VAR_NAME)
  Scenario: With scenarios outlines and datatables
    Given I create file 'testSOATtag<VAR_NAME.id>.json' based on 'schemas/simple<VAR_NAME>.json' as 'json' with:
      | $.a | REPLACE | @{JSON.schemas/empty.json}     | object   |
    Given I save '@{JSON.testSOATtag<VAR_NAME.id>.json}' in variable 'VAR'
    Then I run '[ "!{VAR}" = "{"a":{}}" ]' locally
```

`-DAGENT_LIST=1,2`

More examples can be found in [Loop feature](src/test/resources/features/loopTag.feature)


- **Background Tag**

An AspectJ aspect included in loopTagAspect that allows conditional backgrounds. Its used inside the Background label as can be seen in the examples:

```
@background(VAR)        // Beginning of conditional block of steps
   Given X
   When  Y
   Then  Z
@/background            // End of block
  ```

Being VAR: `-DVAR=value`

_Examples:_

```
  Background:
    Given I run '[ "SHOULD_RUN" = "SHOULD_RUN" ]' locally
  @background(WAIT_NO)
    Given I run '[ "SHOULD_RUN" = "FAIL_RUN" ]' locally
    Given I run '[ "SHOULD_RUN" = "FAIL_RUN" ]' locally
    Given I run '[ "SHOULD_RUN" = "FAIL_RUN" ]' locally
  @/background
    Given I run '[ "SHOULD_RUN" = "SHOULD_RUN" ]' locally
```

If the test above its executed *WITH* `-DWAIT_NO=value` then the background will be:


```
   Background:
     Given I run '[ "SHOULD_RUN" = "SHOULD_RUN" ]' locally
     Given I run '[ "SHOULD_RUN" = "FAIL_RUN" ]' locally
     Given I run '[ "SHOULD_RUN" = "FAIL_RUN" ]' locally
     Given I run '[ "SHOULD_RUN" = "FAIL_RUN" ]' locally
     Given I run '[ "SHOULD_RUN" = "SHOULD_RUN" ]' locally
```

On the other hand, if it is executed *WITHOUT* the environment variable, the background will be:

```
   Background:
     Given I run '[ "SHOULD_RUN" = "SHOULD_RUN" ]' locally
     Given I run '[ "SHOULD_RUN" = "SHOULD_RUN" ]' locally
```


In conclusion, if environment variable is defined the code below the tag would be included as part of the background, if not, it will be omitted.

More examples can be found in [Background feature](src/test/resources/features/backgroundTag1.feature)