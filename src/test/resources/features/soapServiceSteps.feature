@ignore @toocomplex
Feature: communicating with a remote webservice

  This feature provides a basic example on how to execute remote methods of a webservice.
  Because this steps provide a way to interact with any webservice, besides the address
  of the WSDL, it is also necessary to know the format of the request and response messages
  that are interchanged during the communication (XML based messages).

  The library is also capable of replacing values in the requets body before sending and to
  evaluate specific values in the response body

  For this example, we are going to demostrate how to exacute the method of the webservice
  located in http://www.dneonline.com/calculator.asmx

  Scenario: Execute "Add" operation
    Given The webservice WSDL is located in 'http://www.dneonline.com/calculator.asmx?WSDL'
    When I execute the method 'Add' based on 'schemas/AddRequest.xml' with:
      | intA | 1 |
      | intB | 1 |
    Then The response matches the following cases:
      | AddResult | equal | 2 |
    When I execute the method 'Add' based on 'schemas/AddRequest.xml' with:
      | intA | 3 |
      | intB | 5 |
    Then The response matches the following cases:
      | AddResult | equal     | 8  |
      | AddResult | not equal | 10 |

  Scenario: Execute "Multiply" operation
    Given The webservice WSDL is located in 'http://www.dneonline.com/calculator.asmx?WSDL'
    When I execute the method 'Multiply' based on 'schemas/MultiplyRequest.xml' with:
      | intA | 2 |
      | intB | 2 |
    Then The response matches the following cases:
      | MultiplyResult | equal | 4 |
    When I execute the method 'Multiply' based on 'schemas/MultiplyRequest.xml' with:
      | intA | 4 |
      | intB | 4 |
    Then The response matches the following cases:
      | MultiplyResult | equal     | 16 |
      | MultiplyResult | not equal | 17 |

