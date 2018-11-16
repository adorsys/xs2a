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
A PSU may want to cancel the payment. The flow of payment cancellation for embedded and redirect approches was added.

## Create one endpoint in CMS for working with AspspConsentData 
Moved interactions with AspspConsentData from different services into one service
Added ability to delete AspspConsentData

