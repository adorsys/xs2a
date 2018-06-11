# ASPSP profile

Rest-implementation of ASPSP profile for XS2A Interface of Berlin Group

## Configuration and deployment

To run ASPSP profile service from command line:

```
mvn clean install 
mvn spring-boot:run
 
```
For running service with ability to update data, the project must be started in "debug_mode"

```
mvn clean install 
mvn spring-boot:run -Drun.profiles=debug_mode
 
```

