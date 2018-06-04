# Consent management system

Rest-implementation of consent management system for XS2A Interface of Berlin Group

## Configuration and deployment
To interact with keycloak server properly, please add following parameters to your application.properties file 
```
keycloak.auth-server-url=http://localhost:8081/auth
keycloak.realm=xs2a
keycloak.resource=consent-management
keycloak.public-client=true
keycloak.principal-attribute=preferred_username
keycloak.credentials.secret=66ae619f-08ed-4c6c-852d-391fa5efecad
keycloak.bearer-only=true
keycloak.cors=false
```
Some of these parameters you can obtain after installing and running keycloak server (see 'Keycloak run and setting instruction' section below)

To interact with PostgreSQL DB properly, please add following parameters to your application.properties file 
```
spring.datasource.url=jdbc:postgresql://localhost/consent
spring.datasource.username=yourUsername
spring.datasource.password=yourPassword
spring.jpa.properties.hibernate.default_schema=consent
spring.jpa.generate-ddl=true
```
Some of these parameters you can obtain after installing and running PostgreSQL DB (see 'PostgreSQL database run and settings instuctions' section below)

# PostgreSQL database run and settings instuctions
```
- Download latest stable version of PostgreSQL database from 
  https://www.postgresql.org/download/
- Install and run PostgreSQL database
- Create a user and a password, add these parameters to your application.properties file insted of "yourUsername" and "yourPassword"
- Create a database with name "consent" and default settings
```

To run consent-management-system app from command line:

```
mvn clean install 
mvn spring-boot:run
 
```

# Keycloak run and setting instruction
```
- Download latest stable version (keycloak-3.4.3.Final) of Keycloak from 
https://www.keycloak.org/downloads.html
- Go to keycloak-3.4.3.Final/bin folder and run keycloak server:
standalone.bat (for Windows users, *.sh for Linux)
- Create realm with name: xs2a
- Create client with name: consent-management
- Go to 'consent-management' client settings tab and set 'Valid redirect URIs' field to: http://localhost:38080/*
- Set 'Web origins' field to: *
- Set 'Access Type' field to: confidential
- Go to Credential tab, copy user secret and put it to keycloak.credentials.secret in application.properties file
- Create user with name: aspsp
- Create role 'user' and map it to 'aspsp' user 
```

