# Release notes v.2.0.1

## Bugfix: Fixed the process of checking daily access limit for AIS consent
From now on when TPP exceeds allowed frequency per day for AIS consent, it will receive response with `ACCESS_EXCEEDED` error (response code HTTP 429).

## Bugfix: Prevent incorrect modifications of allowed accesses in AIS consent after executing get accounts request
Executing get accounts request will no longer incorrectly provide accesses to balances and transactions in AIS consent 
if no such accesses were granted beforehand.
