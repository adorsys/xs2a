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
By default, all origins, all headers and all HTTP methods are allowed.
You can override CORS settings by changing values in `application.properties`
```
endpoints.cors.allow-credentials
endpoints.cors.allowed-origins
endpoints.cors.allowed-headers
endpoints.cors.allowed-methods
```


