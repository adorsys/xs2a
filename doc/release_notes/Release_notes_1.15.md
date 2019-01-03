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
