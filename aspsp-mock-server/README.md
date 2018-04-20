# Project Title

Implementation of Mock ASPSP for XS2A Interface of Berlin Group 

## Built With

* [Java, version 1.8](http://java.oracle.com) - The main language of implementation
* [Maven, version 3.0](https://maven.apache.org/) - Dependency Management
* [Spring Boot](https://projects.spring.io/spring-boot/) - Spring boot as core Java framework


## Deployment

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
