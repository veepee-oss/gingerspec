Feature: Background aspect tests

  Background:
  @BACKGROUND(WAIT)
    Given I run '[ "SHOULD_RUN_WAIT" = "SHOULD_RUN_WAIT" ]' locally
  @/BACKGROUND
  @BACKGROUND(WAIT_NO)
    Given I run '[ "SHOULD_RUN" = "FAIL_RUN" ]' locally
  @/BACKGROUND
    Given I run '[ "SHOULD_RUN" = "SHOULD_RUN" ]' locally
    ∫
  Scenario: default scenario for background1
    Given I run '[ "SHOULD_RUN" = "SHOULD_RUN" ]' locally

