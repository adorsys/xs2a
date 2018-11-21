# Roadmap

## version 1.12 (Planned date: 23.11.2018)
- Payment cancellation flow.  Redirect and Embedded approaches
- Log TPP request
- Record TPP Request for auditing in the consent database 
- Expose an interface to export consents for further processing by ASPSP applications
- Provide an interface to lock TPP Access by any ASPSP Fraud Application
- Create one endpoint to read AspspConsentData
- Set finalised statuses for Consents, payment transaction and authorisation
- Move logic of Getting a confirmation on the availability of funds to the SPI level

## version 1.13 (Planned date 07.12.2018)
- Get list of consents by psu-id in Consent Management System
- Support delta access for transaction list
- Support Pain endpoints for initiation of payments
- Support of returning list of transactions in MT94x format

## version 1.14 (Planned date 21.12.2018)
- Support of multicurrency account
- Decoupled SCA approach support
- Expiration time of redirectUrl
- Support of Signing Basket
- PIS Support a matrix payment-product/payment-type in aspsp-profile and corresponding services.
- Implement Get Authorisation Sub-Resources Request
- Implmement Get SCA Status Request

