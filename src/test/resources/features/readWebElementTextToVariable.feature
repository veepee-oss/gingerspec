@web
Feature: Get webElement text and store in environment variable

  Scenario: Use google
    Given My app is running in 'www.google.es'
    When I browse to '/'
    Then '2' element exists with 'css:a[class="gb_P"]'
    When I save content of element in index '0' in environment variable 'textCorreo'
    Then I run 'echo '!{textCorreo}' | grep "Gmail"' locally with exit status '0'