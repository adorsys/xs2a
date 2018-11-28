# Release notes v. 1.13

## Add parameter "deltaReportSupported" to ASPSP-profile
Now we can set parameter for delta-report support in ASPSP-Profile.

| Option                                  | Meaning                                                                                             | Default value                                        | Possible values                                                                                      |
|-----------------------------------------|-----------------------------------------------------------------------------------------------------|------------------------------------------------------|------------------------------------------------------------------------------------------------------|
|deltaReportSupported                     | This field indicates if an ASPSP supports Delta reports for transaction details                     | false                                                | true, false                                                                                          |

## Provide XS2A Swagger as an option
Now Swagger is not enabled for XS2A Interface by default.
To enable swagger in xs2a you have to add `@EnableXs2aSwagger` annotation on any of Spring configuration classes / Spring boot Application class in your application. To disable swagger just remove it.
You could also put PSD2 API yaml file to the resource folder of your connector to override default PSD2 API. To do that you need to fill in 
`xs2a.swagger.psd2.api.location` property in your application.properties file. I.e.
`xs2a.swagger.psd.api.location=path/in/my/classpath/my_swagger_api.yml`

## Payment cancellation endpoint gives an error when trying to cancel finalised payments
When payment is finished (has transaction statuses *Cancelled, Rejected, AcceptedSettlementCompleted*) there is no possibility to cancel it or to proceed payment cancellation authorisation flow.
The error "FORMAT_ERROR" with http status 400 and TPP message "Payment is finalised already and cannot be cancelled" will be displayed.


