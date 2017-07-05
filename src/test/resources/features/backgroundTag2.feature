Feature: Background aspect tests 2

  Background:
    Given I run '[ "SHOULD_RUN" = "SHOULD_RUN" ]' locally
  @background(WAIT_NO)
    Given I run '[ "SHOULD_RUN" = "FAIL_RUN" ]' locally
    Given I run '[ "SHOULD_RUN" = "FAIL_RUN" ]' locally
    Given I run '[ "SHOULD_RUN" = "FAIL_RUN" ]' locally
  @/background
    Given I run '[ "SHOULD_RUN" = "SHOULD_RUN" ]' locally

  Scenario: default scenario for background2
    Given I run '[ "SHOULD_RUN" = "SHOULD_RUN" ]' locally
