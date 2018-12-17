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

## TPP Stop List
Possibility to block TPP has been added to CMS.
If TPP is blocked, the 401 CERTIFICATE_BLOCKED error is returned on any request to Xs2a endpoints.

Now CMS contains several endpoints for TPP Stop List proceeding:

**For ASPSP:**
* GET `/aspsp-api/v1/tpp/stop-list` - returns TPP stop list record by TPP authorisation number and national authority ID
* PUT `/aspsp-api/v1/tpp/stop-list/block` - blocks TPP by TPP authorisation number, national authority ID and lock period
* DELETE `/aspsp-api/v1/tpp/stop-list/unblock` - unblocks TPP by TPP authorisation number and national authority ID

**For Xs2a:**
* GET `/api/v1/tpp/stop-list` - checks if TPP is blocked

Scheduler service has been created: it will unblock the TPPs with blocking period expired (if TPP was blocked for the provided period of time).
The scheduler service invocation frequency could be modified by changing `stoplist.cron.expression` value in `application.properties`.
