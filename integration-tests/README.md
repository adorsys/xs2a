#  Integration Tests

The integration tests for the XS2A Application serve the purpose to test the XS2A interface as a Black Box. The Endpoints 
of the interface are called with correct and errorful data to compare the responses of the interface to the
responses defined in the specification v1.2.


## Configuration and deployment

### Prerequisite
To run the Cucumber tests the ASPSP Profile, the ASPSP Mock Server, the Consent Management and the ASPSP XS2A Applications
need to be up and running. For specific instructions on how to configure and start these applications please see 
[README](../README.md).

### Starting the Tests
The Cucumber tests can be started from the CucumberIT.java file. 
Which can be found at `integration-tests/src/test/java/de/adorsys/aspsp/xs2a/integtest`

## Structure of the Tests

There are several components of the Cucumber tests: The Feature Files for defining the test steps, implementation of these 
steps and the Data Input for defining the Header/Body of the Requests and the expected Response of these Requests. 
The tests are logically separated in PIS (Payment Initiation Service), AIS (Account Information Service), 
SCA (Strong Customer Authentication) and FCS (Confirmation of Funds Service). The different components of the tests can 
be found at the following locations:

[Feature Files](/src/test/resources/features)  
[Test data (JSON Files)](/src/test/resources/data-input)  
[Implementation of steps](/src/test/java/de/adorsys/aspsp/xs2a/integtest/stepdefinitions/pis) 

