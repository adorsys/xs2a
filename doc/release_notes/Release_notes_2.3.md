# Release notes v.2.3

## Bugfix: Fix error code on trying to read transaction list with invalid expired consent
From now on, executing Read Transaction List request (`GET /v1/accounts/{account-id}/transactions`) or Read Transaction 
Details request (`GET /v1/accounts/{account-id}/transactions/{resourceId}`) with consent that doesn't have access to 
transaction and is already expired will result in `401 CONSENT_INVALID` error instead of `401 CONSENT_EXPIRED`.

## Bugfix: SCA status EXEMPTED should be a finalised status
From now on, SCA status `EXEMPTED` is considered to be a finalised status. It means that once the authorisation status 
is changed to `EXEMPTED`, it will no longer be possible to change the status of this authorisation.
