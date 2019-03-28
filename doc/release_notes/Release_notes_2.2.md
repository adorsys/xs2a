# Release notes v.2.2

## Bugfix: Add missing links and `Location` header to the AIS consent creation response
From now on, response to the AIS consent creation request(`POST /v1/consents`) contains previously missing `Location` 
header and the `self` link. Also this response now contains `status` and `scaStatus`(in case of `implicit` start of the 
authorisation process) links that were missing in `EMBEDDED` approach.

## PIIS consent was extended with new properties
The following fields were added to PIIS consent (`de.adorsys.psd2.xs2a.core.piis.PiisConsent`):
| Name                    | Description                                                                                                   |
|-------------------------|---------------------------------------------------------------------------------------------------------------|
| cardNumber              | Card Number of the card issued by the PIISP. Should be delivered if available.                                |
| cardExpiryDate          | Expiry date of the card issued by the PIISP                                                                   |
| cardInformation         | Additional explanation for the card product.                                                                  |
| registrationInformation | Additional information about the registration process for the PSU, e.g. a reference to the TPP / PSU contract |

Also these fields were added to `de.adorsys.psd2.consent.aspsp.api.piis.CreatePiisConsentRequest` and can be used for PIIS consent creation in CMS-ASPSP-API 
From now on method de.adorsys.psd2.consent.aspsp.api.piis.CmsAspspPiisService#createConsent(PsuIdData, TppInfo, List, LocalDate, int) is deprecated,
use CmsAspspPiisService#createConsent(PsuIdData, CreatePiisConsentRequest)instead.

## feature: added date and time of last status changing in PIS, AIS and PIIS
Added new columns with timestamps to CMS tables `ais_consent`, `pis_common_payment` and `piis_consent`. Timestamp of the
last status changing is stored, if the entity is created - this timestamp is the same as entity creation timestamp. 

The responses for the listed below endpoints are enriched with the proper field - `statusChangeTimestamp`:
 - GET `/aspsp-api/v1/pis/payments/tpp/{tpp-id}`
 - GET `/aspsp-api/v1/pis/payments/psu`
 - GET `/aspsp-api/v1/pis/payments/account/{account-id}`
 - GET `/aspsp-api/v1/ais/consents/tpp/{tpp-id}`
 - GET `/aspsp-api/v1/ais/consents/psu`
 - GET `/aspsp-api/v1/ais/consents/account/{account-id}`
 - GET `/aspsp-api/v1/piis/consents/tpp/{tpp-id}`
 - GET `/aspsp-api/v1/piis/consents/psu`
 - GET `/aspsp-api/v1/piis/consents/account/{account-id}`

Also, this field is propagated to the SPI level for PIS, AIS and PIIS. 

## Bugfix: X-Request-ID header validation
Added validation for the `X-Request-ID` header in all controllers. Two cases are handled by now:
 - Header is null. In this case response is `400 FORMAT ERROR` with the text `'X-Request-ID' may not be null`
 - Header is not UUID (wrong format). In this case response is also `400 FORMAT ERROR`, the text is `'X-Request-ID' 
 has to be represented by standard 36-char UUID representation`

## Bugfix: All links must be contained in an object with `href`

From now on, all Links in the response in the _links object contain an object with `href`. E.g.
`"scaRedirect": {"href": "https://www.testbank.com/asdfasdfasdf"}`

## Bugfix: search for AIS and PIIS consent now use all PSU Data properties in getConsentsForPsu method
From now on in CmsAspspPiisService#getConsentsForPsu, CmsPsuPiisService#getConsentsForPsu and CmsPsuAisService#getConsentsForPsu 
all PSU Data properties are used for filtering results. 

## Bugfix: TPP validation in AIS and PIS
From now on XS2A will check every request to AIS and PIS endpoints that contains consent ID or payment ID (i. e. every 
request, except `POST /v1/payments/{payment-product}` for payments and `POST /v1/consents` for consents) and verify that 
the request comes from the same TPP that created given consent or payment. TPP in requests are considered to be equal if 
they've used certificates that contain the same authorisation number and authority id.
If there is a mismatch, `401 Unauthorized` error will be returned in the response.

## Bugfix: Update PSU data in consent authorisation
From now on CMS will update PSU data in authorisation while calling the `/psu-api/v1/ais/consent/{consent-id}/authorisation/{authorisation-id}/psu-data`
endpoint.

## Bugfix: Check whether consent has access to balances in Read account list request with balances
From now on XS2A will validate Read account list request (`GET /v1/accounts`) with query parameter `withBalance` set to 
`true` by checking whether given consent has access to balances. If the consent has no such access (i. e. consent's `access` 
property doesn't contain `balances`), `401 CONSENT_INVALID` error will be returned in the response.
