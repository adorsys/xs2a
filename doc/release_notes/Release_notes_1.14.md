# Release notes v. 1.14

## Added cors application properties to the XS2A
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
