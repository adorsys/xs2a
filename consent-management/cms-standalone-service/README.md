# Consent management system

Rest-implementation of consent management system for XS2A Interface of Berlin Group

## Configuration and deployment
To interact with PostgreSQL DB properly, please add following parameters to your application.properties file 
```
spring.datasource.url=jdbc:postgresql://localhost/yourDatabaseName
spring.datasource.username=yourUsername
spring.datasource.password=yourPassword
spring.jpa.properties.hibernate.default_schema=yourDatabaseName
```
We use liquibase for generating DB tables. Property file with settings you can find in resources folder. You can find brief instruction how to setup database quickly in the `cms-db-schema/README.md` file.

Some of these parameters you can obtain after installing and running PostgreSQL DB (see 'PostgreSQL database run and settings instuctions' section below)

# PostgreSQL database run and settings instuctions
```
- Download latest stable version of PostgreSQL database from 
  https://www.postgresql.org/download/
- Install and run PostgreSQL database
- Create a user and a password, add these parameters to your application.properties file instead of "yourUsername" and "yourPassword"
- Create a database with default settings, add the name of the database to your application.properties file instead of "yourDatabaseName"
```

**WARNING:**  `For encryption\decryption of aspsp data we use secret server key ('server_key') which is read from Environment variables.  
So, before starting project you need to create environment variables and set value, as example:  'server_key=12345678' or run app with this parameter` 

To run consent-management-system app from command line:

1. Without test data
```
mvn clean install 
mvn -Drun.arguments=--server_key=12345678 spring-boot:run
 
```

2. With test data
```
mvn clean install 
mvn spring-boot:run -Drun.profiles=data_test
 
```

# Using CMS
Our implementation of CMS offers a set of endpoints to create/update/get the AIS consent information.
As a requested by PSD2 specification every AIS consent contains a counter field to store data about remaining uses of current AIS consent. 
This value should be decremented each time the TPP of PSU requests any consent related information from CMS.
 
**WARNING:** `The counter is not decremented automatically! To perform decrement and logging developer should trigger the corresponding endpoint at AIS controller!`
## AIS:
- An endpoint to create AIS consent
- An endpoint to update consent usage counter (decrements usage value by 1 use) and log the information on account/operation etc the consent was used for
- An endpoint to retrieve the AIS consent by its external identifier. 
- An endpoint to retrieve the AIS consent status by its external identifier.
- An endpoint to update the AIS consent status by its external identifier. 
- An endpoint to update the AIS consent aspsp data.

## PIS:
- An endpoint to create PIS consent.
- An endpoint to retrieve the PIS consent status by its external identifier.
- An endpoint to retrieve the PIS consent by its external identifier. 
- An endpoint to update the PIS consent aspsp data.
- An endpoint to update the PIS consent status by its external identifier.

## PIIS:
- An endpoint to create PIIS consent.
- An endpoint to retrieve PIIS consents by PSU.
- An endpoint to terminate PIIS consent by its external identifier.

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
