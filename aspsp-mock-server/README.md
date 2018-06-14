# ASPSP Mock Server

Implementation of Mock ASPSP for XS2A Interface of Berlin Group 

## Configuration and deployment
To interact with keycloak server properly, please add following parameters to your application.properties file 
```
keycloak.auth-server-url=http://localhost:8081/auth
keycloak.realm=xs2a
keycloak.resource=aspsp-mock
keycloak.public-client=true
keycloak.principal-attribute=preferred_username
keycloak.credentials.secret=70088701-3e98-4343-9573-21ed4334f477
keycloak.bearer-only=true
keycloak.cors=false
```
Some of these parameters you can obtain after install and run keycloak server (see 'Keycloak run and setting instruction' section below)

To run Mock ASPSP app from command line

1. with in-memory DB fongo:

```
mvn clean install 
mvn spring-boot:run -Drun.profiles=fongo
 
```

2. with test data in-memory DB fongo:

```
mvn clean install 
mvn spring-boot:run -Drun.profiles=fongo,data_test 
 
```

3. In order to run app with real DB, please input correct connection credentials into mongo.properties.
   Then use command line to run the app.:  

```
mvn clean install 
mvn spring-boot:run -Drun.profiles=mongo 
 
``` 
# Keycloak run and setting instruction
```
- Download latest stable version (keycloak-3.4.3.Final) of Keycloak from 
https://www.keycloak.org/downloads.html
- Go to keycloak-3.4.3.Final/bin folder and run keycloak server:
standalone.bat (for Windows users, *.sh for Linux)
- Create realm with name: xs2a
- Create client with name: aspsp-mock
- Go to 'aspsp-mock' client settings tab and set 'Valid redirect URIs' field to: http://localhost:28080/*
- Set 'Web origins' field to: *
- Set 'Access Type' field to: confidential
- Go to Credential tab, copy user secret and put it to keycloak.credentials.secret in application.properties file
- Create user with name: aspsp
- Create role 'user' and map it to 'aspsp' user 
```

#Email TAN confirmation

In order to work properly with email TAN confirmation and send letters from your email, you need to change these properties in application.properties file:

```
# your smtp server, for example `smtp.gmail.com` (stmp server for gmail)
spring.mail.host=

# port of the server, for example `587` (port for gmail)
spring.mail.port=

# password to your mail
spring.mail.password=

# email address
spring.mail.username=

# properties for authorization, for example `true` and `false`
spring.mail.properties.mail.smtp.auth=
spring.mail.properties.mail.smtp.starttls.enable=

```
