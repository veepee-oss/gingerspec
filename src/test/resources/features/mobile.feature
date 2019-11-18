@mobile

Feature: Running tests in mobile devices

    This feature provides an overview of the available steps for testing native mobile apps (android & ios)
    The mobile device must be connected to a selenium grid node

    Notice that the @mobile annotation at the begining of the feature is NECESSARY to bootstrap the Appium driver

  @ignore @toocomplex
  Scenario: Openning an closing the app
    Given I open the application
    And I wait '5' seconds
    Given I close the application

  Scenario: Interacting with elements
    And at least '1' mobile elements exists with 'id:com.google.android.calculator:id/digit_2'
    And I click on the mobile element on index '0'
    And at least '1' mobile elements exists with 'id:com.google.android.calculator:id/op_add'
    And I click on the mobile element on index '0'
    And at least '1' mobile elements exists with 'id:com.google.android.calculator:id/digit_2'
    And I click on the mobile element on index '0'
    And at least '1' mobile elements exists with 'id:com.google.android.calculator:id/eq'
    And I click on the mobile element on index '0'
    And I wait '3' seconds
    And at least '1' mobile elements exists with 'id:com.google.android.calculator:id/result_final'
    Then the mobile element on index '0' has '4' as text




