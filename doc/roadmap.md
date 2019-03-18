# Roadmap

## Versions in progress

### version 2.2 (Planned date 29.03.2019)
- Add Card number for PIIS
- Add status and date of last status change for PIS, AIS, PIIS during export
- Bugfix: Check TPPinfo in all calls after initial request
- Bugfix: All links must be contained in an object with "href"

### version 3.0 (Planned date 29.03.2019)
- Migration to dynamic Sandbox, based on ledgers project and corresponding connector.
- Take out ASPSP Mock Server and corresponding Connector out of XS2A


# Further development
Starting 15th of March 2019 XS2A Team is going to provide development within two branches:

## Stable branch 2.x
Stable branch will contain bugfixing and possibly necessary changes to support mandatory endpoints defined by Berlin Group NextGenPSD2 Spec 1.3
Stable branch 2.x will be supported at least till 01.09.2019

### version 2.3 (Planned date 12.04.2019)
- Bugfix: WithBalance = true does not check / validate if permission exists in the Consent
- Bugfix: Get authorisations of payment when one is already finalised 
- Bugfix: XS2A ObjectMapper clashes with Bank Offered Consents
- Bugfix: Invalid or null X-Request-ID returns wrong error in PIS Services
- Bugfix: EndToEndIdentification in CMS DB too short 
- Bugfix: Embedded approach will not work if order of scaApproaches in bank_profile is: first - REDIRECT then - EMBEDDED 


### version 2.4 (Planned date 26.04.2019)
- Bugfix: Check incoming requests to have required information
- Bugfix: Retrieve payment data by redirect-id with correct endpoint
- Bugfix: Wrong Error code in payment initiation respond for not supported xml product types 
- Bugfix: Use Sca Approach from Authorisation Object instead of separate resolving startAuthorisationWithPsuAuthentication link not available in response for start the authorisation process for a payment initiation 
- Bugfix: Consents without successful authorisation should expire with status Rejected 
- Bugfix: "Currency" should be optional while creating AIS consent
- Bugfix: scaStatus link not available in response for Update PSU data for payment initiation (Decoupled Implicit) 
- Bugfix: Wrong response body for Start Payment Authorisation request Redirect Explicit approach for XS2A connector


### version 2.5 (Planned date 10.05.2019)
- Bugfix: Populating PSU_DATA table with excessive data
- Bugfix: Provide correct PSU Data to the SPI in SpiContextData
- Bugfix: Matching of fund confirmation to aspspConsentData
- Bugfix: Bad request when TPP enters an unknown user in the AIS consent embedded approach
- Bugfix: Wrong response for provision of an invalid TAN or password 



### Upcoming features 2.x/3.x (Priorities may be changed)
- Multilevel SCA for Payment initiation in Redirect sca approach
- Multilevel SCA for consents in Redirect sca approach
- Payment Authorisations and Payment Cancellation Authorisations should be separated from AIS Consent Authorisations 
- Optional SCA for Access to all Accounts for all PSD2 defined AIS – Global Consent
- Extend logging with technical activities 
- Support of relative links
- Provide creation date and time in SPIrequest
- add the request execution duration to the log  (Consors)
- Validation of authorisation sub-resources 
- Payment Cancellation Request update  
- Payment sepa-credit-transfers validator
- Consent validator


## Development branch 3.x
Development branch is oriented on implementation of new features and optional endpoints.
No backward compatibility with 2.x is guaranteed.

### Upcoming features 3.x (Priorities may be changed)

- Support OAuth sca
- Support delta access for transaction list
- Component for scheduled batch processing
- Support Get Transaction Status Response with xml format
- Support Get Payment request for xml
- Support of multicurrency accounts in AIS requests
- Support of download link
- Remove PSU data from CMS by request from ASPSP (for example due to Data protection (GDPR))
- Support sessions: Combination of AIS and PIS services
- Add a new optional header TPP-Rejection-NoFunds-Preferred 
- Requirements on TPP URIs  
- FrequencyPerDay should be counted for requests without PSU involvement  (DAB) 
- aspspAccountId no longer available for SPI (ARZ)
- handling for standard pain types (ARZ)

###### Support of Signing Basket
- Implement Establish Signing Basket request
- Implement Get Signing Basket request
- Get Signing Basket Status Request
- Implement Get Authorisation Sub-resources for Signing Baskets
- Implement Get SCA Status request for Signing Baskets
- Implement Cancellation of Signing Baskets
- Support Signing Basket in Embedded approach with multilevel sca
- Support Signing Basket in Decoupled approach with multilevel sca
- Support Signing Basket in Redirect approach with multilevel sca


###### Support of FundsConfirmation Consent:
- Establish FundsConfirmationConsent 
- Get FundsConfirmationConsent Status + object
- Revoke FundsConfirmationConsent
- FundsConfirmationConsent in Redirect approach with multilevel sca
- FundsConfirmationConsent in Embedded approach with multilevel sca
- FundsConfirmationConsent in Decoupled approach with multilevel sca
- Get Authorisation Sub-resource request for FundsConfirmationConsent
- Get Sca Status request for FundsConfirmationConsent 
- Create interface in cms-aspsp-api to get FundsConfirmationConsent 

###### Support of Card Accounts:
- Implement Read Card Account List request
- Implement Read Card Account Details request
- Implement Read Card Account Balance request
- Implement Read Card Account Transaction List request
