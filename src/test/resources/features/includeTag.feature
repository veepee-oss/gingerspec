Feature: Testing the @include tag

  This Feature provides examples on how to use the @include tag. When this tag is used on an scenario,
  GingerSpec will try to execute the provided scenario in the tag first. Be careful when using the @include
  tag, it could be useful for code reusability but could reduce readability. Providing a non existent
  feature file will make your test suite to fail

  Scenario: included_scenario
    Given I run '[ "SHOULD_RUN" = "SHOULD_RUN" ]' locally

  @include(feature:includeTag.feature,scenario:included_scenario)
  Scenario: Including scenario from same feature file
    Given I run '[ "SHOULD_RUN" = "SHOULD_RUN" ]' locally

  @web
  @include(feature:scenarioIncluded.feature,scenario:Dummy_scenario)
  Scenario: Testing include from another feature file
    When '7' elements exists with 'class:legend_txt'
    And I wait '2' seconds
