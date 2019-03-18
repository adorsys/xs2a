# Roadmap

## Versions in progress

### version 2.0.1-Hotfix (Planned date 07.03.2019)
- Bugfix: No IBAN validation for payment initiation
- Bugfix: Date of periodic or future dated payments can be in the past
- Bugfix: List of balances and transactions of Dedicated AIS Consent are updated when the get accounts request is executed
- Bugfix: Unkown payment-service in GET requests results into wrong response
- Bugfix: Tpp-Redirect-Uri is not mandatory for REDIRECT approach
- Bugfix: Validation of frequency per day for consents
- Bugfix: No appropriate error message is returned when access to balances or transactions is not allowed
- Bugfix: No Validation for Expired QWAC

### version 2.1 (Planned date 15.03.2019)
- Event table new searchable columns: psu info and tpp
- Store in payments and in consents creation date and time
- Adjust ValidUntil date according to ConsentLifetime
- Call Spi for ConsentStatus on GET Consent request
- Add Logger to CMS

# Further development
Starting 15th of March 2019 XS2A Team is going to provide development within two branches:

## Stable branch 2.x
Stable branch will contain bugfixing and possibly necessary changes to support mandatory endpoints defined by Berlin Group NextGenPSD2 Spec 1.3
Stable branch 2.x will be supported at least till 01.09.2019


### version 2.2 (Planned date 29.03.2019)
- Extend logging with technical activities 
- Bugfix: Consents without successful authorisation should expire with status Rejected
- Payment Authorisations and Payment Cancellation Authorisations should be separated from AIS Consent Authorisations 
- Optional SCA for Access to all Accounts for all PSD2 defined AIS – Global Consent
- Multilevel SCA for Payment initiation in Redirect sca approach
- Multilevel SCA for consents in Redirect sca approach
- Bugfix: Return a corresponding "Bad Request" error response if consentApi._createConsent receives a consent object that contains both the list of accounts (resp. balances, transaction) and the flag AllPsd2 or AvailableAccounts 
- Bugfix: Check incoming requests to have required information
- Bugfix: 500 Error when double Start Cancellation Authorisation
- Bugfix: Wrong response body for Start Payment Authorisation request Redirect Explicit approach for XS2A connector
- Bugfix: Wrong response for provision of an invalid TAN or password 
- Bugfix: "Currency" should be optional while creating AIS consent
- Bugfix: Retrieve payment data by redirect-id with correct endpoint


### Upcoming features 2.x/3.x (Priorities may be changed)
- Payment sepa-credit-transfers validator
- Consent validator


## Development branch 3.x
Development branch is oriented on implementation of new features and optional endpoints.
No backward compatibility with 2.x is guaranteed.

### version 3.0 (Planned date 29.03.2019)
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

- Support of download link
- Remove PSU data from CMS by request from ASPSP (for example due to Data protection (GDPR))

- Support sessions: Combination of AIS and PIS services

###### Support of Card Accounts:
- Implement Read Card Account List request
- Implement Read Card Account Details request
- Implement Read Card Account Balance request
- Implement Read Card Account Transaction List request
