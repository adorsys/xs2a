# Release notes v. 1.13

## Added parameter "deltaReportSupported" and "redirectUrlExpirationTimeMs" to ASPSP-profile
Now we can set parameters for delta-report support and redirect expiration time in ASPSP-Profile.

| Option                                  | Meaning                                                                                             | Default value                                        | Possible values                                                                                      |
|-----------------------------------------|-----------------------------------------------------------------------------------------------------|------------------------------------------------------|------------------------------------------------------------------------------------------------------|
|deltaReportSupported                     | This field indicates if an ASPSP supports Delta reports for transaction details                     | false                                                | true, false                                                                                          |
|redirectUrlExpirationTimeMs              | This field contains the limit of an expiration time of redirect url set in milliseconds             | 600 000                                              | milliseconds (1, 2,...)                                                                                         |

## Provide XS2A Swagger as an option
Now Swagger is not enabled for XS2A Interface by default.
To enable swagger in xs2a you have to add `@EnableXs2aSwagger` annotation on any of Spring configuration classes / Spring boot Application class in your application. To disable swagger just remove it.
You could also put PSD2 API yaml file to the resource folder of your connector to override default PSD2 API. To do that you need to fill in 
`xs2a.swagger.psd2.api.location` property in your application.properties file. I.e.
`xs2a.swagger.psd.api.location=path/in/my/classpath/my_swagger_api.yml`

## Provide CMS Swagger as an option
Now Swagger is not enabled for CMS Interface by default.
To enable swagger in cms you have to add `@EnableCmsSwagger` annotation on any of Spring configuration classes / Spring boot Application class in your CMS application. To disable swagger just remove it.

## PaymentProduct entity was replaced by raw String value
Now instead of using PaymentProduct enum class, string value is used. PaymentProduct enum class is removed.
In database, instead of saving enum values(SEPA, INSTANT_SEPA, etc), raw string values are saved:  sepa-credit-transfers, instant-sepa-credit-transfers, etc.

## Get authorisation sub-resources is implemented
| Context                             | Method | Endpoint                                        | Description                                                                                     |
|-------------------------------------|--------|-------------------------------------------------|-------------------------------------------------------------------------------------------------|
| Payment Initiation Request          | GET    | v1/{payment-service}/{paymentId}/authorisations | Will deliver an array of resource identifications of all generated authorisation sub-resources. |
| Account Information Consent Request | GET    | v1/consents/{consentId}/authorisations          | Will deliver an array of resource identifications of all generated authorisation sub-resources. |

## No possibility to cancel finalised payment
When payment is finished (has transaction statuses *Cancelled, Rejected, AcceptedSettlementCompleted*) there is no possibility to cancel it or to proceed payment cancellation authorisation flow.
The error "FORMAT_ERROR" with http status 400 and TPP message "Payment is finalised already and cannot be cancelled" will be displayed.

## Added expiration time for redirect url
Redirect url and related authorisation now have an expiration time. The value for expiration time is counted with formula 
"current time of authorisation creation + redirect url expiration time (set in ASPSP-profile)". 
We give redirect id (= authorisation id) in redirect link now (instead of payment id and authorisation in in previous versions), and to get payment/consent id and related payment information, online banking should call 
 **GET /psu-api/v1/pis/consent/redirect/{redirect-id}** endpoint of consent management system.
If redirect url is not expired, online banking gets payment, authorisation id, not ok tpp redirect url and ok tpp redirect url in response, otherwise http code 400 is sent.

## Changes to FundsConfirmationSpi
Now the whole PIIS Consent object is being passed to FundsConfirmationSpi#performFundsSufficientCheck instead of just consent id.
Also FundsConfirmationSpi#performFundsSufficientCheck now responds with
de.adorsys.psd2.xs2a.spi.domain.fund.SpiFundsConfirmationResponse instead of java.lang.Boolean.
