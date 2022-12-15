@web
Feature: Steps for testing web pages

  This feature provides examples on how to use some of the most common steps for selenium. This steps are also
  used for testing GingerSpec and make sure all steps work before every release.

  The steps make use of a local version of https://testpages.herokuapp.com, a public and free set of test pages
  for automating or testing. If you would like to run this tests, you can substitute http://${DEMO_SITE_HOST} by
  https://testpages.herokuapp.com.

  All scenarios that include steps for testing web pages using selenium (such as this one) must include the "@web"
  annotation. This is necessary, since it signals GingerSpec that it should bootstrap selenium and other important
  components.

  Rule: Opening the browser, navigating and managing windows

    Scenario: Open the browser and go to page
      Given I go to 'http://${DEMO_SITE_HOST}'

    Scenario: Maximize the browser
      Given I go to 'http://${DEMO_SITE_HOST}'
      Then I maximize the browser

    Scenario: Check that a new window opened
      Given I go to 'http://${DEMO_SITE_HOST}/styled/windows-test.html'
      Then I click on the element with 'id:gobasicajax'
      Then a new window is opened

    Scenario: Change focus to another opened window.
      Given I go to 'http://${DEMO_SITE_HOST}/styled/windows-test.html'
      Then I click on the element with 'id:gobasicajax'
      Then a new window is opened
      Then I change active window

    Scenario: Check the current url
      Given I go to 'http://${DEMO_SITE_HOST}/styled/index.html'
      Then we are in page 'http://${DEMO_SITE_HOST}/styled/index.html'

    Scenario: Check if current URL contains text
      Given I go to 'http://${DEMO_SITE_HOST}/styled/index.html'
      Then the current url contains the text 'styled'

    Scenario: Close the active window
      Given I go to 'http://${DEMO_SITE_HOST}/styled/index.html'
      And I close the current window

  Rule: Checking elements properties

    # Besides class, you can use id, name, css, xpath, linkText, partialLinkText or tagName
    Scenario: Verify element exists on the page
      Given I go to 'http://${DEMO_SITE_HOST}/styled/find-by-playground-test.html'
      Then '1' elements exists with 'id:p1'
      Then '2' elements exists with 'name:pName2'
      Then '118' elements exists with 'class:normal'
      Then at least '1' elements exists with 'class:explanation'

    Scenario: Verify text of an element
      Given I go to 'http://${DEMO_SITE_HOST}/styled/find-by-playground-test.html'
      And the element with 'id:p1' has 'This is a paragraph text' as text
      And the element with 'name:pName2' index '0' has 'This is b paragraph text' as text

    Scenario: Verify text of an element ignoring case
      Given I go to 'http://${DEMO_SITE_HOST}/styled/find-by-playground-test.html'
      And the element with 'id:p1' has 'ThIs Is A pArAgRaPh TeXt' as text ignoring case
      And the element with 'name:pName2' index '0' has 'ThIs Is B pArAgRaPh TeXt' as text ignoring case

    Scenario: Saving the text of an element
      Given I go to 'http://${DEMO_SITE_HOST}/styled/find-by-playground-test.html'
      When I save content of element with 'id:p1' in environment variable 'mytext'
      Then '${mytext}' matches 'This is a paragraph text'

    # Will look for the given text anywhere on the page DOM
    Scenario: Verify text exists in page source
      Given I go to 'http://${DEMO_SITE_HOST}/styled/index.html'
      Then this text exists:
            """
           Test Pages For Automating
           """
      Then this text exists ignoring case:
            """
           TeSt PaGeS FoR AuToMaTiNg
           """
      Then this text does not exist:
            """
           This text is not present in the source
           """

    Scenario: Verify if element is displayed
      Given I go to 'http://${DEMO_SITE_HOST}/styled/tag/dynamic-table.html'
      And the element with 'id:dynamictable' IS displayed
      And the element with 'id:jsondata' index '0' IS NOT displayed
      Then I click on the element with 'css:summary'
      When the element with 'id:jsondata' index '0' IS displayed

    Scenario: Verify if element is enabled
      Given I go to 'http://${DEMO_SITE_HOST}/styled/dynamic-buttons-disabled.html'
      And the element with 'id:button00' IS enabled
      And the element with 'id:button01' index '0' IS NOT enabled

    Scenario: Verify if an element is selected
      Given I go to 'http://${DEMO_SITE_HOST}/styled/basic-html-form-test.html'
      And the element with 'name:checkboxes[]' index '0' IS NOT selected
      When I click on the element with 'name:checkboxes[]' index '0'
      And the element with 'name:checkboxes[]' index '0' IS selected

    Scenario: Verify the value of element's attribute
      Given I go to 'http://${DEMO_SITE_HOST}/styled/basic-html-form-test.html'
      Then the element with 'name:password' has 'type' as 'password'
      Then the element with 'name:submitbutton' index '0' has 'type' as 'reset'

    Scenario: Saving the value of an element's attribute
      Given I go to 'http://${DEMO_SITE_HOST}/styled/basic-html-form-test.html'
      Then I save the value of the property 'type' of the element with 'name:password' in variable 'TYPE'
      Then '${TYPE}' matches 'password'

    Scenario: Scrolling up or down to element
      Given I go to 'http://${DEMO_SITE_HOST}/styled/basic-html-form-test.html'
      And at least '1' elements exists with 'name:username'
      Then I scroll up until the element with 'name:username' is visible
      Then I scroll down until the element with 'name:password' is visible
      Then I click on the element with 'name:password'

  Rule: Interacting with elements

    Scenario: Perform click on the an element
      Given I go to 'http://${DEMO_SITE_HOST}/styled/events/javascript-events.html'
      When I click on the element with 'id:onclick'
      When I click on the element with 'id:onclick' index '0'
      Then the element with 'id:onclickstatus' has 'Event Triggered' as text

    Scenario: Perform double click on an element
      Given I go to 'http://${DEMO_SITE_HOST}/styled/events/javascript-events.html'
      When I double click on the element with 'id:ondoubleclick'
      When I double click on the element with 'id:ondoubleclick' index '0'
      Then the element with 'id:ondoubleclickstatus' has 'Event Triggered' as text

    Scenario: Perform right click on an element
      Given I go to 'http://${DEMO_SITE_HOST}/styled/events/javascript-events.html'
      When I right click on the element with 'id:oncontextmenu'
      When I right click on the element with 'id:oncontextmenu' index '0'
      Then the element with 'id:oncontextmenustatus' has 'Event Triggered' as text

    Scenario: Hover mouse on an element
      Given I go to 'http://${DEMO_SITE_HOST}/styled/events/javascript-events.html'
      Given I hover on the element with 'id:onmouseover'
      Given I hover on the element with 'id:onmouseover' index '0'
      Then the element with 'id:onmouseoverstatus' has 'Event Triggered' as text

    Scenario: Typing text on an element
      Given I go to 'http://${DEMO_SITE_HOST}/styled/basic-html-form-test.html'
      And I type 'John Smith' on the element with 'name:username'
      And I type '12345678' on the element with 'name:password'
      And I type on the element with 'name:comments' the text:
      """
        (510) 794-7942
        4274 Deep Creek Rd
        Fremont, California(CA), 94555
      """

    Scenario: Clear the text on an element.
      Given I go to 'http://${DEMO_SITE_HOST}/styled/basic-html-form-test.html'
      And I type 'John Smith' on the element with 'name:username'
      And I clear the text of the element with 'name:username'
      Then the element with 'name:username' has '' as text

    Scenario: Press a key on an element
      Given I go to 'http://${DEMO_SITE_HOST}/styled/events/javascript-events.html'
      And I click on the element with 'id:onkeypress'
      Then I send 'ENTER' on the element with 'id:onkeypress'
      And the element with 'id:onkeypressstatus' has 'Event Triggered' as text

    Scenario: Select an option from a dropdown
      Given I go to 'http://${DEMO_SITE_HOST}/styled/basic-html-form-test.html'
      When I select 'Drop Down Item 1' on the element with 'name:dropdown'
      When I de-select every item on the element with 'name:multipleselect[]'
      When I select 'Selection Item 1' on the element with 'name:multipleselect[]'
      And I click on the element with 'name:submitbutton' index '1'
      And the element with 'id:_valuedropdown' has 'dd1' as text
      And the element with 'id:_valuemultipleselect0' has 'ms1' as text

  Rule: Waiting for elements

    Scenario: Wait until the element is present
      Given I go to 'http://${DEMO_SITE_HOST}/styled/dynamic-buttons-simple.html'
      And I click on the element with 'id:button00'
      And I wait until element with 'id:button01' is present
      And I click on the element with 'id:button01'

    Scenario: Wait an specific time until the element is present
      Given I go to 'http://${DEMO_SITE_HOST}/styled/dynamic-buttons-simple.html'
      And I click on the element with 'id:button00'
      And I click on the element with 'id:button01'
      Then I wait '5' seconds until element with 'id:button02' is present
      And I click on the element with 'id:button02'
      Then I wait '10' seconds until element with 'id:button03' is present
      And I click on the element with 'id:button03'
      And the element with 'id:buttonmessage' has 'All Buttons Clicked' as text

    Scenario: Wait for element to be present/visible/hidden/clickable (longer method)
      Given I go to 'http://${DEMO_SITE_HOST}/styled/javascript-redirect-test.html'
      And I click on the element with 'id:delaygotobasic'
      Then I check every '1' seconds for at least '10' seconds until '1' elements exists with 'id:goback' and is 'clickable'
      And I click on the element with 'id:goback'

  Rule: Managing alerts

    Scenario: Dismiss an alert
      Given I go to 'http://${DEMO_SITE_HOST}/styled/alerts/alert-test.html'
      And I click on the element with 'id:confirmexample'
      When I dismiss the alert
      Then the element with 'id:confirmreturn' has 'false' as text

    Scenario: Accept an alert
      Given I go to 'http://${DEMO_SITE_HOST}/styled/alerts/alert-test.html'
      And I click on the element with 'id:confirmexample'
      And I accept the alert
      Then the element with 'id:confirmreturn' has 'true' as text

    Scenario: Wait for an alert to appear
      Given I go to 'http://${DEMO_SITE_HOST}/styled/alerts/alert-test.html'
      When I click on the element with 'id:confirmexample'
      And I wait '5' seconds until an alert appears

  Rule: Working with file pickers/uploading files

    # Ignored since could fail while using selenium grid
    # The file must be reachable by the local browser
    @ignore
    Scenario: Uploading a new profile picture
      Given I go to 'http://${DEMO_SITE_HOST}/styled/file-upload-test.html'
      And '1' elements exists with 'id:fileinput'
      When I assign the file in 'schemas/empty.json' to the element with 'id:fileinput'
      Then I click on the element with 'name:upload'
      And the element with 'id:uploadedfilename' has 'empty.json' as text

  Rule: Dealing with iframes

    Scenario: Switch to iframe by locator
      Given I go to 'http://${DEMO_SITE_HOST}/styled/iframes-test.html'
      When I switch to iframe with 'id:thedynamichtml'
      Then the element with 'id:iframe0' has 'iFrame List Item 0' as text

    Scenario: Switch to a parent frame
      Given I go to 'http://${DEMO_SITE_HOST}/styled/iframes-test.html'
      When I switch to iframe with 'id:thedynamichtml'
      Then the element with 'id:iframe0' has 'iFrame List Item 0' as text
      Then I switch to a parent frame
      And the element with 'css:h1' has 'iFrames Example' as text


  Rule: Miscellaneous

    # Snapshots are saved under target/executions
    Scenario: Taking a snapshot
      Given I go to 'http://${DEMO_SITE_HOST}'
      Then I take a snapshot

    Scenario: Execute JavaScript functions
      Given I go to 'http://${DEMO_SITE_HOST}/styled/basic-html-form-test.html'
      And I execute 'arguments[0].click();' as javascript on the element with 'name:submitbutton'
      And I execute 'alert("This is an alert!")' as javascript
      And I accept the alert
      And I execute 'return document.URL;' as javascript and save the result in the environment variable 'PAGE'
      And '${PAGE}' contains 'basic-html-form-test'



