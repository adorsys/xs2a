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
