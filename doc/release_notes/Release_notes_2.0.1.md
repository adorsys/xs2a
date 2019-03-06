# Release notes v.2.0.1

## Bugfix: Fixed the process of checking daily access limit for AIS consent
From now on when TPP exceeds allowed frequency per day for AIS consent, it will receive response with `ACCESS_EXCEEDED` error (response code HTTP 429).

## Bugfix: Prevent incorrect modifications of allowed accesses in AIS consent after executing get accounts request
Executing get accounts request will no longer incorrectly provide accesses to balances and transactions in AIS consent 
if no such accesses were granted beforehand.

## Bugfix: changed the response code for getting payment and its status by ID with wrong payment service      
Before the response for getting payment details by its ID with wrong payment service in path (GET `/v1/wrong-payment-service/sepa-credit-transfers/payment_id`)
was returning `400 - Bad Request`. The same was in getting payment status (GET `/v1/wrong-payment-service/sepa-credit-transfers/payment_id/status`).
From now on new response is: `404 - Resource Unknown`.

## Bugfix: Frequency per day in AIS consent can accept only positive values
From now on value of the `frequencyPerDay` field in AIS consent is being validated and can only contain positive values 

## Bugfix: made Tpp-Redirect-Uri to be mandatory for TPP-Redirect-Preferred == true case
From now, during the process of AIS consent creation and payment initiation, 
if `TPP-Redirect-Preferred` header is equal to `true`, the `TPP-Redirect-Uri` header is mandatory. 
In case of missing `TPP-Redirect-Uri` header, `400` HTTP error code will be returned.

## Bugfix: Change error code returned when access to the particular resources is not allowed for given AIS consent
From now on error `CONSENT_INVALID` (HTTP response code `401`) will be returned instead of `RESOURCE_UNKNOWN` 
(HTTP response code `404`) if the access to particular resources is not allowed for given AIS consent.

Affected endpoints:
 - Read Account Details (`GET /v1/accounts/{account-id}`)
 - Read Balance (`GET /v1/accounts/{account-id}/balances`)
 - Read Transaction List (`GET /v1/accounts/{account-id}/transactions`)
 - Read Transaction Details (`GET /v1/accounts/{account-id}/transactions/{resourceId}`)

## Bugfix: added validation for incorrect dates in periodic payments creation      
Now while creating new periodic payment its start date and end date are validated:
 - start date can not be in the past
 - end date can not be earlier than start date
Also while creating the future payment its execution date is validated and it can not be in the past.
In all above cases response with error `PERIOD_INVALID` (response code HTTP 400) is returned.
