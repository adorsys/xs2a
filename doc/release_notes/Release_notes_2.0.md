# Release notes v. 2.0

## String field `psuId` changed to object `PsuIdData` for `GetPisAuthorisationResponse` and `AisConsentAuthorizationResponse`      

Full PSU data (PSU Id together with PSU corporate ID, PSU ID type and PSU corporate ID type) now used in `GetPisAuthorisationResponse` and `AisConsentAuthorizationResponse`
in consent-management instead of psu id only. Full PSU data is transmitted and stored in consent-management. No changes to SPI-level are needed.
