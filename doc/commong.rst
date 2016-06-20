Common Methods
**************

There are several methods implemented in CommonG class that can be/are used in different feature steps implementations.

locateElement
=============

 Looks for webelements inside a selenium context and checks that the number of them matches the number passed as parameter.
 This search will be made by id, name and xpath expression matching an {@code locator} value.

- Receives:
	- String method: What label to search for (possible values: id | name | class | xpath | css).
	- String element: Element to search.
	- Integer expectedCount: Expected number of elements.

- Returns:
	- List<WebElement>: The list of elements.
	(Assertion failure will be returned if the expected number of elements is not found).


locateElements
==============

Looks for webelements inside a selenium context.
This search will be made by id, name and xpath expression matching an {@code locator} value

- Receives:
	- String method: What label to search for (possible values: id | name | class | xpath | css).
	- String element: Element to search.

- Returns:
	- String: Output file.


captureEvidence
===============

Captures a snapshot or an evidence in the driver.

- Receives:
	- WebDriver driver: The web driver.
	- String type: Type of capture.
	
- Returns:
	- The list of elements.


adjustLastCapture
=================

Cuts last image just in case it dupes information.

- Receives:
	- Integer newTrailingImageHeight: New image height.
	- List<File> capture: List of captures to adjust.
	
- Returns:
	- File: File containing adjusted images.

chromeFullScreenCapture
=======================

Scrolls loop n times to get the whole page if browser is chrome.

- Receives:
	- WebDriver driver: Web driver.
	
- Returns:
	- File: File containing full screen capture.


getDocumentHeight
=================

Gets the height of the html document.

- Receives:
	- WebDriver driver: Web driver.
	
- Returns:
	- Integer: Returns the height of the web element.


retrieveData
============

Returns the information contained in the file passed as parameter.

- Receives:
	- String baseData: Path to file to be read.
	- String type: Type of information, it can be: json|string.
	
- Returns:
	- String: Content of the file.

modifyData
==========

Returns the information modified according to modifications requested.

- Receives:
	- String data: String containing the information
	- String type: Type of information, it can be: json|string.
	- DataTable modifications: Changes to be made.
	
- Returns:
	- String: Modified string.

The modifications follow this structure:
::
	| <key> | <operation> | <value> |

Where:
	- key: key in the json or piece of string, depending on the type parameter.
	- operation: Can take the following values
		- DELETE: Delete the key in json or string in current value.
			- {"key1":"val1", "key2":"val2"} -> | key1 | DELETE | N/A | -> {"key2":"val2"}
			- "mystring" -> | str | DELETE | N/A | -> "mying"
		- ADD: Add new key to json or append string to current value.
			- {"key1":"val1", "key2":"val2"} -> | key3 | ADD | val3 | -> {"key1":"val1", "key2":"val2", "key3":"val3"}
			- "mystring" -> | N/A | ADD | new | -> "mystringnew"
		- UPDATE: Update value in key or modify part of string.
			- {"key1":"val1", "key2":"val2"} -> | key1 | UPDATE | newval1 | -> {"key1":"newval1", "key2":"val2"}
			- "mystring" -> | str | UPDATE | mod | -> "mymoding"
		- REPLACE: Update value in key or modify part of string.
			- {"key1":"val1", "key2":"val2"} -> | key1 | REPLACE | al->alue | -> {"key1":"value1", "key2":"val2"}
			- "mystring" -> | str | REPLACE | mod | -> "mymoding"
                - REPLACE: Modify part per Json object
		        - {"key1":"value1","key2":{"key3":"value3"}} -> | key2.key3 | REPLACE | 5 | number | -> {"key1":"value1","key2":{"key3":newValue}}
                        - "value3" -> | value3 | REPLACE | 5 | -> 5
		- APPEND: Append value to key value or to string
			- {"key1":"val1", "key2":"val2"} -> | key1 | APPEND | new | -> {"key1":"val1new", "key2":"val2"}
			- "mystring" -> | N/A | APPEND | new | -> "mystringnew"
		- PREPEND: Prepend value to key value or to string
			- {"key1":"val1", "key2":"val2"} -> | key1 | PREPEND | new | -> {"key1":"newval1", "key2":"val2"}
			- "mystring" -> | N/A | PREPEND | new | -> "newmystring"
	- value: New value (for certain operations (DELETE) this field is not used, and any value can be set, i.e. N/A)


generateRequest
===============

Generates the request based on the type of request, the end point, the data and type passed.

- Receives:
	- String requestType: Type of request to be sent (Implemented: GET|POST|PUT|DELETE, Non-implemented: CONNECT|PATCH|HEAD|OPTIONS|REQUEST|TRACE).
	- String endPoint: End point to sent the request to.
	- String data: Data to be sent for PUT/POST requests.
	- String type: Type of data to be sent (json|string).
	
- Returns:
	- Future<Response>: Response obtained from the request.


setPreviousElement
==================

Saves the value in the attribute in class extending CommonG.

- Receives:
	- String element: Attribute in class extending CommonG where to store the value.
	- String value: Value to be stored.
	
- Returns:
	- void

