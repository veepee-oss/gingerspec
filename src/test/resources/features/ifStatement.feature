Feature: Conditional execution

  This allows the conditional execution of steps during runtime. All steps enclosed
  between this step and if (statement) { } will be executed only if the given
  statement returns true, otherwise, the steps will be skipped.
  The statement can be any javascript expression that can return a true|false output. You can even
  use variables created during the scenario execution.

  Warning: use this functionality sparingly, or only in very concrete automation cases. We discourage
  the creation of tests that could return different results on different runs. Also, this functionality
  has not been tested on multi-threaded mode, so it may break when running tests in parallel

  Scenario: Using if block to control execution
    * if (1==1) {
    * I run 'echo "This should be executed"' locally
    * }
    * if (1==2) {
    * I run 'echo "This should NOT be executed"' locally
    * I run 'exit 1' locally
    * }

  Scenario: Using variables created during the scenario
    * I save 'GingerSpec' in variable 'NAME'
    * if ('${NAME}'.contains('Ginger')) {
    * I run 'echo "This should be executed"' locally
    * }
    * if ('${NAME}'.contains('foo')) {
    * I run 'echo "This should NOT be executed"' locally
    * I run 'exit 1' locally
    * }