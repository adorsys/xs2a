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

## Added transaction status for PIS

Now endpoints for exporting payments (`/aspsp-api/v1/pis/payments/tpp/{tpp-id}`, `/aspsp-api/v1/pis/payments/psu`, `/aspsp-api/v1/pis/payments/account/{account-id}`)
and for getting payment by ID (`/psu-api/v1/payment/{payment-id}`) store the transaction status of the payment in the response. For example, when ASPSP wants
to get payment by its ID - it will receive the transaction status among the other fields.

## Payment and consent CMS endpoints return creation datetime in the response

From now CMS `/psu-api/v1/ais/`, `/psu-api/v1/payment/`, `/aspsp-api/v1/ais/` and `/aspsp-api/v1/pis/` endpoints 
contain payment/consent creation datetime in the response.

## Call Spi for ConsentStatus on GET Consent request
From now on, SPI Developer may implement logic for method `getConsentStatus` in `AisConsentSpi` interface.
Requests get consent status (for AIS) call SPI level.
If consent status in CMS is finalised (REJECTED, REVOKED_BY_PSU, EXPIRED, TERMINATED_BY_TPP, TERMINATED_BY_ASPSP), call to SPI won't be performed.

## Links are generated using internal URL
From now on, it is possible to generate links using internal URL, defined in the ASPSP profile, as `xs2aBaseUrl` property. To enable this feature, `forceXs2aBaseUrl` property
in ASPSP profile should be set to `true`.

## Bugfix: New step in consent creation validation

From now on the consent creation request has the new validation: if consent has both the account accesses list AND the flag (`allPsd2` or `availableAccounts` or both) - 
the `400 Bad Request` response is returned.  

## Bugfix: IBAN validation in payment creation

From now on the IBAN codes are validated during the payment initiation requests. In case of wrong IBAN the response is `400 FORMAT_ERROR`. Note, that
IBAN is validated by checking its length, country code and checksum.
