# Roadmap

## Versions in progress

### version 2.0 (Planned date 01.03.2019)
- Consent may have several PSUs
- Store choosen scaApproach in CMS 
- Multilevel SCA for consents in Embedded sca approach
- Multilevel SCA for Payment initiation in Decoupled sca approach
- Multilevel SCA for consents in Decoupled sca approach
- Support DTAZV cross-border payment
- Provide the bookingStatus as parameter to ASPSP
- Bugfix: Embedded-authorisation-endpoints shouldn't be accessible in redirect/decoupled
- Bugfix: Wrong response for Payment Cancellation in Redirect Approach
- Bugfix: Payment cancellation should support other payment status
- Bugfix: add missing psuCorporateId for GetPisAuthorisationResponse and AisConsentAuthorizationResponse

# Further development
Starting 1st of March 2019 XS2A Team is going to provide development within two branches:

## Stable branch 2.x
Stable branch will contain bugfixing and possibly necessary changes to support mandatory endpoints defined by Berlin Group NextGenPSD2 Spec 1.3
Stable branch 2.x will be supported at least till 01.09.2019

### version 2.1 (Planned date 15.03.2019)

- Optional SCA for Access to all Accounts for all PSD2 defined AIS – Global Consent
- Multilevel SCA for Payment initiation in Redirect sca approach
- Multilevel SCA for consents in Redirect sca approach
- Extend logging with technical activities 
- Event table new searchable columns: psu info and tpp
- Store in payments and in consents creation date and time
- Payment Authorisations and Payment Cancellation Authorisations should be separated from AIS Consent Authorisations 
- Bugfix: 500 Error when double Start Cancellation Authorisation
- Bugfix: Return a corresponding "Bad Request" error response if consentApi._createConsent receives a consent object that contains both the list of accounts (resp. balances, transaction) and the flag AllPsd2 or AvailableAccounts 
- Bugfix: Wrong response body for Start Payment Authorisation request Redirect Explicit approach for XS2A connector
- Bugfix: Consents without successful authorisation should expire with status Rejected
- Bugfix: Wrong response for provision of an invalid TAN or password 
- Bugfix: "Currency" should be optional while creating AIS consent
- Bugfix: Validation of frequency per day for consents
- Bugfix: Tpp-Redirect-Uri is not mandatory for REDIRECT approach
- Bugfix: No IBAN validation for payment intiation
- Bugfix: Check incoming requests to have required information
- Bugfix: Retrieve payment data by redirect-id with correct endpoint

### Upcoming features 2.x/3.x (Priorities may be changed)
- Payment sepa-credit-transfers validator
- Consent validator

## Development branch 3.x
Development branch is oriented on implementation of new features and optional endpoints.
No backward compatibility with 2.x is guaranteed.

### version 3.0 (Planned date 15.03.2019)
- Migration to dynamic Sandbox, based on ledgers project and corresponding connector.
- Take out ASPSP Mock Server and corresponding Connector out of XS2A


### Upcoming features 3.x (Priorities may be changed)
- Implement Establish Signing Basket request
- Implement Get Signing Basket request
- Get Signing Basket Status Request
- Implement Get Authorisation Sub-resources for Signing Baskets
- Implement Get SCA Status request for Signing Baskets
- Implement Cancellation of Signing Baskets
- Support Signing Basket in Embedded and Decoupled approaches
- Support Signing Basket in Redirect approach
- Multilevel SCA for Signing Basket in Embedded sca approach
- Multilevel SCA for Signing Basket in Redirect sca approach
- Multilevel SCA for Signing Basket in Decoupled sca approach


- Support delta access for transaction list
- Component for scheduled batch processing
- Support Get Transaction Status Response with xml format
- Support Get Payment request for xml
- Support of multicurrency accounts in AIS requests

- Include CMS Events into XS2A Events journal
- Support of download link
- Remove PSU data from CMS by request from ASPSP (for example due to Data protection (GDPR))
- Provide several PSU's data to SPI-level

- Support sessions: Combination of AIS and PIS services

###### Support of Card Accounts:
- Implement Read Card Account List request
- Implement Read Card Account Details request
- Implement Read Card Account Balance request
- Implement Read Card Account Transaction List request
