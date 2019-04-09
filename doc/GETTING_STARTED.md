# Getting started

## Prerequisites

- Java JDK version 1.8.x, Maven 3.x;
- Relational database for consent-management-system. We recommend to use PostgreSQL 9.5;
- Docker (Optional).


### Clone git repository and build a project:
```bash
$ git clone https://github.com/adorsys/xs2a.git
$ cd xs2a
$ mvn clean install
```

### Use docker-compose

There is docker-compose.yml in root folder which will help to run containers in docker.
Some images will be build from sources, some fetched prebuild from docker hub.
All liquibase DB settings will be applied automatically and you can skip the step `Run a Consent-Management-System server`

Docker-compose runs containers: 
- aspsp-xs2a_aspsp-profile (aspsp-profile-server)
- aspsp-xs2a_consent-management (cms-standalone-service)
- aspsp-xs2a_consent-management-postgres (Postgres DB)
- aspsp-xs2a_xs2a-standalone-starter (xs2a-standalone-starter)

Start docker compose.
```
$ docker-compose up
```

### Run an ASPSP-Profile:
`Windows`
```
> cd aspsp-profile\aspsp-profile-server
> mvn spring-boot:run
```

`UNIX` (`BSD`, `Linux` or `MacOS`)
```bash
$ cd aspsp-profile/aspsp-profile-server
$ mvn spring-boot:run
```
Open a browser on page [http://localhost:48080/swagger-ui.html](http://localhost:48080/swagger-ui.html)

### Run a Consent-Management-System server:
First of all you need to setup a relational database for it.
We recommend to use PostgreSQL.
Default parameters for that DB one can find in the `liquibase.example.properties`:

| Parameter   | Value     |
|-------------|-----------|
| DB Host     | localhost |
| DB Port     | 5432      |
| DB Name     | consent   |
| DB Schema   | consent   |
| DB User     | cms       |
| DB Password | cms       |

Setup a database and db schema.

Then create tables using liquibase:

`Windows`
```
> cd .\consent-management\cms-db-schema\
> copy liquibase.example.properties liquibase.properties
```

`UNIX` (`BSD`, `Linux` or `MacOS`)
```bash
$ cd consent-management/cms-db-schema
$ cp liquibase.example.properties liquibase.properties
$ mvn liquibase:update
```

Now, once PostgreSQL-server is running on port `5432` you may run consent-management-system server:
```bash
$ cd consent-management/cms-standalone-service
$ mvn -Drun.arguments=--server_key=12345678 spring-boot:run
```
Open a browser on page [http://localhost:38080/swagger-ui.html](http://localhost:38080/swagger-ui.html)

### Run a XS2A-Server:
By default XS2A-library shall be packed with the ASPSP-Connector, that connects the library to the corresponding ASPSP-systems.

To start the XS2A do:

Open a browser on page [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

Now you may try to put some data using the xs2a-interface.
See some test scripts [for Postman](../scripts/tests/postman) or [Insomnia](../scripts/tests/insomnia) as a starting point.
