# Release notes v. 1.15

## Table of contents
- [Get SCA Status Request](#get-sca-status-request)
- [TPP-Nok-Redirect-URI returned when scaRedirect URI is expired (for AIS)](#tpp-nok-redirect-uri-returned-when-scaredirect-uri-is-expired-for-ais)
- [One active authorisation per payment for one PSU](#one-active-authorisation-per-payment-for-one-psu)
- [Bugfix: validate PSU credentials during update PSU data requests](#bugfix-validate-psu-credentials-during-update-psu-data-requests)
- [Bugfix: method encryptConsentData in SecurityDataService takes byte array as an argument](#bugfix-method-encryptconsentdata-in-securitydataservice-takes-byte-array-as-an-argument)
- [Add Instance ID to services in cms-aspsp-api and cms-psu-api](#add-instance-id-to-services-in-cms-aspsp-api-and-cms-psu-api)
- [Bugfix: Embedded SCA Approach is not supported for Bank Offered Consent](#bugfix-embedded-sca-approach-is-not-supported-for-bank-offered-consent)
- [Implement interfaces for exporting consents/payments from CMS](#implement-interfaces-for-exporting-consentspayments-from-cms)
- [Obsoleting Consents that were not confirmed](#obsoleting-consents-that-were-not-confirmed)
- [Obsoleting Payments that were not confirmed](#obsoleting-payments-that-were-not-confirmed)
- [Upgrade version of Jackson library](#upgrade-version-of-jackson-library)
- [XS2A now supports definition of allowed payments products by each payment type](#xs2a-now-supports-definition-of-allowed-payments-products-by-each-payment-type)
- [Add specific bank account identifier in all types of payments, AIS and PIIS consents](#add-specific-bank-account-identifier-in-all-types-of-payments-ais-and-piis-consents)
- [One active authorisation per consent for one PSU](#one-active-authorisation-per-consent-for-one-psu)
- [Added expiration time for payment cancellation redirect URL](#added-expiration-time-for-payment-cancellation-redirect-url)
- [Bugfix: Deleted consent changes its status to terminatedByTpp](#bugfix-deleted-consent-changes-its-status-to-terminatedbytpp)
- [Support of single, periodic and bulk payment initiation with pain.001 XML message](#support-of-single-periodic-and-bulk-payment-initiation-with-pain001-xml-message)
- [Implementation of specification 1.3 according to the yaml file from Berlin Group](#implementation-of-specification-13-according-to-the-yaml-file-from-berlin-group)

## Get SCA Status Request
Endpoints for getting the SCA status of the authorisation were implemented.
Available endpoints are listed below.

| Context                             | Method | Endpoint                                                                        | Description                                                         |
|-------------------------------------|--------|---------------------------------------------------------------------------------|---------------------------------------------------------------------|
| Payment Initiation Request          | GET    | /v1/{payment-service}/{paymentId}/authorisations/{authorisationId}              | Checks the SCA status of a authorisation sub-resource.              |
| Payment Cancellation Request        | GET    | /v1/{payment-service}/{paymentId}/cancellation- authorisations/{cancellationId} | Checks the SCA status of a cancellation authorisation sub-resource. |
| Account Information Consent Request | GET    | /v1/consents/{consentId}/authorisations/{authorisationId}                       | Checks the SCA status of a authorisation sub-resource.              |

## TPP-Nok-Redirect-URI returned when scaRedirect URI is expired (for AIS)
Now for AIS if scaRedirect URI is expired we deliver TPP-Nok-Redirect-URI in the response from CMS to Online-banking. This response is returned with code 408.
If TPP-Nok-Redirect-URI was not sent from TPP and in CMS is stored null, then CMS returns empty response with code 408. If payment is not found or psu data is incorrect, CMS returns 404. 

## One active authorisation per payment for one PSU
When PSU creates new authorisation for a payment, all previous authorisations, created by this PSU for the same payment, will be failed and expired.

## Bugfix: validate PSU credentials during update PSU data requests
From now on SPI response status UNAUTHORIZED_FAILURE corresponds to PSU_CREDENTIALS_INVALID error(response code HTTP 401).

Now SPI-Mock correctly handles invalid PSU credentials.

## Bugfix: method encryptConsentData in SecurityDataService takes byte array as an argument
Now to encrypt aspspConsentData in SecurityDataService we should provide byte array as an argument instead of Base64 encoded string.

## Add Instance ID to services in cms-aspsp-api and cms-psu-api
From now on methods in cms-aspsp-api and cms-psu-api also require `instanceId` to be provided as a mandatory argument.
This id represents particular service instance and is used for filtering data from the database.

All corresponding CMS endpoints were also updated and from now on support instanceId as an optional header. 
If the header isn't provided, default value `UNDEFINED` will be used instead.

The following services were affected by this change:
  - In consent-aspsp-api:
    - `de.adorsys.psd2.consent.aspsp.api.ais.CmsAspspAisExportService`
    - `de.adorsys.psd2.consent.aspsp.api.piis.CmsAspspPiisService`
    - `de.adorsys.psd2.consent.aspsp.api.pis.CmsAspspPisExportService`
  - In consent-psu-api:
    - `de.adorsys.psd2.consent.psu.api.CmsPsuAisService`
    - `de.adorsys.psd2.consent.psu.api.CmsPsuPiisService`
    - `de.adorsys.psd2.consent.psu.api.CmsPsuPisService`

## Bugfix: Embedded SCA Approach is not supported for Bank Offered Consent
According to Berlin Group Spec, now Bank Offered Consent is not supported for Embedded SCA Approach.

If ASPSP doesn't support Bank Offered Consent at all (defined by parameter `bankOfferedConsentSupport` in ASPSPS Profile) 
then TPP will receive HTTP 405 response code with message code "SERVICE_INVALID" for any approach, 
instead of "PARAMETER_NOT_SUPPORTED" (HTTP 400 response code).

## Implement interfaces for exporting consents/payments from CMS
Implementations for Java interfaces `de.adorsys.psd2.consent.aspsp.api.ais.CmsAspspAisExportService` and
 `de.adorsys.psd2.consent.aspsp.api.pis.CmsAspspPisExportService` were provided.

Corresponding endpoints were also added to CMS, they are listed in the table below.

| Method | Endpoint                               | Description                                                                                                          |
|--------|----------------------------------------|----------------------------------------------------------------------------------------------------------------------|
| GET    | aspsp-api/v1/ais/consents/tpp/{tpp-id} | Returns a list of AIS consent objects by given mandatory TPP ID, optional creation date, PSU ID Data and instance ID |
| GET    | aspsp-api/v1/ais/consents/psu          | Returns a list of AIS consent objects by given mandatory PSU ID Data, optional creation date and instance ID         |
| GET    | aspsp-api/v1/pis/payments/tpp/{tpp-id} | Returns a list of payments by given mandatory PSU ID Data, optional creation date and instance ID.                   |
| GET    | aspsp-api/v1/pis/payments/psu          | Returns a list of payments by given mandatory TPP ID, optional creation date, PSU ID Data and instance ID.           |

## Obsoleting Consents that were not confirmed
Now ASPSP developer is able to provide the period of time (in milliseconds). Not confirmed during this period of time AIS consents will be considered obsolete.
The default value is set to 24 hours. Can be adjusted via ASPSP Profile Parameter `notConfirmedPaymentExpirationPeriodMs`.
Consent status becomes `EXPIRED` and SCA status for dedicated consent authorisation becomes `FAILED` on such requests:
* TPP sends Get consent status request
* xs2a receives start authorisation request for this consent
* TPP sends Get SCA status request
* xs2a receives Update PSU Data request for this payment

Also, scheduler service has been created: it will obsolete all the AIS consents with confirmation expired.
The scheduler service invocation frequency could be modified by changing `not-confirmed-consent-expiration.cron.expression` value in `application.properties`.
The default value is the top of every hour of every day.

## Obsoleting Payments that were not confirmed
Now ASPSP developer is able to provide the period of time (in milliseconds). Not confirmed during this period payments will be considered obsolete.
The default value is set to 24 hours. Can be adjusted via ASPSP Profile Parameter `notConfirmedPaymentExpirationPeriodMs`.
Transaction status becomes `REJECTED` and SCA status for dedicated payment authorisation becomes `FAILED` on such requests:
* TPP sends Get transaction status request
* xs2a receives start authorisation request for this payment
* TPP sends Get SCA status request
* xs2a receives Update PSU Data request for this payment

Also, scheduler service has been created: it will obsolete all the payments with confirmation expired.
The scheduler service invocation frequency could be modified by changing `not-confirmed-payment-expiration.cron.expression` value in `application.properties`.
The default value is the top of every hour of every day.

## Upgrade version of Jackson library
We updated Jackson version because of vulnerability found in FasterXML jackson-databind library.

[jackson-databind 2.x versions before 2.9.8 might allow attackers to have unspecified impact by leveraging failure to block the axis2-transport-jms class from polymorphic deserialization](https://nvd.nist.gov/vuln/detail/CVE-2018-19360)

## XS2A now supports definition of allowed payments products by each payment type

ASPSP now has a possibility to choose which combination payment-product/payment-type to work with.
Now to set available payment products for each type, the following table in ASPSP Profile to be filled:
```
 supportedPaymentTypeAndProductMatrix:
  SINGLE:
   - sepa-credit-transfers
   - instant-sepa-credit-transfers
  PERIODIC:
   - sepa-credit-transfers
   - instant-sepa-credit-transfers
  BULK:
   - sepa-credit-transfers
   - instant-sepa-credit-transfers
```
Other payment products can be added for every payment type.

## Add specific bank account identifier in all types of payments, AIS and PIIS consents
SPI Developers now able to provide specific unique identifier for bank accounts used in payments, AIS and PIIS consents.
This allows to bind these objects in CMS to specific customers or accounts in order to implement other services for the customer.
Field `aspspAccountId` can be provided in a response to SPI `initiatePayment` or `initiateConsent` request.
For PIIS `aspspAccountId` can be provided on creation of PIIS consent on endpoint **POST /aspsp-api/v1/piis/consents** as a part of account data.

This field can be used as a search criteria on export endpoints in CMS then.

## One active authorisation per consent for one PSU
When PSU creates new authorisation for consent, all previous authorisations, created by this PSU for the same consent, will be failed and expired.

## Added expiration time for payment cancellation redirect URL
A new `paymentCancellationRedirectUrlExpirationTimeMs` parameter has been added to ASPSP profile.

| Option                                           | Meaning                                                                                                          | Default value | Possible values         |
|--------------------------------------------------|------------------------------------------------------------------------------------------------------------------|---------------|-------------------------|
| `paymentCancellationRedirectUrlExpirationTimeMs` | This field contains the limit of an expiration time of redirect URL for payment cancellation set in milliseconds | 600 000       | milliseconds (1, 2,...) |

Payment cancellation redirect URL and related authorisation now have an expiration time. The value for expiration time is counted with formula 
"current time of authorisation creation + payment cancellation redirect URL expiration time (set in ASPSP-profile)". 
We give redirect id (= authorisation id) in redirect link now, and to get payment information, online banking should call 
 **GET /psu-api/v1/pis/consent/redirects/cancellation/{redirect-id}** endpoint of consent management system.
If redirect URL is not expired, online banking gets payment, authorisation id, not ok tpp redirect URL and ok tpp redirect URL (for now these URLs are null temporary) in response, otherwise http code 408 Request Timeout is sent.

## Bugfix: Deleted consent changes its status to terminatedByTpp
When TPP sends request to delete consent, status of consent now will be `terminatedByTpp` instead of `revokedByPsu`.

## Support of single, periodic and bulk payment initiation with pain.001 XML message
Now TPP can initiate payments with pain.001 XML message body. Content type of the request should be `application/xml` for single and bulk payments 
and `multipart/form-data; boundary=AaaBbbCcc` for periodic payments with xml body part named `xml_sct` and json body part named `json_standingorderType`. 
The body of the pain.001 xml payment is stored as a byte array in the consent management system.

## Implementation of specification 1.3 according to the yaml file from Berlin Group
Now XS2A interface are updated according to the requirements of specification 1.3 from Berlin Group. No changes on SPI level were performed, only controllers and related classes were changed.

Payment product was added as a path parameter to certain PIS endpoints:

| Method | Context                                                                  | Old path                                                                       | New path                                                                                         |
|--------|--------------------------------------------------------------------------|--------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------|
| GET    | Get Payment Request                                                      | /v1/{payment-service}/{paymentId}                                              | /v1/{payment-service}/{payment-product}/{paymentId}                                              |
| GET    | Get Transaction Status Request                                           | /v1/{payment-service}/{paymentId}/status                                       | /v1/{payment-service}/{payment-product}/{paymentId}/status                                       |
| DELETE | Payment Cancellation Request                                             | /v1/{payment-service}/{paymentId}                                              | /v1/{payment-service}/{payment-product}/{paymentId}                                              |
| GET    | Get Cancellation Authorisation Sub-Resources Request                     | /v1/{payment-service}/{paymentId}/cancellation-authorisations                  | /v1/{payment-service}/{payment-product}/{paymentId}/cancellation-authorisations                  |
| POST   | Start Authorisation Process in context of a Payment Initiation Request   | /v1/{payment-service}/{paymentId}/authorisations                               | /v1/{payment-service}/{payment-product}/{paymentId}/authorisations                               |
| POST   | Start Authorisation Process in context of a Payment Cancellation Request | /v1/{payment-service}/{paymentId}/cancellation-authorisations                  | /v1/{payment-service}/{payment-product}/{paymentId}/cancellation-authorisations                  |
| PUT    | Update PSU Data in the context of a Payment Initiation Request           | /v1/{payment-service}/{paymentId}/authorisations/{authorisationId}             | /v1/{payment-service}/{paymentId}/{payment-product}/authorisations/{authorisationId}             |
| PUT    | Update PSU Data in the context of a Payment Cancellation Request         | /v1/{payment-service}/{paymentId}/cancellation-authorisations/{cancellationId} | /v1/{payment-service}/{payment-product}/{paymentId}/cancellation-authorisations/{cancellationId} |

Also from now on parameters `creditorAgent` and `country`(part of `address`) in the request body are being validated.
Please ensure that `creditorAgent` is a valid BICFI identifier(e.g. `AAAADEBBXXX`) and that `country` is a 2 character ISO 3166 country code(e.g. `SE`).

**Please note that in this release some problems with errors responses appear. They are will be fixed in the next version.**
