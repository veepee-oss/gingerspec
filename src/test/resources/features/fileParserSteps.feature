Feature: Verify the resulting records from a file

  This feature tests all the different methods for parsing files and filtering records.
  The library Utah Parser is used for parsing semi-structured text files, more information
  here: https://github.com/sonalake/utah-parser


  Scenario: Parse a text file against a XML definition and get a list of records
    Given I parse the file located at 'files/f10_ip_bgp_summary_example.txt' using the template defined in 'files/f10_ip_bgp_summary_template.xml'
    Then the result contains '3' records
    Then the result contains at least '3' records
    And the total of the column 'localAS' is '196653'
    And there are '3' records with column 'localAS' equal to '65551'
    And there are '1' records with column 'remoteAS' equal to '65553'
    And the record at position '0' at column 'remoteIp' has the value '10.10.10.10'


  Scenario: Matching multiple columns in a record
    Given I parse the file located at 'files/f10_ip_bgp_summary_example.txt' using the template defined in 'files/f10_ip_bgp_summary_template.xml'
    Then the result contains '3' records
    And I get the first record with column 'status' equal to '5'
    And the selected record matches the following cases:
     | uptime     | equal            | 10:37:12  |
     | routerId   | contains         | 192       |
     | remoteAS   | does not contain | 9         |


  Scenario: Filtering
    Given I parse the file located at 'files/f10_ip_bgp_summary_example.txt' using the template defined in 'files/f10_ip_bgp_summary_template.xml'
    Then the result contains '3' records
    And I select all records that match the following cases:
      | uptime    | contains         | 10:  |
    Then the result contains '2' records
    And I select all records that match the following cases:
      | status    | does not contain  | 0 |
    Then the result contains '1' records
    Then I get the record at position '0'
    And the selected record matches the following cases:
      | localAS   | equal  | 65551 |
      | remoteAS  | equal  | 65551 |