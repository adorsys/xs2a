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
