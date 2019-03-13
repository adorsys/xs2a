# Release notes v.2.1

## Event table extended with new searchable columns
From now on XS2A Event object (`de.adorsys.psd2.xs2a.core.event.Event`) contains the following fields:
 * PSU Data
 * TPP authorisation number
 * X-Request-Id

Also corresponding columns (`psu_id`, `psu_id_type`, `psu_corporate_id`, `psu_corporate_id_type`, `tpp_authorisation_number`, `x_request_id`) were added to the `event` table in the CMS database.
Also pay attention that property `requestId` in Event payload object was deleted as duplicated field.

## Bugfix: Now property `validUntil` in AIS consent creation request is inclusive
From now on the property `validUntil` is inclusive. It means that TPP is able to create AIS consent with current date in `validUntil` property and 
can access the consent until the end of the day.

Consent will get expired on the next day after the `validUntil` date.

## Adjust validUntil date according to consentLifetime property (set in ASPSP-profile) during creating AIS consent
When TPP sends AIS consent creation request, `validUntil` field may be adjusted according to `consentLifetime` property (from ASPSP-profile)
Following rules are used to adjust `validUntil` date:
 * if `consentLifetime` = 0 - no adjustments will be applied
 * if `consentLifetime` > 0 - ASPSP calculates maximum allowed `validUntil` date for AIS consent as current date plus value of `consentLifetime` property. 
If requested date exceeds the allowed one, the consent will be created with `validUntil` property set to maximal allowed date, otherwise no adjustments will be applied.

## Added authorisation type to response for getting PSU data authorisations

Now these endpoints: `/v1/payment/{payment-id}/authorisation/psus` and `/v1/ais/consent/{consent-id}/authorisation/psus` have enriched
responses with new field added - `authorisationType`. The value can be `CREATED` or `CANCELLED` by now.
