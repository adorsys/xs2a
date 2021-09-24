# Create Oracle Env

0. Switch folder to docker file
```
 cd path/to/proj/aspsp-xs2a/consent-management/cms-standalone-service/src/main/resources/oracle
 
```

1.Build oracle docker image (please waite ...)
```
 docker build -t oracle-image-11 -f Dockerfile .
```

2.Check images for new oracle image
```
 docker images
```

3.Start contained with Oracle (please waite ...)
```
 docker run --name oracle-db -p 1521:1521 oracle-image-11
```

4.Add dependencies 

 - add depends to pom.xml in  'cms-db-schema' 
 
```
 <dependency>
    <groupId>com.oracle</groupId>
    <artifactId>ojdbc7</artifactId>
    <version>12.1.0.2</version>
    <scope>system</scope>
    <systemPath>${basedir}/../cms-standalone-service/src/main/resources/oracle/lib/ojdbc7.jar</systemPath>
</dependency>

```

 - add depends to pom.xml in  'cms-standalone-service' 
```
<dependency>
     <groupId>com.oracle</groupId>
     <artifactId>ojdbc7</artifactId>
     <version>12.1.0.2</version>
     <scope>system</scope>
     <systemPath>${basedir}/src/main/resources/oracle/lib/ojdbc7.jar</systemPath>
 </dependency>
```

5.Edit application.properties in 'cms-standalone-service' and in 'spi-mock'

```
spring.datasource.url: jdbc:oracle:thin:@localhost:1521:XE 
spring.jpa.properties.hibernate.default_schema=CMS

```

