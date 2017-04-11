@web
Feature: Selenium run test

  Scenario: Dummy scenario
    Given My app is running in 'www.google.com:80'
    When I browse to '/'
    When '1' elements exists with 'id:lst-ib'
    And I click on the element on index '0'
    Then I type 'stratio' on the element on index '0'
    Then I send 'ENTER' on the element on index '0'
    And I wait '1' seconds

  @include(feature:scenarioIncluded.feature,scenario:Dummy_scenario)
  Scenario: Testing include
    Given I wait '1' seconds

  Scenario: Dummy scenario with HTTPS
    Given My app is running in 'qa.stratio.com'
    When I securely browse to '/'

  Scenario: Checking element steps
    Given My app is running in 'jenkins.stratio.com'
    When I browse to '/'
    Then in less than '20' seconds, checking each '2' seconds, '1' elements exists with 'id:side-panel'
    When '1' elements exists with 'xpath://*[@id="header"]/div[2]/a/b'
    And I click on the element on index '0'
    When '1' elements exists with 'id:main-panel'
    Then the element on index '0' has 'id' as 'main-panel'
    And the element on index '0' has 'User:' as text
    Then '1' elements exists with 'id:j_username'
    And I clear the content on text input at index '0'
    When '1' elements exists with 'id:remember_me'
    And the element on index '0' IS NOT selected
    Then I click on the element on index '0'