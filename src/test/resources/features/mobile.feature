@ignore @toocomplex
@mobile
Feature: Running tests in mobile devices

    This feature provides an overview of the available steps for testing native mobile apps (android & ios).
    Notice that the @mobile annotation at the beginning of the feature is NECESSARY to bootstrap the Appium driver

    You can connect your mobile device as a node in a selenium grid using Appium, and run this feature like this
    (assuming grid is running in localhost:4444):
    mvn verify -Dit.test=com.privalia.qa.specs.MobileGIT -DSELENIUM_GRID=localhost:4444

    Or you can directly start an Appium server (no grid required) and run this feature like this (assuming Appium
    server is running in localhost:4723)
    mvn verify -Dit.test=com.privalia.qa.specs.MobileGIT -DSELENIUM_NODE=localhost:4723

  Scenario: Opening an closing the app
    Given I open the application
    And I wait '5' seconds
    Given I close the application

  Scenario: Changing orientation
    Given I rotate the device to 'landscape' mode
    And I wait '3' seconds
    Given I rotate the device to 'portrait' mode







