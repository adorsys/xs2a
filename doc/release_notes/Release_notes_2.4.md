# Release notes v.2.4

## Bugfix: Fix update authorisation status endpoint in CMS-PSU-API not working with lowercase values for authorisation status
From now on, CMS-PSU-API endpoints for updating authorisation status for payments 
(`PUT /psu-api/v1/payment/{payment-id}/authorisation/{authorisation-id}/status/{status}`) and consents 
(`PUT /psu-api/v1/ais/consent/{consent-id}/authorisation/{authorisation-id}/status/{status}`) will work correctly with
lowercase status values. Also trying to provide an invalid status value will result in `400 Bad Request` error being 
returned instead of `500 Internal Server Error`.

## Bugfix: Fixed process of creating PIIS consent for the same account, PSU and TPP
From now on, PIIS consent is dedicated for one account and only one consent can be `VALID` for the same account, PSU and TPP.
On new consent creation, previous PIIS consents with the same parameters will change status to `REVOKED_BY_PSU`.
To create PIIS consent (`POST /aspsp-api/v1/piis/consents/`) provide account in `account` field instead of using `accounts` field for several accounts.
When retrieving PIIS consent, `account` field will represent actual account of the consent or first created account for previously created PIIS consents with several accounts.
Also starting from this version ASPSP is not able to create PIIS consent without TPP. Previous PIIS consents without concrete TPP are closed.

## Delete column `usage_counter` from table `ais_consent`
Deprecated column `usage_counter` was removed from table `ais_consent`

## Bugfix: Separate links for start authorisation and create consent and initiate payment
From now on, response has corrected Links:
- when authorization sub-resource is already created then response contains link `updatePsuIdentification` (if PSU-ID is not given in request)
and `updatePsuAuthentication` (if PSU-ID is given in request).
- when authorization sub-resource is not created yet and no more additional data should be expected from PSU then response contains link `startAuthorisation`
- when authorization sub-resource is not created yet and some additional data should be expected from PSU then response contains link `startAuthorisationWithPsuIdentification` (if PSU-ID is not given in request)
and `startAuthorisationWithPsuAuthentication` (if PSU-ID is given in request).

## Feature: Validation for payment initiation and consent creation
From now on, the endpoints for payment initiation and consent creation
(POST `/v1/{payment-service}/{payment-product}`, POST `/v1/consents`) have multi-layered validation:
- HTTP headers and bodies are validated before the initial request reaches the Spring RestController layer (interceptor is used);
- Business validation is implemented after the RestController layer.

First stage check the format of headers and fields and returns the response with the list of human-readable errors.
This response always has `400 FORMAT ERROR` HTTP code in case of any errors. If any new error occurs - it is added to
the list. The second stage (business validation) is launched only after first stage succeeded and returns the appropriate
HTTP code and error information according to the documentation. 

## Bugfix: Fix global consent creation returning wrong error code
From now on, attempting to create global consent(i. e. consent that contains only `"allPsd2": "allAccounts"` in its 
`access` property) with `allPsd2Support` property in ASPSP profile set to `false` will result in `405 SERVICE_INVALID` 
error( instead of previous `400 PARAMETER_NOT_SUPPORTED`) being returned in the response.

## Bugfix: Initial SCA-Status 'started' is changed to 'received'
From now on, all authorisations (AIS and PIS) are created with `RECEIVED` ScaStatus instead of 'STARTED'. 
