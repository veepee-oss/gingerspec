Feature: Backgorund aspect tests 3

  Background:
  @background(WAIT_NO)
    Given I run '[ "SHOULD_RUN" = "FAIL_RUN" ]' locally
    Given I run '[ "SHOULD_RUN" = "FAIL_RUN" ]' locally
  @/background


  Scenario: default scenario for background3
    Given I run '[ "SHOULD_RUN" = "SHOULD_RUN" ]' locally
