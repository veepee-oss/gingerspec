@rest
Feature: Elasticsearch steps test

  Scenario: Connect to Elasticsearch
    Given I connect to Elasticsearch cluster at host '${ES_NODE}'
    Given I connect to 'Elasticsearch' cluster at '${ES_CLUSTER}'

  Scenario: Obtain clustername in Elasticsearch
    Given I obtain elasticsearch cluster name in '${ES_NODE}:9200' and save it in variable 'clusternameES'

  Scenario: Create new index in Elasticsearch
    Given I create an elasticsearch index named 'indexes' removing existing index if exist
    Then An elasticsearch index named 'indexes' exists

#  Scenario: Execute a query in Elasticsearch
#    Given I execute an elasticsearch query over index 'indexES' and mapping '.*' and column '.*' with value '.*' to '.*'
#    Given The Elasticsearch index named '.+' and mapping '.+' contains a column named '.+' with the value '.+'

  Scenario: Connect to Elasticsearch with params
    Given I drop an elasticsearch index named 'indexes'
    Given An elasticsearch index named 'indexes' does not exist

  Scenario: Drop Elasticsearch indexes
    Given I drop every existing elasticsearch index

  Scenario: Connect to Elasticsearch with clustername obtained
    Given I connect to Elasticsearch cluster at host '${ES_NODE}' using native port '9300' using cluster name '!{clusternameES}'
