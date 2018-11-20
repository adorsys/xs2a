# Release notes v. 1.12

## Extract CMS web-endpoints to different maven modules
A SPI Developer now can decide what endpoints on CMS side he needs.
There are three modules with different endpoints to serve different possible purposes of CMS.
* consent-xs2a-web is used to provide endpoints for xs2a-service. 
This is normally needed if CMS is deployed as a separate service.
* consent-psu-web is used to provide endpoints for presentation and work with consents in some PSU application, 
i.e. online-banking system. This module can be used in any setup of CMS (embedded and standalone).
* consent-aspsp-web is used to provide other endpoints available for banking systems.
This module can be used in any setup of CMS (embedded and standalone).

## Payment cancellation is added
A PSU may want to cancel the payment. The flow of payment cancellation for embedded and redirect approaches was added.
SPI Developer needs to implement PaymentCancellationSpi in order to support it.

## Return full SCA Object from SPI during request to send TAN
By invoking `requestAuthorisationCode` method SPI developer now obliged
to return full SCA Authentification Object for choosen SCA Method.
This is done to avoid unnecessary additional call to list all available methods second time.
`SpiAuthorizationCodeResult` now adjusted accordingly.

## Improved nullability checks in SpiResponse
Due to importance of having not-nullable results in SPI API nullability checks by SpiResponse object construction were added.
Please note, that this also implies, if you need to return VoidResponse.
Please use SpiResponse.voidResponse() static method for that.

## Support Oracle DB
Migrations scripts were modified in order to fix oracle supporting issues. You may be required to regenerate local schemes.

## Log TPP requests and responses
Now all the requests and responses from TPP to XS2A are logged.
Logging flow is configured in logback-spring.xml file.
If there is a need to rewrite logging configurations, the following should be done:
* logback file (logback.xml or logback.groovy (if Groovy is on the classpath)) should be created inside the project. 

Please, use only mentioned names for logback files to make rewriting configs work.

## New Java Interface for locking TPP Access provided in CMS-ASPSP-API
With the de.adorsys.psd2.consent.aspsp.api.CmsAspspTppService ASPSP can implement a functionality of locking TPP by a certain
time or forever or unlocking it. This may be required to provide temporary solutions to block requests from inappropriately
behaving TPP before its certificate will be revoked.
Functional implementation for this Java interface is planned to provided in the upcoming weeks. See [Roadmap](../roadmap.md)
