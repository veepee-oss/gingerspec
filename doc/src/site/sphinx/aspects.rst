Aspects
*******

Several aspects have been implemented to perform different operations at certain points during the execution.

Configuration
=============

If you want a particular aspect to be used in your test project, you will need to add a new entry in the aspects tag in aop.xml file (located at src/test/resources/META-INF/aop.xml).
The entries will look something like this:
::
	<?xml version="1.0" encoding="ISO-8859-1" standalone="no"?>
	<aspectj>
		<aspects>
			<aspect name="com.stratio.cucumber.aspects.IgnoreTagAspect" />
			<aspect name="com.stratio.cucumber.aspects.ReplacementAspect" />
		</aspects>
		<weaver
			options="-Xlint:ignore -Xset:weaveJavaPackages=true,weaveJavaxPackages=true">
			<dump within="com.stratio.specs..*" beforeandafter="true"/>
		</weaver>	
	</aspectj>

As you can see, there is an entry for each aspect to be used in the project.

Implemented Aspects
===================

These are the aspects currently implemented with a small explanation of their behavior.

AssertAspect
------------

This aspect will report to the user when an assert from 'org.hamcrest.MatcherAssert.assertThat' has failed.
Extra information like the reason for the failure will be provided.


AssertJAspect
-------------

This aspect will report to the user when an assert from 'org.assertj.core.internal.Failures.failure' has failed.
Extra information like the reason for the failure will be provided.
Example:
::
	E ssertJAspect 11:08:29 (sertJAspect.java: 31) - Assertion failed: Expected response status code to be <200> but was <500>


BrowsersDataProviderAspect
--------------------------

If a System property with FORCE_BROWSER exists then Methods in 'BrowsersDataProvider' will return its value.
This aspect is used to be able to define the browser to use when testing using Selenium.
Example:
::
	mvn verify -DSELENIUM_GRID=<selenium_grid_host>:<selenium_grid_port> -DFORCE_BROWSER=<selenium_browser> -Dit.test=<test_to_execute>


IgnoreTagAspect
---------------

Aspect used to ignore those tests tagged with '@ignore'.
There are three reasons for ignoring a test:

* '@tillfixed\\(\\w+-\\d+\\)': Ignored until certain specified bug is fixed.

* '@unimplemented': Test not yet implemented.

* '@toocomplex': Feature is too complex to be tested.

It is compulsory to define one of these extra tags.

ReplacementAspect
-----------------

This aspect is used to replace environment variables and variables defined in classes extending CommonG class with their assigned values.
This replacement is completely transparent to the user. User does not have to care about making replacements in the implementation of their features.
Implemented steps will always receive the replaced values.

* Example for environment variables

If we have the following step:
::
	Given I send requests to '${SPARKTA_HOST}:${SPARKTA_API_PORT}'

Given we launch the execution of the tests with:
::
	mvn verify -DSPARKTA_HOST=localhost -DSPARKTA_PORT=9090 -DSPARKTA_API_PORT=9091 -DSELENIUM_GRID=jenkins.stratio.com:4444 -DFORCE_BROWSER=chrome_aalfonsotest -Dit.test=com.stratio.sparkta.tests_at.api.fragments.Delete

SPARKTA_HOST and SPARKTA_API_PORT will be replaced by their defined value, localhost and 9091 respectively.

* Example for variables defined in class extended from CommonG

If we have the following steps:
::
	Scenario: Delete a fragment with type input and name inputfragment1 referenced by policy
		# Create fragment
		Given I send a 'POST' request to '/fragment' based on 'schemas/fragments/fragment.conf' as 'json' with:
		| id | DELETE | N/A |
		| name | UPDATE | inputfragment1 |
		| fragmentType | UPDATE | input |
		# Save fragment id
		Then the service response status must be '200'.
		And I save element '$.id' in attribute 'previousFragmentID'
		# Create policy referencing previously created fragment
		When I send a 'POST' request to '/policy' based on 'schemas/policies/policy.conf' as 'json' with:
		| fragments[0].id | UPDATE | !{previousFragmentID} |
		| fragments[0].name | UPDATE | inputfragment1 |
		| fragments[0].fragmentType | UPDATE | input |
		| fragments[1] | DELETE | N/A |
		| id | DELETE | N/A |
		| input | DELETE | N/A |

And we have implemented the following class:
::
	import com.stratio.specs.CommonG;

	public class Common extends CommonG {
    	public static String previousFragmentID = "";
    	public static String previousFragmentID_2 = "";
    	public static String previousPolicyID = "";
    	public static String previousPolicyID_2 = "";
	}

When we execute the code, we will store the parameter '$.id' returned by the execution of the 'POST' operation in the attribute 'previousFragmentID' defined in class Common.
Later on, in the datatable modification:
::
	| fragments[0].id | UPDATE | !{previousFragmentID} |
	
We will replace the value of key 'fragments[0].id' with the value stored in 'previousFragmentID'

The output of the execution of the steps above will look like this:
::
	I       Delete 11:19:16 (  WhenGSpec.java:230) - Retrieving data based on schemas/fragments/fragment.conf as json
	I       Delete 11:19:16 (  WhenGSpec.java:234) - Modifying data {"id":"id","fragmentType":"fragmentType","name":"name","description":"description","shortDescription":"shortDescription","element":{"name":"elementName","type":"elementType","configuration":{"consumerKey":"*****","consumerSecret":"*****","accessToken":"*****","accessTokenSecret":"*****"}}} as json
	I       Delete 11:19:17 (  WhenGSpec.java:238) - Generating request POST to /fragment with data {"element":{"name":"elementName","configuration":{"consumerKey":"*****","accessToken":"*****","accessTokenSecret":"*****","consumerSecret":"*****"},"type":"elementType"},"shortDescription":"shortDescription","description":"description","name":"inputfragment1","fragmentType":"input"} as json
	I       Delete 11:19:17 (  WhenGSpec.java:242) - Saving response
	I       Delete 11:19:17 (  ThenGSpec.java:547) - Verifying response message
	I       Delete 11:19:17 ( GivenGSpec.java: 54) - Saving element: $.id in attribute: previousFragmentID
	I  Reflections 11:19:17 (Reflections.java:224) - Reflections took 36 ms to scan 2 urls, producing 22 keys and 139 values 
	I  Reflections 11:19:17 (Reflections.java:224) - Reflections took 15 ms to scan 2 urls, producing 22 keys and 139 values 
	I       Delete 11:19:17 (  WhenGSpec.java:230) - Retrieving data based on schemas/policies/policy.conf as json
	I       Delete 11:19:17 (  WhenGSpec.java:234) - Modifying data {"id":"id","name":"name","description":"description","sparkStreamingWindow":6000,"checkpointPath":"checkpoint","rawData":{"enabled":"false","partitionFormat":"day","path":"myTestParquetPath"},"fragments":[{"id":"id","fragmentType":"type","name":"name","description":"description","shortDescription":"short description","element":null},{"id":"id","fragmentType":"type","name":"name","description":"description","shortDescription":"short description","element":null}],"input":{"name":"name","type":"input","configuration":{"consumerKey":"*****","consumerSecret":"*****","accessToken":"*****","accessTokenSecret":"*****"}},"cubes":[{"name":"testCube","checkpointConfig":{"timeDimension":"minute","granularity":"minute","interval":30000,"timeAvailability":60000},"dimensions":[{"name":"hashtags","field":"status","type":"TwitterStatus","precision":"hashtags"}],"operators":[{"name":"countoperator","type":"Count","configuration":{}}]}],"outputs":[{"name":"name","type":"output","configuration":{"isAutoCalculateId":"false","path":"/home/jcgarcia/yeah/","header":"false","delimiter":","}},{"name":"name2","type":"output","configuration":{"isAutoCalculateId":"false","path":"/home/jcgarcia/yeah/","header":"false","delimiter":","}}],"transformations":[{"name":"f","type":"Morphlines","order":1,"inputField":"_attachment_body","outputFields":["f"],"configuration":{"morphline":{"id":"morphline1","importCommands":["org.kitesdk.**"],"commands":[{"readJson":{}},{"extractJsonPaths":{"paths":{"field1":"/field-in-json1","field2":"/field-in-json2"}}},{"removeFields":{"blacklist":["literal:_attachment_body","literal:message"]}}]}}}]} as json
	I       Delete 11:19:17 (  WhenGSpec.java:238) - Generating request POST to /policy with data {"sparkStreamingWindow":6000,"description":"description","rawData":{"enabled":"false","partitionFormat":"day","path":"myTestParquetPath"},"fragments":[{"id":"4936d05c-d37e-4d4d-9288-de1b5f2b0906","element":null,"shortDescription":"short description","description":"description","fragmentType":"input","name":"inputfragment1"}],"name":"name","checkpointPath":"checkpoint","cubes":[{"checkpointConfig":{"interval":30000,"timeAvailability":60000,"granularity":"minute","timeDimension":"minute"},"name":"testCube","dimensions":[{"field":"status","precision":"hashtags","name":"hashtags","type":"TwitterStatus"}],"operators":[{"name":"countoperator","type":"Count","configuration":{}}]}],"outputs":[{"name":"name","type":"output","configuration":{"delimiter":",","path":"/home/jcgarcia/yeah/","isAutoCalculateId":"false","header":"false"}},{"name":"name2","type":"output","configuration":{"delimiter":",","path":"/home/jcgarcia/yeah/","isAutoCalculateId":"false","header":"false"}}],"transformations":[{"order":1,"outputFields":["f"],"name":"f","inputField":"_attachment_body","type":"Morphlines","configuration":{"morphline":{"id":"morphline1","commands":[{"readJson":{}},{"extractJsonPaths":{"paths":{"field2":"/field-in-json2","field1":"/field-in-json1"}}},{"removeFields":{"blacklist":["literal:_attachment_body","literal:message"]}}],"importCommands":["org.kitesdk.**"]}}}]} as json

We can see in the line saying 'Generating request POST to /policy', that the value '"fragments":[{"id":"id"' has been replaced by '"fragments":[{"id":"cd45c082-ec68-4bff-baba-390816f89da4"'

SeleniumAspect
--------------

If an exception is thrown by selenium, this aspect saves a screenshot.

IncludeTagAspect
----------------

It allows to include a previously defined scenario in the current one.
- If the tag is added at the feature level, the imported scenario is run as a background.
- If the tag is added at the scenario level, the imported scenario is added to the current scenario.

The tag syntax is the following:

- When importing asimple scenario:
	@include(feature:<feature_name>.feature, scenario:<scenario_name>)

- When importing a scenario receiving parameters:
	@include(feature:<feature_name>.feature, scenario:<scenario_name>, params:[<param_name1>:<param_value1>,...,<param_nameN>:<param_valueN>]