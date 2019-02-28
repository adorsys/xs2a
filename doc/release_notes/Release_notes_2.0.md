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

## Changed response class for funds confirmation request.

Fixed wrong response to POST request `/v1/funds-confirmations`, now returns the `InlineResponse200` response.

## Booking status is provided as parameter of AccountSpi.requestTransactionsForAccount

The bank service can filter the transactions using the bookingStatus (received as query parameter in `GET transaction list` request),
so Xs2a doesn't need to get all transactions from bank and filter them - ASPSP filters transactions itself (or SPI-connector developer can
choose a way of implementing the filtering on SPI level if ASPSP does not support filtering).

From now Xs2a does not filter transactions by given booking status. Booking status is provided to SPI level as a parameter of 
`AccountSpi.requestTransactionsForAccount`. There it could be either provided to ASPSP or used to filter transactions on SPI level.

In Xs2a - ASPSP-mock connector filtering is done on SPI level (in private methods `getFilteredTransactions` and
`filterByBookingStatus` in `AccountSpiImpl`).

## BookingStatus entity is moved to xs2a-core, Xs2aBookingStatus and SpiBookingStatus are deleted

From now on only one enum that represents booking status exists. `BookingStatus` is moved to `xs2a-core` package, duplicates 
`Xs2aBookingStatus` and `SpiBookingStatus` are deleted.

## List of PSU Data is provided in payment objects to all SPI methods

From now on, these SPI payment objects contain list of PSU Data:
 - SinglePaymentSpi
 - PeriodicPaymentSpi
 - BulkPaymentSpi
 - SpiPaymentInfo

## Bugfix: changed link in cancellation authorisation response

Fixed `scaStatus` link in response for starting authorisation POST request for the addressed payment cancellation.
Before fixing the link was: `/v1/{payment-service}/{payment-product}/{payment-id}/authorisations/{authorisation-id}`,
now it is: `/v1/{payment-service}/{payment-product}/{payment-id}/cancellation-authorisations/{authorisation-id}`.

## Several PSUs in AIS consent
Due to multilevel authorisation of consents, we can store data of several PSUs for each consent.

These changes also affect SPI level, meaning that from now on `SpiAccountConsent` contains list of `SpiPsuData` instead of a single object.

## Added support of payment initiation of any payment in `application/xml` or `text/plain` format

Xs2a payment initiation controller now supports initiation of payment in `application/xml` or `text/plain` format.
To enable this feature, a non-standard payment product should be added to aspsp-profile `supportedPaymentTypeAndProductMatrix`(standard payment products are the 4 ones, defined by the Berlin Group Specification 1.3).
The body of the payment is stored in the byte array and returned completely in the same form with get payment endpoint.
Get payment status endpoint returns JSON response with current transaction status of the payment.

## Bugfix: Removed duplicate transaction status in CMS for payment

Both `pis_common_payment` and `pis_payment_data` tables contained `transaction_status` column. This lead to inconsistencies.
Transaction status field is removed from `pis_payment_data` table.

## Split Swagger Documentation to CMS APIs to three Swagger Specifications
Now instead one annotation `@EnableCmsSwagger` there are three annotations:
* `@EnableCmsAspspApiSwagger`,
* `@EnableCmsPsuApiSwagger`,
* `@EnableCmsXs2aApiSwagger`.

They may be used independently or all together to provide 3 Swagger specifications (may be selected in top right corner of Swagger UI).

## AccountAccessType entity is moved to xs2a-core, AisAccountAccessType, SpiAccountAccessType and Xs2aAccountAccessType are deleted

From now on only one enum that represents account access type exists in xs2a. `AccountAccessType` is moved to `xs2a-core` package, duplicates 
`AisAccountAccessType`, `SpiAccountAccessType` and `Xs2aAccountAccessType` are deleted.

## Multilevel SCA required is stored in AIS consent

AIS consent (entity and table) was extended to store Multilevel SCA required (a `boolean` value). 
`SpiInitiateAisConsentResponse` was extended to contain `multilevelScaRequired`
Multilevel SCA required is saved in AIS consent during the consent initiation. 
The value is received from SPI as a part of SPI response payload (`SpiInitiateAisConsentResponse` type) on `AisConsentSpi#initiateAisConsent()` call. 

## Multilevel SCA for Establish Consent in Embedded approach

Support of multilevel SCA for AIS Embedded approach was added.
To make it work, `AisConsentSpi#verifyScaAuthorisation()` SPI Response payload type was changed from `VoidResponse` 
to `SpiVerifyScaAuthorisationResponse` (currently contains only one field - `ConsentStatus`). 
It has been done to provide the possibility for SPI to return the consent status when authorisation is finished.
We expect to receive a `PARTIALLY_AUTHORISED` consent status during the Multilevel SCA flow if authorisation is not finished by all PSUs.

Please, note: if AIS consent contains `multilevelScaRequired`, that equals to `false`, 
but `PARTIALLY_AUTHORISED` has been received as a part of `AisConsentSpi#verifyScaAuthorisation()` response payload, 
`multilevelScaRequired` value of AIS consent will be updated to `true` in DB.

## Feature: added endpoints for getting consents and payments SCA statuses

New endpoints were added to the CmsPsuPisController and to the CmsPsuAisController. The first one: GET `psu-api/v1/payment/{payment-id}/authorisation/psus` - returns map consisting of PsuData IDs (keys) and statuses of their authorisations for the given payment (values). If PsuData ID is null - this entry is not present in the map. Second endpoint: GET `psu-api/v1/ais/consent/{consent-id}/authorisation/psus` - returns the same map, but input data is consent ID here and it returns authorisations for this consent ID.

## Bugfix: validate PIIS consent creation request
From now on the request for creating new PIIS consent by ASPSP is being validated in controller(POST 
`aspsp-api/v1/piis/consents` endpoint) and 
service(`de.adorsys.psd2.consent.aspsp.api.piis.CmsAspspPiisService#createConsent`) layers.
In case of invalid request HTTP `400 Bad Request` status code(for endpoint) or empty response(for service) will be returned.

To be considered valid the request must contain:
 - some PSU credentials data
 - either no TPP object or TPP info object with authorisation number and authority ID
 - non-empty list of accounts
 - valid consent expiration date

## Bugfix: Payment cancellation supports all non-finalised transaction status

Before, any attempt to start cancellation authorisation for a payment with payment status other then `RCVD` or `PATC` would cause internal server error.
Now, we support payment cancellation process for all payments with non-finalised transaction status(`ACCC`, `ACSC`, `RJCT`,`CANC`).

## Payment cancellation flow reworked

Start payment cancellation logic was reworked and the logic of spi calls was changed. From now, all cancel payment requests will invoke
`PaymentCancellationSpi#initiatePaymentCancellation` method(except for payments with finalized payment status). Depending on SpiPaymentCancellationResponse properties
`transactionStatus` and `cancellationAuthorisationMandated`:
- when `transactionStatus` is `CANC`, no further SPI calls are made;
- when `transactionStatus` is another finalized status(`ACCC`, `ACSC`, `RJCT`), no further SPI calls are made and error message is returned to TPP;
- when both `cancellationAuthorisationMandated` and `paymentCancellationAuthorizationMandated` property in bank profile are false, or `transactionStatus`
is `RCVD`, `PaymentCancellationSpi#cancelPaymentWithoutSca` is invoked;
- when at least one of `cancellationAuthorisationMandated` and `paymentCancellationAuthorizationMandated` property in bank profile is true,
no further SPI calls are made and `startAuthorisation` link is returned to TPP.
