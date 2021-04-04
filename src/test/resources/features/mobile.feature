@ignore @toocomplex
@mobile
Feature: Running tests in mobile devices

  This feature provides an overview of the available steps for testing native mobile apps (android & ios).
  Notice that the @mobile annotation at the beginning of the feature is NECESSARY to bootstrap the Appium driver

  Run this test like this: mvn verify -Dit.test=com.privalia.qa.specs.MobileGIT

  If not specified, GingerSpec will automatically use http://localhost:4723 as the url of the Appium server. You can
  specify any other url using -DSELENIUM_GRID=<url> when running

  Scenario: Opening an closing the app
    Given I open the application
    And I wait '5' seconds
    Given I close the application

  Scenario: Changing orientation
    Given I rotate the device to 'landscape' mode
    And I wait '3' seconds
    Given I rotate the device to 'portrait' mode







