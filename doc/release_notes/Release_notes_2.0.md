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

## BookingStatus entity moved to xs2a-core, Xs2aBookingStatus and SpiBookingStatus deleted

From now on only one enum that represents booking status exists. `BookingStatus` is moved to `xs2a-core` package, duplicates 
`Xs2aBookingStatus` and `SpiBookingStatus` are deleted.

## List of PSU Data is provided in payment objects to all SPI methods

From now on, these SPI payment objects contain list of PSU Data:
 - SinglePaymentSpi
 - PeriodicPaymentSpi
 - BulkPaymentSpi
 - SpiPaymentInfo

## Several PSUs in AIS consent
Due to multilevel authorisation of consents, we can store data of several PSUs for each consent.

These changes also affect SPI level, meaning that from now on `SpiAccountConsent` contains list of `SpiPsuData` instead of a single object.
