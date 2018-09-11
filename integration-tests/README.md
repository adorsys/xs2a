#  Integration Tests

Integration tests for the XS2A Application

## Configuration and deployment

### Prerequisite
To run the Cucumber tests the ASPSP Profile, the ASPSP Mock Server, the Consent Management and the ASPSP XS2A Applications
need to be up and running. For specific instructions on how to configure and start these applications, please see 
/README.md.

### Starting the Tests
The Cucumber tests can be started from the CucumberIT.java file. 
Which can be found at `integration-tests/src/test/java/de/adorsys/aspsp/xs2a/integtest.`

## Structure of the Tests

There several components of the Cucumber tests: The Feature Files for defining the test steps, implementation of these 
Steps and the Data Input for  defining the Header/Body of the Requests and the expected Response of these Requests. 
The tests are logically separated in PIS (Payment Initiation Service), AIS (Account Information Service), 
SCA (Strong Customer Authentication) and FCS (Confirmation of Funds Service). The different components of the tests can 
be found at the following locations:

The **Feature files** of the Cucumber tests: `/integration-tests/src/test/resources/features/`  
**Input** of the Test data (JSON Files): `/integration-tests/src/test/resources/data-input`  
The **Implementation** of the test steps: `/integration-tests/src/test/java/de/adorsys/aspsp/xs2a/integtest/stepdefinitions/pis` 

