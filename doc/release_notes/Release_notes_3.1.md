# Release notes v.3.1

## Table of Contents
- New Concept: SpiAspspConsentDataProvider
- Feature: Added new parameter for new Redirect SCA Approach
- Feature: Changed links for new Redirect SCA Approach subtype
- Take out ASPSP Mock Server and corresponding Connector out of XS2A
- Bugfix: Fix error code on trying to read transaction list with invalid expired consent
- Bugfix: SCA status EXEMPTED should be a finalised status
- Bugfix: Fix error with detecting SCA approach based on "tpp-redirect-preferred" header
- Bugfix: Get authorisations of payment when one is already finalised
- Bugfix: links building while getting account list and account details
- Bugfix: Fix update PSU Data without unnecessary PSU_ID in header
- Bugfix: empty arrays are included in all responses
- Bugfix: Return correct value of "frequencyPerDay" property on get Consent request
- Migrate to .yaml OpenAPI v. 1.3.3
- Bugfix: Validate the value of the combinedServiceIndicator property on consent creation

## New Concept: SpiAspspConsentDataProvider
In previous versions SPI-API provided a possibility to store some information, such as access tokens or any other 
workflow-relevant data in encrypted consent object. For that purpose in every SPI-Call there were `AspspConsentData` parameter,
that provided access to `AspspConsentData` byte-array. Since it was plain POJO, unnecessary reads/updates were invoked.

Now we change this concept by providing instead `AspspConsentData` object an `SpiAspspConsentDataProvider` interface.
Within the SPI-call now developer can access `AspspConsentData` byte-array using the `loadAspspConsentData` method.
And in case of saving the data, `updateAspspConsentData` may be used, that invokes immediate save of consent data to the database.

**Please note**: from now on the SPI-Developer is responsible to save the AspspConsentData to the database. XS2A-Core provides
this possibility, but if no `updateAspspConsentData` call happens, no data will be saved.

Therefore, for the objects, _where new parameter is provided_, no further need to provide AspspConsentData as part of the response object.
For that reason `SpiResponseBuilder`'s method `aspspConsentData` was deprecated.

In this release we provide these changes for `PaymentSpi`, in future versions the same approach will be used for `ConsentSpi`
and `AuthorisationSpi` as well.

## Feature: Added new parameter for new Redirect SCA Approach
A new `scaRedirectFlow` parameter has been added to ASPSP profile.

| Option          | Meaning                                                             | Default value | Possible values |
|-----------------|---------------------------------------------------------------------|---------------|-----------------|
| scaRedirectFlow | This field indicates what variant of Redirect approach will be used | REDIRECT      | REDIRECT, OAUTH |

## Feature: Changed links for new Redirect SCA Approach subtype
Redirect SCA approach was extended: now it has two possible subtypes - `REDIRECT` (default) and `OAUTH`.
Depending on this subtype, the link names would be different in the following requests:
 - payment initiation
 - consent creation
 - payment cancellation authorisation

In case of OAUTH subtype scaOAuth link will be used instead of scaRedirect.

## Take out ASPSP Mock Server and corresponding Connector out of XS2A
From now on several modules are no longer present in the repository :
 - aspsp-idp
 - aspsp-mock-api
 - aspsp-mock-server
 - online-banking-demo
 - online-banking-demo-ui
 - spi-mock 
In XS2A version 2.x these modules are available for usage.

In order to test the actual version of XS2A you can use `xs2a-standalone-starter` component that starts XS2A
with stub implementation of `spi-api`. Thus it can be reused with your `spi-api` implementation.
To get a full functional Sandbox environment consider using our projects based on XS2A:
* [PSD2 Accelerator](https://github.com/adorsys/psd2-accelerator) - a static XS2A Sandbox that is 
compliant to the NISP Sandbox requirements.
* [XS2ABank](https://github.com/adorsys/xs2a-sandbox) - a dynamic XS2A Sandbox that simulates 
work of simple core banking system by ledgers project.

## Bugfix: Fix error code on trying to read transaction list with invalid expired consent
From now on, executing Read Transaction List request (`GET /v1/accounts/{account-id}/transactions`) or Read Transaction 
Details request (`GET /v1/accounts/{account-id}/transactions/{resourceId}`) with consent that doesn't have access to 
transaction and is already expired will result in `401 CONSENT_INVALID` error instead of `401 CONSENT_EXPIRED`.

## Bugfix: SCA status EXEMPTED should be a finalised status
From now on, SCA status `EXEMPTED` is considered to be a finalised status. It means that once the authorisation status 
is changed to `EXEMPTED`, it will no longer be possible to change the status of this authorisation.

## Bugfix: Fix error with detecting SCA approach based on "tpp-redirect-preferred" header
From now on, the following rules are applied to choose SCA Approach from the list of available SCA Approaches in ASPSP-profile:
 * If header "tpp-redirect-preferred" is provided with value "true" and ASPSP supports Redirect approach, then this approach will be used.
 * If header "tpp-redirect-preferred" is provided with value "false", the first non-Redirect approach from the list will be used.
 * If header "tpp-redirect-preferred" is not provided, the first approach from the list will be chosen.
 * If ASPSP has only one SCA approach in profile, header "tpp-redirect-preferred" will be ignored and only approach from profile will be used

## Bugfix: Get authorisations of payment when one is already finalised
From now on, the GET authorisations request(`GET /v1/payments/sepa-credit-transfers/{PAYMENT_ID}/authorisations`) works with all payments statuses (not only with RCVD or PATC )

## Bugfix: links building while getting account list and account details
Implemented the new mechanism for links generation in the `getAccoutList` request (GET `/v1/accounts/{withBalance}`) and 
`readAccountDetails` request (GET `/v1/accounts/{account-id}/{withBalance}`).
Currently these links depend on the consent and its accesses: if the consent has only accounts access - the links are 
not provided. If the consent has balances access - balances link is present and for the transactions access - transactions
link is given. Boolean path parameter `withBalance` influences only the responses for these requests and has nothing common
with the links.

## Bugfix: Fix update PSU Data without unnecessary PSU_ID in header
From now on, `update PSU Data` request for selecting SCA method doesn’t need to contain PSU-ID header and will be processed correctly even without it.

## Bugfix: empty arrays are included in all responses
From now on, empty arrays in the HTTP responses are included in the bodies. For example, before the response was: 
`"access": {}`, now it is: `"access": { "accounts": [], "balances": [], "transactions": []}`.

## Bugfix: Return correct value of "frequencyPerDay" property on get Consent request
From now on, "frequencyPerDay" property in get AIS Consent(`GET /v1/consents/{consentId}`) response contains initial value or adjusted by ASPSP, according to profile settings.
Before that, this field always had initial value that was sent by TPP.

## Migrate to .yaml OpenAPI v. 1.3.3
XS2A models and interfaces were updated in accordance with version 1.3.3 of OpenAPI 3.0 file by Berlin Group.

The migration process caused several changes to the SPI level:
 - New property `requestedExecutionTime` was added to the `de.adorsys.psd2.xs2a.spi.domain.payment.SpiBulkPayment`.
 - Some properties in `de.adorsys.psd2.xs2a.spi.domain.account.SpiExchangeRate` were renamed in accordance with the 
specification. Getters that used old names of these properties were marked as deprecated and will be removed in 2.6.
New getters should be used to access these properties instead.

| Old method name | New method name           |
|-----------------|---------------------------|
| getRate         | getExchangeRate           |
| getRateDate     | getQuotationDate          |
| getRateContract | getContractIdentification |

## Bugfix: Validate the value of the combinedServiceIndicator property on consent creation
From now on, the value of the `combinedServiceIndicator` property in consent creation request(`POST /v1/consents`) will
be properly validated. If `combinedServiceIndicator` in the request body is set to `true`, but the ASPSP doesn't support it
(i. e. `combinedServiceIndicator` property in the ASPSP profile is set to `false`), `400 SESSIONS_NOT_SUPPORTED` error
will be returned in the response.
