# Release notes v. 1.17

## Bugfix: not able to retrieve the payment by redirect id when psu-id is not set in the initial request
When retrieving the payment by redirect id (endpoint GET /psu-api/v1/pis/consent/redirects/{redirect-id} in consent-management) now there is no need
to provide psu data and the payment can be retrieved without it, only by redirect id and instance id.

This also applies for getting payment for cancellation (endpoint GET /psu-api/v1/payment/cancellation/redirect/{redirect-id} in consent-management)

## Bugfix: Remove PSU Data from endpoint for getting consent by redirect id in CMS-PSU-API
PSU Data is no longer required for getting consent by redirect id.
As a result, headers `psu-id`, `psu-id-type`, `psu-corporate-id` and `psu-corporate-id-type` are no longer used in `psu-api/v1/ais/consent/redirect/{redirect-id}` endpoint.
PsuIdData was also removed as an argument from `de.adorsys.psd2.consent.psu.api.CmsPsuAisService#checkRedirectAndGetConsent` method.

## Delete old 'tpp demo' 
Deleted extra functional in 'tpp-demo' package

## Support of multiple SCA approaches
ASPSP can support several SCA approaches, so now XS2A Interface supports multiple SCA approaches also.
ASPSP profile was extended with list of approaches (in order of priority - first one with the highest priority) instead of one single approach.

| Option                                   | Meaning                                                                                                | Default value | 
|------------------------------------------|--------------------------------------------------------------------------------------------------------|---------------|
| scaApproaches                            | List of SCA Approach supported by ASPSP ordered by priority                                            |  - REDIRECT   |
|                                          |                                                                                                        |  - EMBEDDED   |
|                                          |                                                                                                        |  - DECOUPLED  |

Choice of SCA approaches depends on header parameter in payment,consent or signing basket initiation request - `TPP-Redirect-Preferred`. For Payment Cancellation parameter "TPP-Redirect-Preferred" is not used.
If `TPP-Redirect-Preferred` is true and ASPSP supports REDIRECT approach, then `REDIRECT` approach is used. Otherwise first approach in ASPSP-profile option `scaApproaches` is used.

## Bugfix: remove discrepancies between not null constraints in migration files and constraints in Java classes
There were some discrepancies between not null constraints specified in liquibase migration files and constraints specified in Java classes that were removed.
Now tables created via generated DDL should have the same constraints as the tables created via liquibase.

Attention: if you've bypassed the CMS and inserted some records into `pis_payment_data` table with `common_payment_id` 
column set to `null` directly, you'll have to assign some value to this column before updating the database.

## Provide endpoint to export AIS consents by aspspAccountId from CMS to ASPSP
By accessing endpoint GET `/aspsp-api/v1/ais/consents/account/{account-id}` (or corresponding method in `CmsAspspAisExportService.java`) 
one can export AIS Consents that contain certain account id. Method `exportConsentsByAccountId` now implemented and working.

## Implemented java interface and endpoint to save Account Access object in Consent by Online banking
Now by accessing `/psu-api/v1/ais/consent/{consent-id}/save-access` endpoint(or corresponding method in `CmsPsuAisService.java`)
online banking can update AccountAccess (along with `aspspAccountId` and `resourceId` if necessary), frequency per day and expiration date in consent.

## Bugfix: Additional payment products work only with "pain-" prefix

From now on SPI Developer may define any payment product they would like to support.
Only four payment products are considered as "standard" and processed in XS2A with the standard flow:
* sepa-credit-transfers
* instant-sepa-credit-transfers
* target-2-payments
* cross-border-credit-transfers

Any other payment product will be processed as byte-array to SPI level. SPI Developer needs to handle the payload
in SPI Connector.
Right now only content types `application/json` and `application/xml` are supported (support of `multipart/form-data`
is provided additionally to setup PAIN periodic payments).
Support of content type `text/plain` is planned.

## Bugfix: added missing ais_consent fields
Now while sending POST request to the `/v1/consents` endpoint the fields "availableAccounts" and "allPsd2" are persisted to the ais_consent table. Also they are available while calling the connector.

## Changed response class for cancellation authorisation
Now when sending GET request to the `/v1/{payment-service}/{paymentId}/cancellation-authorisations` the result is CancellationList class, which is actually a list of IDs.

## Added support of Spring Data 2.x
In order to use Spring Data 2.x in CMS developer now shall use a dependency to help-module `spring-boot-2.x-support`
```xml
        <dependency>
            <groupId>de.adorsys.psd2</groupId>
            <artifactId>spring-boot-2.x-support</artifactId>
            <version>1.17-SNAPSHOT</version>
        </dependency>

```
By default `spring-boot-1.5.x-support` is used.

## Bugfix: removed duplicated links in xs2a responses
Previously in xs2a responses we had two blocks of links with the same content, but different namings (`links` and `_links`).
Now xs2a interface provides only one block of links.

## Implement interface for exporting PIIS consents from CMS to ASPSP
Provided implementation for Java interface `de.adorsys.psd2.consent.aspsp.api.piis.CmsAspspPiisFundsExportService` 
that allows exporting PIIS consents by ASPSP account id, TPP ID and PSU ID Data.

From now on these endpoints are fully functional:

| Method | Endpoint                                         | Description                                                                                               |
|--------|--------------------------------------------------|-----------------------------------------------------------------------------------------------------------|
| GET    | /aspsp-api/v1/piis/consents/account/{account-id} | Returns a list of consents by given mandatory aspsp account id, optional creation date and instance ID    |
| GET    | /aspsp-api/v1/piis/consents/psu                  | Returns a list of consents by given mandatory PSU ID Data, optional creation date and instance ID         |
| GET    | /aspsp-api/v1/piis/consents/tpp/{tpp-id}         | Returns a list of consents by given mandatory TPP ID, optional creation date, PSU ID Data and instance ID |

## Bugfix: fix instanceId not being set in the CMS that doesn't have any pre-insert listeners
From now on `instanceId` property will be correctly set to its default value(`UNDEFINED`) when the CMS doesn't have any pre-insert listeners to override this property. 

## Bugfix: Error messages from spi-api should be returned to the tpp in response
Now messages that are provided in spi-api in case of the error will be returned to the tpp in response

## Implemented Decoupled SCA approach
From now on XS2A supports Decoupled SCA approach for authorising account consents, payments and payment cancellations.
It occurs in the following cases:
 * if `DECOUPLED` SCA approach was chosen by ASPSP
 * during the `EMBEDDED` SCA approach if decoupled SCA method was chosen by PSU during selection of SCA methods

New method `de.adorsys.psd2.xs2a.spi.service.AuthorisationSpi#startScaDecoupled` was added to the SPI interface to be implemented by SPI developers.
The response of this method should contain the message, shown to the PSU, containing recommendation to proceed the authorisation via the dedicated mobile app.

`authorisationId`, provided in this method shall be used as `redirectId` to finish authorisation in the App, by accessing corresponding endpoints in CMS-PSU-API

An SPI Developer now shall consider also Flag `decoupled` in `de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthenticationObject` (defaults to `false`) returend by
method `requestAvailableScaMethods`.
If it is set to `true` and PSU chooses this authentication method, the SCA approach will be switched to `DECOUPLED`

## Added a possibility to require PSU-ID in initial request for payment initiation or establishing consent
Now ASPSP can forbid initiating payment or establishing AIS consent without PSU-ID by setting option `psuInInitialRequestMandated` to `true` in ASPSP Profile.

| Option                      | Meaning                                                                                                      | Default value |
|-----------------------------|--------------------------------------------------------------------------------------------------------------|---------------|
| psuInInitialRequestMandated | This field indicates if ASPSP requires PSU in initial request for payment initiation or establishing consent | false         | 

## Added additional step to identify PSU if TPP doesn’t send PSU-ID in authorisation
When TPP creates authorisation for AIS consent, payment or payment cancellation without PSU-ID, 
`startAuthorisationWithPsuIdentification` link will be returned, using which TPP should upload the PSU identification data.
At  this stage if TPP doesn’t send PSU-ID, there will be FORMAT_ERROR with http status 400. 
If PSU-ID was sent, authorisation status will be changed to `PSUIDENTIFIED` and TPP will get link 
`startAuthorisationWithPsuAuthentication` using which PSU authentication data should be uploaded.
After PSU identified itself, there is no need to send PSU-ID in next requests to make authorisation finalised.

## Bugfix: Fixed problem with wrong payment service (payment product) for Get payment status, Get payment information and Cancel payment

When using GET `/v1/{payment-service}/{payment-product}/{paymentId}/status`, GET `/v1/{payment-service}/{payment-product}/{paymentId}` or DELETE `/v1/{payment-service}/{payment-product}/{paymentId}/` 
with incorrect payment service(e.g `periodic-payments` instead of `payments` with payment id of single payment, not periodic, or `instant-sepa-credit-transfers` instead of `sepa-credit-transfers`),
there were no correct errors provided (`405 SERVICE_INVALID` for incorrect payment service and `403 PRODUCT_INVALID` for incorrect payment product).

Now when you enter incorrect payment service, the request will not be executed and the error `405 SERVICE_INVALID` will be returned.
If you enter incorrect payment product, the request also will not be executed and the error `403 PRODUCT_INVALID` will be returned.

## Removed unused enumerator value
According to the Implementation Guidelines version 1.3 the value `AVAILABLE` of the Balance type doesn't exist. It is removed from SpiBalanceType and AspspBalanceType.

## Bugfix: fix error on AIS consent confirmation when using OracleDB
Trying to confirm AIS consent in CMS that uses OracleDB will no longer fail.
In order to fix this error the type of `authority_id` column in `tpp_info` table was changed to `VARCHAR(255)`.

Beware: during migration of existing records in `tpp_info` table only the first `255` characters of `authority_id` 
will be retained, all exceeding characters will be lost.
