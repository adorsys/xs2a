# Release notes v. 1.14

## Supported payment creation with pain.001 XML message
Now, an endpoint POST /v1/payments/{payment-product} supports creation of a single payment in pain.001 format. 
A TPP needs to provide body in pain.001 XML format and provide a required payment-product(pain.001-sepa-credit-transfers, etc). 
GET /v1/payments/{payment-id} endpoint was updated as well to support a single payment in pain.001 format and returns the whole XML body of it.
For this type of payment, HttpServletRequest body is read and transferred as a byte array. 
New Spi interface was added - CommonPaymentSpi. So far, it is responsible for the creation of all types of pain.001 XML payments.
An Spi Developer should provide an implementaion of this interface to be able to operate pain.001 XML payments.

## CORS for Xs2a library
By default, allow credentials, all origins and all headers are disabled.
You can override CORS settings by changing values in `application.properties`
```
# Whether credentials are supported. When not set, credentials are not supported
xs2a.endpoints.cors.allow-credentials=true
# Comma-separated list of origins to allow. '*' allows all origins. When not set, CORS support is disabled
xs2a.endpoints.cors.allowed-origins=*
# Comma-separated list of headers to include in a response.
xs2a.endpoints.cors.allowed-headers=Origin,Authorization,Content-Type
# Comma-separated list of methods to allow. '*' allows all methods. When not set, defaults to GET
xs2a.endpoints.cors.allowed-methods=GET,POST,PUT,DELETE
```
## Removed Spring Boot dependencies from Xs2a library
Spring Boot dependencies were removed from Xs2a library. Now we use the following Spring dependencies (v. 4.3.21.RELEASE): 
* spring-test
* spring-hateoas
* spring-web
* spring-context

## Changes to CmsPsuAisService and CmsPsuPisService
Now methods updatePsuDataInConsent and updatePsuInPayment take redirectId as an argument instead of consentId and paymentId accordingly.
Also ulr paths in CmsPsuAisConsentController and CmsPsuPisController were changed. Look at the table below.

| Method | Context                     | Old path                                                   | New path                                                    |
|--------|-----------------------------|------------------------------------------------------------|-------------------------------------------------------------|
| PUT    | Updates PSU Data in consent | psu-api/v1/ais/consent/{consent-id}/update-psu-data        | psu-api/v1/ais/consent/redirects/{redirect-id}/psu-data     |
| GET    | Gets consent by redirect id | psu-api/v1/ais/consent/redirect/{redirect-id}              | psu-api/v1/ais/consent/redirects/{redirect-id}              |
| PUT    | Updates PSU Data in payment | psu-api/v1/pis/consent/{payment-id}                        | psu-api/v1/pis/consent/redirects/{redirect-id}/psu-data     |
| GET    | Gets payment by redirect id | psu-api/v1/pis/consent/{payment-id}/redirect/{redirect-id} | psu-api/v1/pis/consent/{payment-id}/redirects/{redirect-id} |
