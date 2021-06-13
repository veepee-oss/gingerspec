@web
Feature: Get webElement text and store in environment variable

  Scenario: Use google
    Given My app is running in '${DEMO_SITE_HOST}'
    When I browse to '/'
    Then '1' element exists with 'class:entry-title'
    When I save content of element in index '0' in environment variable 'h1text'
    Then I run 'echo '${h1text}' | grep "Home"' locally with exit status '0'


