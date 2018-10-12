# Roadmap


## version 1.9 (planned date: 12.10.2018)
- Confirmation of funds request. Add interface on side of SPI.
- Add validation of TPP data
- Update of Payment cancellation request according to specification 1.2
- Change logic of implicit/explicit method of authorisation
- Update AspspConsentData field in the Consent. Support bytearray and base64 encoding.
- Validation of Consent (expiration date).
- Get list of reachable accounts. Embedded approach.
- Get balances for a given account. Embedded approach.
- Migration to package and Maven GroupId "de.adorsys.psd2": Consent Management
- Extend cms-client to work with AspspConsentData endpoints

## version 1.10 (Planned date: 26.10.2018) 
- version 2.0 of SPI-API
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




