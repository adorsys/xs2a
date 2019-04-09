# XS2A Server

Rest-implementation of PSD2 XS2A Specification of Berlin Group.

## CORS
By default, allow credentials, all origins and all headers are disabled.
You can override CORS settings by changing values in `application.properties`
```
# Whether credentials are supported. When not set, credentials are not supported.
xs2a.endpoints.cors.allow-credentials=true
# Comma-separated list of origins to allow. '*' allows all origins. When not set, CORS support is disabled.
xs2a.endpoints.cors.allowed-origins=*
# Comma-separated list of headers to include in a response.
xs2a.endpoints.cors.allowed-headers=Origin,Authorization,Content-Type
# Comma-separated list of methods to allow. '*' allows all methods. When not set, defaults to GET.
xs2a.endpoints.cors.allowed-methods=GET,POST,PUT,DELETE
```
## Provide XS2A Swagger as an option
To enable swagger in xs2a you have to add @EnableXs2aSwagger annotation to your connector. To disable swagger just remove it.
You should also put PSD2 API yaml file to the resource folder of your connector to override default PSD2 API. To do that you need to fill in 
xs2a.swagger.psd2.api.location property in your application.properties file
