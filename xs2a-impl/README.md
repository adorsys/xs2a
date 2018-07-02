# XS2A Server

Rest-implementation of PSD2 XS2A Specification of Berlin Group

## Configuration and deployment
To interact with keycloak server properly, please add following parameters to your application.properties file 
```
keycloak.auth-server-url=http://localhost:8081/auth
keycloak.realm=xs2a
keycloak.resource=xs2a-impl
keycloak.public-client=true
keycloak.principal-attribute=preferred_username
keycloak.credentials.secret=74cae234-510c-4094-9439-1ee734e8eefb
keycloak.bearer-only=true
keycloak.cors=false
```
Some of these parameters you can obtain after install and run keycloak server (see 'Keycloak run and setting instruction' section below)

Credentials necessary for secure communication between xs2a and aspsp-mock services (Oauth2 password flow):
```
keycloak-username=aspsp
keycloak-password=zzz
```

To skip ssl certificate verification add the following property to your application.properties file: 
```
skip.ssl.certificate.verification=true
```

To run XS2A-Server app from command line

```
mvn clean install 
mvn spring-boot:run
 
```

``` 
# Keycloak run and setting instruction
```
- Download latest stable version (keycloak-3.4.3.Final) of Keycloak from 
https://www.keycloak.org/downloads.html
- Go to keycloak-3.4.3.Final/bin folder and run keycloak server:
standalone.bat (for Windows users, *.sh for Linux)
- Create realm with name: xs2a
- Create client with name: xs2a-impl
- Go to 'xs2a-impl' client settings tab and set 'Valid redirect URIs' field to: http://localhost:8080/*
- Set 'Web origins' field to: *
- Set 'Access Type' field to: confidential
- Go to Credential tab, copy user secret and put it to keycloak.credentials.secret in application.properties file
- Create user with name: aspsp
- Create role 'user' and map it to 'aspsp' user 
```

