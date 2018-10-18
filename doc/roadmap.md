# Roadmap


## version 1.10 (Planned date: 26.10.2018) 
- version 2.0 of SPI-API
- Payment cancellation request according to specification 1.2
- SCA methods, make extendable list of authorisation methods
- Embedable Consent Management System
- Remove delayed payments,  use single payments
- Fix nullpointerexception while invoking /v1/accounts/ ( https://github.com/adorsys/xs2a/issues/16 )
- Support encryption of aspspConsentData.
- Prototype online banking (PSU-ASPSP interface)
    - Payment initiation
    - Account information service. Bank offered.
    - Account information service. Dedicated accounts.
- Migration to package and Maven GroupId "de.adorsys.psd2": XS2A


## version 1.11 (Planned date: 09.11.2018)
- Get list of consents by psu-id in Consent Management System.
- PIIS Consent. Post, Get, Put. Read aspspConsentData.
- PIS Support a matrix payment-product/payment-type in aspsp-profile and corresponding services.
- Support of Berlin Group XS2A Specification 1.3
- Support delta access for transaction list
- Consent access Validator
- Support of multicurrency account
- Multitier support in Consent Manager
- Change logic of Getting a confirmation on the availability of funds. 

