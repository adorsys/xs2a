# Release notes v.2.5

## Deleted deprecated method createConsent in CmsAspspPiisService
Method `de.adorsys.psd2.consent.aspsp.api.piis.CmsAspspPiisService#createConsent(PsuIdData, TppInfo, List, LocalDate, int)` was removed,
use `CmsAspspPiisService#createConsent(PsuIdData, CreatePiisConsentRequest)` instead.

## New mechanism for counting frequencyPerDay
From now on, we count the number of consent usages by every endpoint:

- /accounts
- /accounts/account-id per account-id
- /accounts/account-id/transactions per account-id
- /accounts/account-id/balances per account-id
- /accounts/account-id/transactions/transaction-id per account-id and transaction-id, if applicable.

If the amount of accesses for any of these endpoint is exceeded - the `429 ACCESS_EXCEEDED` is returned. All other
endpoints are still accessible until their amount is not exceeded.

Also, the `usageCounter` field in `AisAccountConsent` is deprecated - now the new field `usageCounterMap` should be used
instead. It is a map: key is the endpoint, value is a number of its usage. The following services were affected by this
change:

  - In consent-aspsp-api:
    - `de.adorsys.psd2.consent.aspsp.api.ais.CmsAspspAisExportService`
    (`GET /aspsp-api/v1/ais/consents/tpp/{tpp-id}`, `GET /aspsp-api/v1/ais/consents/psu`, `GET /aspsp-api/v1/ais/consents/account/{account-id}`)
  - In consent-psu-api:
    - `de.adorsys.psd2.consent.psu.api.CmsPsuAisService`
    (`GET /psu-api/v1/ais/consent/{consent-id}`, `GET /psu-api/v1/ais/consent/consents`)

## Bugfix: Wrong error code "requestedExecutionDate" value in the past
Error code was changed to `400 EXECUTION_DATE_INVALID` from `400 FORMAT_ERROR` when `requestedExecutionDate` field is less then current date.
