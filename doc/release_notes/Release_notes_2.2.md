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
