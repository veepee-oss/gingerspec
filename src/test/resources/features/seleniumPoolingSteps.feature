@web
Feature: Selenium wait with pooling

  Scenario: Verify that in Demo->Tbas there are 3 tabs, and in registration 3 radio buttons and 3 checkboxes

    Given My app is running in '${DEMO_SITE_HOST}'
    And I browse to '/index.html@p=82.html'
    Then I check every '1' seconds for at least '10' seconds until '3' elements exists with 'xpath://*[contains(@class, 'ui-tabs-anchor')]' and is 'clickable'
    And I wait '1' seconds
    When I browse to '/registration'
    Then I check every '1' seconds for at least '10' seconds until '3' elements exists with 'name:radio_4[]' and is 'clickable'
    Then I check every '1' seconds for at least '10' seconds until '3' elements exists with 'name:checkbox_5[]' and is 'clickable'
    And I wait '1' seconds

    @ignore @toocomplex
  Scenario: Verify that a warning alert appears when clicking on the excel icon without campaigns listed and that the alert can be
    dismissed or accepted

    Given My app is running in 'boit-uat24.privalia-test.com:80'
    And I browse to '/login'
    When '1' elements exists with 'xpath://*[@id="aclusr_username"]'
    Then I type 'root' on the element on index '0'
    When '1' elements exists with 'xpath://*[@id="aclusr_password"]'
    Then I type '1111' on the element on index '0'
    And '1' elements exists with 'xpath://*[@id="submit"]'
    And I click on the element on index '0'
    Given I wait '1' seconds
    When I browse to '/logisticorders/index'
    Then I check every '1' seconds for at least '10' seconds until '1' elements exists with 'xpath://*[@id="pager2"]/table/tbody/tr/td[2]' and is 'clickable'
    And I click on the element on index '0'
    Then I check every '1' seconds for at least '10' seconds until an alert appears
    Then I dismiss the alert
    And I wait '2' seconds
    And I click on the element on index '0'
    Then I check every '1' seconds for at least '10' seconds until an alert appears
    And I accept the alert
    And I browse to '/login/logout'
    And I wait '2' seconds