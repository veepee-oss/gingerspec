@web
Feature: Selenium run test

  @ignore @toocomplex
  Scenario: Test a file picker
    Given My app is running in '${DEMO_SITE_HOST}'
    When I browse to '/registration'
    When '1' elements exists with 'id:profile_pic_10'
    Then I assign the file in 'schemas/empty.json' to the element on index '0'
    And I wait '3' seconds

  Scenario: Test send keys function
    Given My app is running in '${DEMO_SITE_HOST}'
    When I browse to '/registration'
    When '1' elements exists with 'id:name_3_firstname'
    Then I type 'testUser' on the element on index '0'
    Then I send 'ENTER' on the element on index '0'
    And I wait '1' seconds
    When '7' elements exists with 'class:legend_txt'
    And I wait '2' seconds

  @include(feature:scenarioIncluded.feature,scenario:Dummy_scenario)
  Scenario: Testing include
    When '7' elements exists with 'class:legend_txt'
    And I wait '2' seconds

    @ignore @toocomplex
  Scenario: Dummy scenario with HTTPS
    Given My app is running in 'es.privalia-test.com'
    When I securely browse to '/'

  Scenario: Checking element steps
    Given My app is running in '${DEMO_SITE_HOST}'
    When I browse to '/registration'
    Then in less than '20' seconds, checking each '2' seconds, '1' elements exists with 'id:name_3_firstname'
    When '1' elements exists with 'xpath://*[@id="name_3_lastname"]'
    And I click on the element on index '0'
    When '1' elements exists with 'id:phone_9'
    Then the element on index '0' has 'id' as 'phone_9'
    And I type '555-555' on the element on index '0'
    And I clear the content on text input at index '0'
    When '1' elements exists with 'xpath://*[@id="pie_register"]/li[6]/div/label'
    And the element on index '0' has 'Phone Number' as text
    When '3' elements exists with 'name:radio_4[]'
    And the element on index '0' IS NOT selected
    Then I click on the element on index '0'
    And the element on index '0' IS selected