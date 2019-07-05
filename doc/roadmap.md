# Roadmap

## Versions in progress

### version 2.10 (Planned date 19.07.2019)
* Bugfix: Using invalid date format results in internal Java error being returned to the TPP 
* Bugfix: Finalised status of AIS consent changes after expiration
* Bugfix: Optional fields in JSON structure are commented in yaml 
* Bugfix: CMS PSU-API: GET with Redirect-ID fails on Common-Payment 

 
### version 3.8 (Planned date 19.07.2019)
* All bugfixes from version 2.10
* Event api refactoring
* AspspConsentData refactoring
* Don't use any additional fields to identify TPP, except TPP ID 
* Add to events psuId from payments and consents if not present in request headers 
* Add call getAuthorisationByAuthorisationId to CMS-PSU-API



# Further development
Starting 15th of March 2019 XS2A Team is going to provide development within two branches:

## Stable branch 2.x
Stable branch will contain bugfixing and possibly necessary changes to support mandatory endpoints defined by Berlin Group NextGenPSD2 Spec 1.3
Stable branch 2.x will be supported at least till 01.09.2019


### version 2.11 (Planned date 02.08.2019)
* New internal request id 
* Include attributes in all access-log for all request entries 
* Bugfix: Provide correct PSU Data to the SPI in SpiContextData 
* Bugfix: Error on updating PSU Data with no body in the request 
* Bugfix: Incorrect response for Update PSU data for payment initiation request without psuId in header
* Bugfix: supportedTransactionApplicationTypes in profile should be String 
* Bugfix: Empty array "account" in Read Balances and Read Transaction List responses
* Bugfix: Check incoming requests to have required information
* Bugfix: Error on initiating payment with custom payment product and JSON body 

### version 2.12 (Planned date 16.08.2019)
* Include an attribute in all access-log for all response entries 
* Log http requests and responses to separate logger 
* Bugfix: Consents without successful authorisation should expire with status Rejected
* Bugfix: PSU data should be updated for both payment\consent and authorisation 
* Bugfix: Incorrect link in response to create consent authorisation request in Oauth SCA Redirect flow 
* Bugfix: When bank returns PATC status for payment, multilevel flag in CMS should be set to true 
* Bugfix: SpiAccountConsent shouldn't return real ID (PK from DB)
* Bugfix: POST cancellation-authorisations don't return cancellationId 

### version 2.13 (Planned date 30.08.2019)
* Bugfix: Populating PSU_DATA table with excessive data
* Bugfix: Incorrect TransactionId in read transaction details leads to internal server error 
* Bugfix: Only Pending transactions should be available in getTransactionList response when query param set to "pending"
* Bugfix: Incorrect error code in response for ReadAccountList for consent which was revoked by PSU 
* Bugfix: Wrong response for provision of an invalid TAN or password 
* Remove deprecate enum `ALL_ACCOUNTS_WITH_BALANCES` in `AccountAccessType` class in v.2.10

 
## Development branch 3.x
Development branch is oriented on implementation of new features and optional endpoints.
No backward compatibility with 2.x is guaranteed.

### version 3.9 (Planned date 02.08.2019)
* All bugfixes from version 2.11
* Multilevel SCA for Payment Initiation in Redirect approach
* Multilevel SCA for Establish Consent in Redirect approach 
* Implement Establish Signing Basket request
* Implement Cancellation of Signing Baskets

### version 3.10 (Planned date 16.08.2019)
* All bugfixes from version 2.12
* Support Signing Basket in Embedded approach with multilevel sca
* Support Signing Basket in Decoupled approach with multilevel sca
* Support Signing Basket in Redirect approach with multilevel sca
* Add getBasketAuthorisationByAuthorisationId to CMS-PSU-API 
* Add getBasketIdByRedirectId to CMS-PSU-API 
* Add getBasketByBasketId to CMS-PSU-API 

### version 3.11 (Planned date 30.08.2019)
* All bugfixes from version 2.13
* Implement Get Signing Basket Status Request
* Implement Get Signing Basket Request 
* Implement Get Authorisation Sub-resources for Signing Baskets
* Implement Get SCA Status request for Signing Baskets



### Upcoming features (Priorities may be changed)
* Execute payment without sca in OAuth approach 
* Validation of authorisation sub-resources
* Move AuthenticationObject to xs2a-core 
* Move PaymentAuthorisationType to the xs2a-core 
* Support delta access for transaction list
* Bugfix: Error 500 returned in attempt to delete consent in status "received" 
* Remove deprecated AspspConsentData updates in v.3.6
* Redesign of error handlers on SPI level 
* Bugfix: Bad request when TPP entern an unknown user in the AIS consent embedded approach
* Bugfix: Wrong protocol in links in response for mostly all endpoints (PIS, AIS) 
* Support of download link 
* Refactor CMS: return ResponseObject instead of Strings, Enums, Booleans etc.
* Optional SCA for Access to all Accounts for all PSD2 defined AIS – Global Consent 
* Go through code and aggregate all messages sent to PSU to message bundle  
* Support of relative links
* Component for scheduled batch processing 
* Support Get Transaction Status Response with xml format 
* Support Get Payment request for xml 
* Support of multicurrency accounts in AIS requests 
* Remove PSU data from CMS by request from ASPSP (for example due to Data protection (GDPR)) 
* Support sessions: Combination of AIS and PIS services 
* Add a new optional header TPP-Rejection-NoFunds-Preferred 
* Requirements on TPP URIs  
* handling for standard pain types
* Update enum MessageErrorCode.java 
* Add instance_id for export PIIS consent 
* Extend CMS to store sca method and TAN for Redirect approach 
* Add to events rejected requests 
* Extract events to separate module in CMS 
* Refactoring of payment saving Part 2 
* Refactor field validators (especially IBAN) to perform validation in Spring Component, not in static context 
* Recoverability 
* Implement CommonPaymentSpi interface in connector 
* Support all 3 formats of ISODateTime 
* Add service to delete consents and payments after period of time 
* Support OAuth sca for PIS
* Support OAuth sca for Payment cancellation
* Support OAuth sca for AIS 
* Payment Authorisations and Payment Cancellation Authorisations should be separated from AIS Consent Authorisations 
* Provide creation date and time in SPIrequest 
* add the request execution duration to the log 


#### Support of FundsConfirmation Consent:
* Establish FundsConfirmationConsent 
* Get FundsConfirmationConsent Status + object
* Revoke FundsConfirmationConsent
* FundsConfirmationConsent in Redirect approach with multilevel sca
* FundsConfirmationConsent in Embedded approach with multilevel sca
* FundsConfirmationConsent in Decoupled approach with multilevel sca
* Get Authorisation Sub-resource request for FundsConfirmationConsent
* Get Sca Status request for FundsConfirmationConsent 
* Create interface in cms-aspsp-api to get FundsConfirmationConsent 

#### Support of Card Accounts:
* Implement Read Card Account List request
* Implement Read Card Account Details request
* Implement Read Card Account Balance request
* Implement Read Card Account Transaction List request