@web
Feature: This feature is used to be included as part of includetagAspect testing

  Scenario: Dummy_scenario
    Given My app is running in '${DEMO_SITE_HOST}'
    When I browse to '/registration'
    When '1' elements exists with 'id:name_3_firstname'
    Then I type 'testUser' on the element on index '0'
    Then I maximize the browser
    Then I send 'ENTER' on the element on index '0'
