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

#  @ignore @tillfixed(QA-357)
#  Scenario: Ignored scenario (till ticket fixed)
#    Given I run '[ "THIS SHOULDNT HAVE BEEN RUN" = "" ]' locally
