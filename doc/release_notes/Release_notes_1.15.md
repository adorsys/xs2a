# Release notes v. 1.15

## Get SCA Status Request
Endpoints for getting the SCA status of the authorisation were implemented.
Available endpoints are listed below.

| Context                             | Method | Endpoint                                                                        | Description                                                         |
|-------------------------------------|--------|---------------------------------------------------------------------------------|---------------------------------------------------------------------|
| Payment Initiation Request          | GET    | /v1/{payment-service}/{paymentId}/authorisations/{authorisationId}              | Checks the SCA status of a authorisation sub-resource.              |
| Payment Cancellation Request        | GET    | /v1/{payment-service}/{paymentId}/cancellation- authorisations/{cancellationId} | Checks the SCA status of a cancellation authorisation sub-resource. |
| Account Information Consent Request | GET    | /v1/consents/{consentId}/authorisations/{authorisationId}                       | Checks the SCA status of a authorisation sub-resource.              |

## Bugfix: validating PSU identification during updates PSU data requests
Now PSU identification is validating in next services:
- PaymentAuthorisationSpi
- PaymentCancellationSpi
- AisConsentSpi

If PSU can not be matched by the addressed ASPSP or is blocked, or a password resp. OTP was not correct, TPP will receive 
HTTP 401 response code with message code "PSU_CREDENTIALS_INVALID". 
