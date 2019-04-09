# Release notes v.3.1

## New Consept: SpiAspspConsentDataProvider
In previous versions SPI-API provided a possibility to store some information, such as access tokens or any other 
workflow-relevant data in encrypted consent object. For that purpose in every SPI-Call there were `AspspConsentData` parameter,
that provided access to `AspspConsentData` byte-array. Since it was plain POJO, unnecessary reads/updates were invoked.

Now we change this concept by providing instead `AspspConsentData` object an `SpiAspspConsentDataProvider` interface.
Within the SPI-call now developer can access `AspspConsentData` byte-array using the `loadAspspConsentData` method.
And in case of saving the data, `updateAspspConsentData` may be used, that invokes immidiate save of consent data to the database.

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
