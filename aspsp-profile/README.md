# ASPSP Profile

This component provides a static configuration of ASPSP, that allows XS2A to perform only with features and options,
supported by other ASPSP systems.

The component consists of 4 modules:
* [aspsp-profile-api](aspsp-profile-api/README.md) - an API of profile, that is used by other components of the system.
* [aspsp-profile-lib](aspsp-profile-lib/README.md) - a service implementation that is used on the server side and may be used as well in embedded approach.
* [aspsp-profile-remote](aspsp-profile-remote/README.md) - a service implementation that uses remotely deployed service (by aspsp-profile-server) via HTTP.
* [aspsp-profile-server](aspsp-profile-server/README.md) - a spring-boot application that serves ASPSP Profile via HTTP endpoints.

## Usage
This component can be used in two variants: **standalone** (with a spring boot server running) and **embedded**.
Generally standalone approach is recommended.

To access the ASPSP Profile service from your code, one shall use aspsp-profile-api dependency, connecting corresponding implementation dependency to a starter module.

Please note that other components, also on ASPSP side, may also need to access ASPSP Profile, so to use it embedded please plan your endpoints and connections accordingly.

Please refer for documentation of components for details of running and usage.

## Configuration
Configuration can be supplied using the yaml-file in the file system.
If no file is supplied, default configuration will be used.
To supply file please provide a property to spring framework (i.e. via application.properties of your app or environment variable):
```
bank_profile.path=/path/to/yaml-file
```

### Supported configuration options


| Option                         | Meaning                            | Default value                                        | Possible values                                                                                      |
|--------------------------------|------------------------------------|------------------------------------------------------|------------------------------------------------------------------------------------------------------|
|frequencyPerDay                 |                                    | 5                                                    |                                                                                                      |
|combinedServiceIndicator        |                                    | false                                                |                                                                                                      | 
|availablePaymentProducts        |                                    | sepa-credit-transfers, instant-sepa-credit-transfers | sepa-credit-transfers, instant-sepa-credit-transfers, target-2-payments,cross-border-credit-transfers| 
|availablePaymentTypes           |                                    | bulk, periodic, delayed                              | Note: single payments are always available                                                           | 
|scaApproach                     |                                    | REDIRECT                                             | REDIRECT, EMBEDDED, DECOUPLED, OAUTH                                                                 | 
|tppSignatureRequired            |                                    | false                                                |                                                                                                      | 
|bankOfferedConsentSupport       |                                    | false                                                |                                                                                                      | 
|pisRedirectUrlToAspsp           |                                    | http://localhost:4200/                               | String                                                                                               | 
|aisRedirectUrlToAspsp           |                                    | http://localhost:4200/                               | String                                                                                               | 
|multicurrencyAccountLevel       |                                    | SUBACCOUNT                                           | SUBACCOUNT, AGGREGATION, AGGREGATION_AND_SUBACCOUNT                                                  | 
|availableBookingStatuses        |                                    | BOOKED, PENDING                                      | BOOKED, PENDING, BOTH                                                                                | 
|supportedAccountReferenceFields |                                    | MSISDN                                               | IBAN, BBAN, PAN, MASKEDPAN, MSISDN. Note: IBAN is always supported                                   | 
|consentLifetime                 |                                    | 0                                                    |                                                                                                      | 
|transactionLifetime             |                                    | 0                                                    |                                                                                                      | 
|allPsd2Support                  |                                    | false                                                |                                                                                                      | 
|authorisationStartType          |                                    | EXPLICIT                                             | EXPLICIT, IMPLICIT                                                                                   | 
