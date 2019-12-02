@mobile

Feature: Running tests in mobile devices

    This feature provides an overview of the available steps for testing native mobile apps (android & ios)
    The mobile device must be connected to a selenium grid node

    Notice that the @mobile annotation at the begining of the feature is NECESSARY to bootstrap the Appium driver

    Mobile devices should be connected to a Selenium Grid using Appium, so you must use -DSELENIUM_GRID parameter
    when running this feature, besides the -DAPP parameter, with the absolute address of the .apk (Android) or the
    .ipa (iOS) file to test

    mvn verify -Dit.test=com.privalia.qa.specs.MobileGIT
          -DSELENIUM_GRID=localhost:4444
          -DAPP=/Users/jose.fernandez/privalia/gingerspec/src/test/resources/calculator.apk


  Scenario: Openning an closing the app
    Given I open the application
    And I wait '5' seconds
    Given I close the application

  @ignore @toocomplex
  Scenario: Interacting with elements
    And at least '1' elements exists with 'id:com.google.android.calculator:id/digit_2'
    And I click on the element on index '0'
    And at least '1' elements exists with 'id:com.google.android.calculator:id/op_add'
    And I click on the element on index '0'
    And at least '1' elements exists with 'id:com.google.android.calculator:id/digit_2'
    And I click on the element on index '0'
    And at least '1' elements exists with 'id:com.google.android.calculator:id/eq'
    And I click on the element on index '0'
    And I wait '3' seconds
    And at least '1' elements exists with 'id:com.google.android.calculator:id/result_final'
    Then the element on index '0' has '4' as text


  Scenario: Changing orientation
    Given I rotate the device to 'landscape' mode
    And I wait '3' seconds
    Given I rotate the device to 'portrait' mode







