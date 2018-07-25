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
We use liquibase for generating DB tables. Property file with settings you can find in resources folder.

Some of these parameters you can obtain after installing and running PostgreSQL DB (see 'PostgreSQL database run and settings instuctions' section below)

# PostgreSQL database run and settings instuctions
```
- Download latest stable version of PostgreSQL database from 
  https://www.postgresql.org/download/
- Install and run PostgreSQL database
- Create a user and a password, add these parameters to your application.properties file instead of "yourUsername" and "yourPassword"
- Create a database with default settings, add the name of the database to your application.properties file instead of "yourDatabaseName"
```

To run consent-management-system app from command line:

```
mvn clean install 
mvn spring-boot:run
 
```
