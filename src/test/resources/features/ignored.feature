Feature: Testing the @ignore tag

  This feature shows an example on how to use the @ignore tag. You can place @ignore on top of the scenario
  to just ignore that particular scenario, or at feature level, to ignore all scenarios in the feature. You just can
  also use the tag @ignore + <ignore reason> to provide more context.

  @ignore
  Scenario: Ignored scenario (no reason specified)
    Given I run '[ "THIS SHOULDNT HAVE BEEN RUN" = "${file:UTF-8:src/test/resources/schemas/unexistant.json}" ]' locally
    Given I run 'exit 1' locally

  @ignore @unimplemented
  Scenario: Ignored scenario (unimplemented)
    Given I run '[ "THIS SHOULDNT HAVE BEEN RUN" = "${file:UTF-8:src/test/resources/schemas/unexistant.json}" ]' locally
    Given I run 'exit 1' locally

  @ignore @manual
  Scenario: Ignored scenario (manual)
    Given I run '[ "THIS SHOULDNT HAVE BEEN RUN" = "${UNEXISTANT_VAR}" ]' locally
    Given I run 'exit 1' locally

  @ignore @toocomplex
  Scenario: Ignored scenario (too complex)
    Given I run '[ "THIS SHOULDNT HAVE BEEN RUN" = "!UNEXISTANT_VAR" ]' locally
    Given I run 'exit 1' locally

  @ignore @tillfixed(QATM-34)
  Scenario: Ignored scenario (till ticket fixed)
    Given I run '[ "THIS SHOULDNT HAVE BEEN RUN" = "" ]' locally
    Given I run 'exit 1' locally