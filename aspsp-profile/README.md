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


| Option                                        | Meaning                                                                                                                      | Default value                                                | Possible values                                                                                       |
|-----------------------------------------------|------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------|-------------------------------------------------------------------------------------------------------|
|frequencyPerDay                                | This field indicates the requested maximum frequency for an access per day                                                   | 5                                                            | 0, 1, ...                                                                                             |
|combinedServiceIndicator                       | This field indicates if a payment initiation service will be addressed in the same "session" or not                          | false                                                        | true, false                                                                                           | 
|availablePaymentProducts                       | This field contains list of payment products supported by ASPSP                                                              | sepa-credit-transfers, instant-sepa-credit-transfers         | sepa-credit-transfers, instant-sepa-credit-transfers, target-2-payments, cross-border-credit-transfers, pain.001-sepa-credit-transfers, pain.001-instant-sepa-credit-transfers, pain.001-target-2-payments, pain.001-cross-border-credit-transfers| 
|availablePaymentTypes                          | This field contains list of payment types supported by ASPSP                                                                 | SINGLE, BULK, PERIODIC                                       | SINGLE, BULK, PERIODIC (Note: single payments are always available, even if not mentioned here)       | 
|scaApproach                                    | This field indicates SCA approach supported by ASPSP                                                                         | REDIRECT                                                     | REDIRECT, EMBEDDED, DECOUPLED, OAUTH                                                                  | 
|tppSignatureRequired                           | This field contains a signature of the request by the TPP on application level.                                              | false                                                        | true, false                                                                                           | 
|bankOfferedConsentSupport                      | This field indicates if an ASPSP supports "Bank Offered Consent" consent model or not                                        | false                                                        | true, false                                                                                           | 
|pisRedirectUrlToAspsp                          | This field contains URL to ASPSP service in order to to work with PIS                                                        | http://localhost:4200/pis/                                   | String                                                                                                | 
|aisRedirectUrlToAspsp                          | This field contains URL to ASPSP service in order to to work with AIS                                                        | http://localhost:4200/ais/                                   | String                                                                                                | 
|multicurrencyAccountLevel                      | This field contains multicurrency account types supported by ASPSP                                                           | SUBACCOUNT                                                   | SUBACCOUNT, AGGREGATION, AGGREGATION_AND_SUBACCOUNT                                                   | 
|availableBookingStatuses                       | This field contains booking statuses supported by ASPSP                                                                      | BOOKED, PENDING                                              | BOOKED, PENDING, BOTH                                                                                 | 
|supportedAccountReferenceFields                | This field contains account reference fields supported by ASPSP                                                              | MSISDN                                                       | IBAN, BBAN, PAN, MASKEDPAN, MSISDN. Note: IBAN is always supported                                    | 
|consentLifetime                                | This field contains the limit of a maximum lifetime of consent set in days                                                   | 0                                                            | days (0, 1, ...)                                                                                      | 
|transactionLifetime                            | This field contains the limit of a maximum lifetime of transaction set in days                                               | 0                                                            | days (0, 1, ...)                                                                                      | 
|allPsd2Support                                 | This field indicates if ASPSP supports Global consents or not                                                                | false                                                        | true, false                                                                                           | 
|authorisationStartType                         | This field indicates type of authorisation start                                                                             | EXPLICIT                                                     | EXPLICIT, IMPLICIT                                                                                    | 
|transactionsWithoutBalancesSupported           | This field indicates if an ASPSP might add balance information to transactions list or not                                   | false                                                        | true, false                                                                                           | 
|signingBasketSupported                         | This field indicates if an ASPSP supports signing basket or not                                                              | true                                                         | true, false                                                                                           | 
|paymentCancellationAuthorizationMandated       | This field indicates if payment cancellation authorization mandated or not                                                   | false                                                        | true, false                                                                                           | 
|piisConsentSupported                           | This field indicates if an ASPSP supports storing PIIS consent in CMS                                                        | false                                                        | true, false                                                                                           |
|deltaReportSupported                           | This field indicates if an ASPSP supports Delta reports for transaction details                                              | false                                                        | true, false                                                                                           |
|redirectUrlExpirationTimeMs                    | This field contains the limit of an expiration time of redirect url set in milliseconds                                      | 600 000                                                      | milliseconds (1, 2,...)                                                                               |
|notConfirmedConsentExpirationPeriodMs          | This field contains the limit of an expiration time of not confirmed consent set in milliseconds                             | 86 400 000                                                   | milliseconds (1, 2,...)                                                                               |
|notConfirmedPaymentExpirationPeriodMs          | This field contains the limit of an expiration time of not confirmed payment set in milliseconds                             | 86 400 000                                                   | milliseconds (1, 2,...)                                                                               |
|supportedPaymentTypeAndProductMatrix           | This field contains map of payment types and products supported by ASPSP                                                     | SINGLE: sepa-credit-transfers, instant-sepa-credit-transfers | (SINGLE, BULK, PERIODIC): sepa-credit-transfers, instant-sepa-credit-transfers, target-2-payments, cross-border-credit-transfers, pain.001-sepa-credit-transfers, pain.001-instant-sepa-credit-transfers, pain.001-target-2-payments, pain.001-cross-border-credit-transfers| 
|paymentCancellationRedirectUrlExpirationTimeMs | This field contains the limit of an expiration time of redirect url for payment cancellation set in milliseconds             | 600 000                                                      | milliseconds (1, 2,...)                                                                               |
