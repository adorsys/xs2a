# Roadmap

## Next versions
### version 1.17 (Planned date 15.02.2019)
- Extend cms-psu-api to support Bank-Offered consent
- Support of multiple SCA approaches simultaneously
- PSU-ID mandated by ASPSP
- Decoupled SCA approach support. Payments
- Decoupled SCA approach support. AIS
- Decoupled SCA approach support. Payment cancellation
- Provide endpoint to export AIS consents by aspspAccountId from CMS to ASPSP
- Provide endpoint to export PIIS consents by aspspAccountId from CMS to ASPSP


## Further development

Starting **1st of March 2019** XS2A Team is going to provide development within two branches:

### Stable branch 2.x
Stable branch will contain bugfixing and possibly necessary changes to support mandatory endpoints
defined by Berlin Group NextGenPSD2 Spec 1.3
Stable branch 2.x will be supported at least till 01.09.2019

#### version 2.0 (Planned date 01.03.2019)
- Multilevel SCA for Payment initiation in Redirect sca approach
- Multilevel SCA for consents in Embedded approach
- Multilevel SCA for consents in Redirect sca approach
- Multilevel SCA for Payment Cancellation in Embedded sca approach
- Multilevel SCA for Payment Cancellation in Redirect sca approach
- Multilevel SCA for Payment initiation in Decoupled sca approach
- Multilevel SCA for consents in Decoupled sca approach
- Multilevel SCA for Payment Cancellation in Decoupled sca approach
- Optional SCA for Access to all Accounts for all PSD2 defined AIS – Global Consent

### Development branch 3.x
Development branch is oriented on implementation of new features and optional endpoints.
No backward compatibility with 2.x is guaranteed.

#### version 3.0 (Planned date 01.03.2019)
- Migration to dynamic Sandbox, based on [ledgers project](https://github.com/adorsys/ledgers)
and corresponding [connector](https://github.com/adorsys/xs2a-connector-examples).
- Take out ASPSP Mock Server and corresponding Connector out of XS2A


#### Upcoming features 3.x (Priorities may be changed)
- Support delta access for transaction list 
- Multilevel SCA for consents in Embedded sca approach
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
- Support DTAZV cross-border payment
- Component for scheduled batch processing


- Support Get Transaction Status Response with xml format
- Support Get Payment request for xml
- Support of multicurrency accounts in AIS requests

- Support of Card Accounts:
  * Implement Read Card Account List request
  * Implement Read Card Account Details request
  * Implement Read Card Account Balance request
  * Implement Read Card Account Transaction List request
- Support sessions: Combination of AIS and PIS services
- Include CMS Events into XS2A Events journal
- Support of download link
- Remove PSU data from CMS by request from ASPSP (for example due to Data protection (GDPR))
