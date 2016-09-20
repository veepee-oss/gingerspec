@ignore @toocomplex
Feature: Authentication DCOS

  Scenario: Authentication with pem file null and password not null
    Given I want to authenticate in DCOS cluster '${DCOS_IP}' with email 'qatest@stratio.com' with user '${USER}' and password '${PASS}' using pem file 'none'

  Scenario: Authentication with pem file not null and password null
    Given I want to authenticate in DCOS cluster '${DCOS_IP}' with email 'qatest@stratio.com' with user '${USER}' and password '' using pem file '${PEM}'
