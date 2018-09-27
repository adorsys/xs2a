# ASPSP profile

Rest-implementation of ASPSP profile for XS2A Interface of Berlin Group

## Configuration and deployment

In order to run ASPSP profile service with external *.yml property file you must specify correct path to file in  
`application.properties` in `bank_profile.path =` field.  
If path will not be correct, system will take *.yml property file by default.

To run ASPSP profile service from command line:

```
mvn clean install 
mvn spring-boot:run
 
```
In order to run service with ability to update data, the project must be started in "debug_mode"

```
mvn clean install 
mvn spring-boot:run -Drun.profiles=debug_mode
 
```
## CORS
By default, allow credentials, all origins and all headers are disabled.
You can override CORS settings by changing values in `application.properties`
```
# Whether credentials are supported. When not set, credentials are not supported.
endpoints.cors.allow-credentials=true
# Comma-separated list of origins to allow. '*' allows all origins. When not set, CORS support is disabled.
endpoints.cors.allowed-origins=*
# Comma-separated list of headers to include in a response.
endpoints.cors.allowed-headers=Origin,Authorization,Content-Type
# Comma-separated list of methods to allow. '*' allows all methods. When not set, defaults to GET.
endpoints.cors.allowed-methods=GET,POST,PUT,DELETE
```


