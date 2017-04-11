@web
Feature: This feature is used to be included as part of includetagAspect testing

  Scenario: Dummy_scenario
    Given My app is running in 'jenkins.stratio.com'
    When I browse to '/'
    Then I take a snapshot
    Then I maximize the browser
    When I switch to iframe with 'id:_yuiResizeMonitor'
    Then I wait '1' seconds
    And I switch to a parent frame
    When '1' elements exists with 'id:_yuiResizeMonitor'
    Then I switch to the iframe on index '0'