Feature: Every scenario should be ignored, not failing the tests

  @ignore @unimplemented
  Scenario: Ignored scenario (unimplemented)
    Given I run '[ "THIS SHOULDNT HAVE BEEN RUN" = "@{JSON.unexistant.json}" ]' locally

  @ignore @manual
  Scenario: Ignored scenario (manual)
    Given I run '[ "THIS SHOULDNT HAVE BEEN RUN" = "${UNEXISTANT_VAR}" ]' locally

  @ignore @toocomplex
  Scenario: Ignored scenario (too complex)
    Given I run '[ "THIS SHOULDNT HAVE BEEN RUN" = "!UNEXISTANT_VAR" ]' locally

  @ignore @tillfixed(QATM-34)
  Scenario: Ignored scenario (till ticket fixed)
    Given I run '[ "THIS SHOULDNT HAVE BEEN RUN" = "" ]' locally

  Scenario: included_scenario
    Given I run '[ "SHOULD_RUN" = "SHOULD_RUN" ]' locally

  @include(feature:ignored.feature,scenario:included_scenario)
  Scenario: Including scenario
    Given I run '[ "SHOULD_RUN" = "SHOULD_RUN" ]' locally

#  @include(feature:non_existant.feature,scenario:non_existant_scenario)
  Scenario: Ignoring include tag so non scenanrio is included.
    Given I run '[ "SHOULD_RUN" = "SHOULD_RUN" ]' locally