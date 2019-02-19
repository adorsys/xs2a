# Release notes v. 2.0

## String field `psuId` changed to object `PsuIdData` for `GetPisAuthorisationResponse` and `AisConsentAuthorizationResponse`      

Full PSU data (PSU Id together with PSU corporate ID, PSU ID type and PSU corporate ID type) now used in `GetPisAuthorisationResponse` and `AisConsentAuthorizationResponse`
in consent-management instead of psu id only. Full PSU data is transmitted and stored in consent-management. No changes to SPI-level are needed.

## SCA approach stored in the authorisation

AIS and PIS authorisations (entity and table) were extended to store SCA approach. 
SCA approach value is saved during the authorisation creation and could be updated to a new value in case of switching from Embedded to Decoupled SCA approaches.

## Embedded authorisation endpoints are not accessible in redirect/decoupled

From now, embedded authorisation endpoints are accessible during the embedded SCA approach only.
If such endpoints are called in case of, for example, REDIRECT SCA approach, 403 error response will be returned.
In case of switching from Embedded to Decoupled SCA approaches endpoints become to be non-accessible also.
