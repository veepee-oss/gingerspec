@web
Feature: Selenium wait with pooling

  Scenario: Verify that the logistic orders page displays correctly 11 clickable buttons in the top navigation bar

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
    Then I check every '1' seconds for at least '10' seconds until '11' elements exists with 'xpath://*[contains(@class, 'dir')]' and is 'clickable'
    And I browse to '/login/logout'
    And I wait '2' seconds


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