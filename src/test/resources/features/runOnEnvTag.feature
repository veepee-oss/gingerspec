Feature: Testing the @runOnEnv and @skipOnEnv tags

  This feature shows examples on how to use the @runOnEnv and @skipOnEnv tags. These tags allows the
  conditional execution of scenarios. You can think of @runOnEnv as IF and @skipOnEnv as an IF NOT
  statement. With @runOnEnv the scenario will only be executed if ALL the params are defined. And
  with @skipOnEnv the scenario will be omitted if ANY of params are defined.

  @runOnEnv(WAIT)
  Scenario: RunOnEnv with param defined.
    Given I run 'sleep 1' locally

  @runOnEnv(NO_WAIT)
  Scenario: RunOnEnv with param NOT defined.
    Given I run 'sleep 1' locally

  @runOnEnv(WAIT,NO_WAIT)
  Scenario: RunOnEnv with more than one param. Ones defined and others not defined.
    Given I run 'sleep 3' locally

  @skipOnEnv(NO_WAIT)
  Scenario: SkipOnEnv with param NOT defined.
    Given I run 'sleep 1' locally

  @skipOnEnv(WAIT)
  Scenario: SkipOnEnv with param defined.
    Given I run 'sleep 1' locally

  @skipOnEnv(WAIT,NO_WAIT)
  Scenario: AND skipping option. SkipOnEnv with more than one param.
    Given I run 'sleep 1' locally
    Given I run 'sleep 1' locally

  @skipOnEnv(WAIT)
  @skipOnEnv(NO_WAIT)
  Scenario: OR skipping option. SkipOnEnv with more than one param.
    Given I run 'sleep 1' locally

  @runOnEnv(WAIT)
  @skipOnEnv(NO_WAIT)
  Scenario: RunOnEnv with more than one param. Ones defined and others not defined.
    Given I run 'sleep 1' locally
