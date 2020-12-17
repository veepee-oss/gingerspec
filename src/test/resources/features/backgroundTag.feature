Feature: Testing background tag

  This feature provides an example on how to use the @BACKGROUND tag. You can use this tag to enclose
  an step or group of steps and execute them only if the provided global variable is present.
  You can use @BACKGROUND tag within regular scenarios or, as in this example, within a background. You can
  even use this tag within datatables.

  Background:
  @BACKGROUND(WAIT)
    Given I run '[ "SHOULD_RUN_WAIT" = "SHOULD_RUN_WAIT" ]' locally
  @/BACKGROUND
  @BACKGROUND(WAIT_NO)
    Given I run '[ "SHOULD_RUN" = "FAIL_RUN" ]' locally
    Given I run 'exit 1' locally
  @/BACKGROUND
    Given I run '[ "SHOULD_RUN" = "SHOULD_RUN" ]' locally

  Scenario: default scenario for background1
    Given I run '[ "SHOULD_RUN" = "SHOULD_RUN" ]' locally

