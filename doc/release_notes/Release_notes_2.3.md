# Release notes v.2.3

## Bugfix: Fix error code on trying to read transaction list with invalid expired consent
From now on, executing Read Transaction List request (`GET /v1/accounts/{account-id}/transactions`) or Read Transaction 
Details request (`GET /v1/accounts/{account-id}/transactions/{resourceId}`) with consent that doesn't have access to 
transaction and is already expired will result in `401 CONSENT_INVALID` error instead of `401 CONSENT_EXPIRED`.

## Bugfix: SCA status EXEMPTED should be a finalised status
From now on, SCA status `EXEMPTED` is considered to be a finalised status. It means that once the authorisation status 
is changed to `EXEMPTED`, it will no longer be possible to change the status of this authorisation.

## Bugfix: Fix error with detecting SCA approach based on "tpp-redirect-preferred" header
From now on, the following rules are applied to choose SCA Approach from the list of available SCA Approaches in ASPSP-profile:
 * If header "tpp-redirect-preferred" is provided with value "true" and ASPSP supports Redirect approach, then this approach will be used.
 * If header "tpp-redirect-preferred" is provided with value "false", the first non-Redirect approach from the list will be used.
 * If header "tpp-redirect-preferred" is not provided, the first approach from the list will be chosen.
 * If ASPSP has only one SCA approach in profile, header "tpp-redirect-preferred" will be ignored and only approach from profile will be used

## Bugfix: links building while getting account list and account details
Implemented the new mechanism for links generation in the `getAccoutList` request (GET `/v1/accounts/{withBalance}`) and 
`readAccountDetails` request (GET `/v1/accounts/{account-id}/{withBalance}`).
Currently these links depend on the consent and its accesses: if the consent has only accounts access - the links are 
not provided. If the consent has balances access - balances link is present and for the transactions access - transactions
link is given. Boolean path parameter `withBalance` influences only the responses for these requests and has nothing common
with the links.

## Bugfix: Fix update PSU Data without unnecessary PSU_ID in header
From now on, `update PSU Data` request for selecting SCA method doesn’t need to contain PSU-ID header and will be processed correctly even without it.
