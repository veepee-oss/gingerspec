@ignore @manual
Feature: Failed DCOS Authentication

  Scenario: Authentication with pem file null and password null: exception
    Given I want to authenticate in DCOS cluster '0.0.0.0' with email 'qatest@stratio.com' with user 'failedUser' and password '' using pem file 'none'