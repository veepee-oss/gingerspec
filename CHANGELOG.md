# Changelog

Only listing significant user-visible, not internal code cleanups and minor bug fixes.

## 0.4.0 (upcoming)

* [QA-272] better classes packaging
* [QA-298] Apache2 license. Step definitions redefined

## 0.3.0 (January 26, 2017)

* CukesGHooks will invoke the logger with each step

## 0.2.0 (June 2016)

* Ignored scenarios will fail if ignore cause was an already done jira ticket
* No more a submodule project
* Added new aspect to force the browser name and version in tests development ,using FORCE_BROWSER (even availableUniqueBrowsers factory is defined).

  Eg: mvn -U verify -DSELENIUM_GRID=jenkins.stratio.com:4444 **-DFORCE_BROWSER=chrome_33**
