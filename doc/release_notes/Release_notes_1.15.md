# Release notes v. 1.15

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
Now to encrypt aspspConsentData in SecurityDataService we should provide byte array as an argument instead of Base64 encoded string

## Add instanceId to services in cms-aspsp-api and cms-psu-api
From now methods in cms-aspsp-api and cms-psu-api also require instanceId to be provided as a mandatory argument.
This id represents particular service instance and is used for filtering data from the database.

All corresponding CMS endpoints were also updated and from now on support instanceId as an optional header. 
If the header isn't provided, default value `UNDEFINED` will be used instead.

The following services were affected by this change:
  - In consent-aspsp-api:
    - de.adorsys.psd2.consent.aspsp.api.ais.CmsAspspAisExportService
    - de.adorsys.psd2.consent.aspsp.api.piis.CmsAspspPiisService
    - de.adorsys.psd2.consent.aspsp.api.pis.CmsAspspPisExportService
  - In consent-psu-api:
    - de.adorsys.psd2.consent.psu.api.CmsPsuAisService
    - de.adorsys.psd2.consent.psu.api.CmsPsuPiisService
    - de.adorsys.psd2.consent.psu.api.CmsPsuPisService

## Bugfix: Embedded SCA Approach is not supported for Bank Offered Consent
Now Bank Offered Consent is not supported for Embedded SCA Approach.

If ASPSP doesn't support Bank Offered Consent then TPP will receive HTTP 405 response code with message code "SERVICE_INVALID" for any approach, instead of "PARAMETER_NOT_SUPPORTED"(HTTP 400 response code)

## Implement interfaces for exporting consents/payments from CMS
Implementations for Java interfaces de.adorsys.psd2.consent.aspsp.api.ais.CmsAspspAisExportService and
 de.adorsys.psd2.consent.aspsp.api.pis.CmsAspspPisExportService were provided.

Corresponding endpoints were also added to CMS, they are listed in the table below.

| Method | Endpoint                               | Description                                                                                                          |
|--------|----------------------------------------|----------------------------------------------------------------------------------------------------------------------|
| GET    | aspsp-api/v1/ais/consents/tpp/{tpp-id} | Returns a list of AIS consent objects by given mandatory TPP ID, optional creation date, PSU ID Data and instance ID |
| GET    | aspsp-api/v1/ais/consents/psu          | Returns a list of AIS consent objects by given mandatory PSU ID Data, optional creation date and instance ID         |
| GET    | aspsp-api/v1/pis/payments/tpp/{tpp-id} | Returns a list of payments by given mandatory PSU ID Data, optional creation date and instance ID.                   |
| GET    | aspsp-api/v1/pis/payments/psu          | Returns a list of payments by given mandatory TPP ID, optional creation date, PSU ID Data and instance ID.           |

## Obsoleting Consents that were not confirmed
Now ASPSP developer is able to provide the period of time (in milliseconds),that not confirmed AIS consents should be obsoleted after.
The default value for this is 24 hours.
Consent status becomes EXPIRED and SCA status for dedicated consent authorisation becomes FAILED on such a requests:
* TPP sends Get consent status request
* xs2a receives start authorisation request for this consent
* TPP sends Get SCA status request
* xs2a receives Update PSU Data request for this payment

Also, scheduler service has been created: it will obsolete all the AIS consents with confirmation expired.
The scheduler service invocation frequency could be modified by changing `not-confirmed-consent-expiration.cron.expression` value in `application.properties`.
The default value is the top of every hour of every day.

## Obsoleting Payments that were not confirmed
Now ASPSP developer is able to provide the period of time (in milliseconds),that not confirmed payments should be obsoleted after.
The default value for this is 24 hours.
Transaction status becomes REJECTED and SCA status for dedicated payment authorisation becomes FAILED on such a requests:
* TPP sends Get transaction status request
* xs2a receives start authorisation request for this payment
* TPP sends Get SCA status request
* xs2a receives Update PSU Data request for this payment

Also, scheduler service has been created: it will obsolete all the payments with confirmation expired.
The scheduler service invocation frequency could be modified by changing `not-confirmed-payment-expiration.cron.expression` value in `application.properties`.
The default value is the top of every hour of every day.

## Upgrade version of Jackson library
We updated Jackson version because FasterXML jackson-databind 2.x before 2.9.8 might allow attackers to have unspecified impact by leveraging failure to block the axis2-transport-jms class from polymorphic deserialization. https://nvd.nist.gov/vuln/detail/CVE-2018-19360

## Aspsp-Profile supports matrix payment-product/payment-type

ASPSP now has a possibility to chose which payment-product/payment-type to work with. Now to set available payment products for each type, the following table in bank_profile.yaml
should be filled:

**supportedPaymentTypeAndProductMatrix**:

  *SINGLE*:
   - sepa-credit-transfers
   - instant-sepa-credit-transfers
   
  *PERIODIC*:
   - sepa-credit-transfers
   - instant-sepa-credit-transfers
   
  *BULK*:
   - sepa-credit-transfers
   - instant-sepa-credit-transfers
  
Other payment products can be added for every payment type.

## One active authorisation per consent for one PSU
When PSU creates new authorisation for consent, all previous authorisations, created by this PSU for the same consent, will be failed and expired.

## Added expiration time for payment cancellation redirect url
A new `paymentCancellationRedirectUrlExpirationTimeMs` parameter has been added to ASPSP profile.

| Option                                         | Meaning                                                                                                          | Default value | Possible values         |
|------------------------------------------------|------------------------------------------------------------------------------------------------------------------|---------------|-------------------------|
| paymentCancellationRedirectUrlExpirationTimeMs | This field contains the limit of an expiration time of redirect url for payment cancellation set in milliseconds | 600 000       | milliseconds (1, 2,...) |

Payment cancellation redirect url and related authorisation now have an expiration time. The value for expiration time is counted with formula 
"current time of authorisation creation + payment cancellation redirect url expiration time (set in ASPSP-profile)". 
We give redirect id (= authorisation id) in redirect link now, and to get payment information, online banking should call 
 **GET /psu-api/v1/pis/consent/redirects/cancellation/{redirect-id}** endpoint of consent management system.
If redirect url is not expired, online banking gets payment, authorisation id, not ok tpp redirect url and ok tpp redirect url (for now these urls are null temporary) in response, otherwise http code 408 Request Timeout is sent.
