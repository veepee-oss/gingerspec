# Changelog

Only listing significant user-visible, not internal code cleanups and minor bug fixes.

## 0.2.0 (upcoming)

* No more a submodule project
* Added new aspect to force the browser name and version in tests development ,using FORCE_BROWSER (even availableUniqueBrowsers factory is defined).

  Eg: mvn -U verify -DSELENIUM_GRID=jenkins.stratio.com:4444 **-DFORCE_BROWSER=chrome_33**
