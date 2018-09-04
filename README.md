# Reference Java implementation of PSD2 XS2A Interface of Berlin Group
With **PSD2** (Directive (EU) 2015/2366 of the European Parliament and of the Council on Payment Services in the
Internal Market, published 25 November 2016) the European Union has published a new directive on payment services
in the internal market.
Among others PSD2 contains regulations on new services to be operated by so called
*Third Party Payment Service Providers* (TPP) on behalf of a *Payment Service User* (PSU).

These new services are:
* *Payment Initiation Service* (PIS) to be operated by a Payment Initiation Service Provider (PISP) TPP as defined by article 66 of [PSD2],
* *Account Information Service* (AIS) to be operated by an Account Information Service Provider (AISP) TPP as defined by article 67 of [PSD2], and
* *Confirmation on the Availability of Funds Service* (FCS) to be used by a Payment Instrument Issuing Service Provider (PIISP) TPP as defined by article 65 of [PSD2].

To implement these new services (subject to PSU consent) a TPP needs to access the account of the PSU.
The account is usually managed by another PSP called the *Account Servicing Payment Service Provider* (ASPSP).
To support the TPP in accessing the accounts managed by an ASPSP, each ASPSP has to provide an **"access to account
interface"** (**XS2A interface**).
Responsibilities and rights of TPP and ASPSP concerning the interaction at the XS2A interface are defined and
regulated by PSD2.

## Who we are
[adorsys](https://adorsys.de/en/index.html) is a company who works ever since the very beginning of PSD2 with its requirements and implicit tasks.
We help banks to be PSD2 complaint (technical and legal terms). To speed up the process we provide this open source XS2A interface, specified by Berlin Group,
that can be connected to your middleware system.
You can check your readiness for PSD2 Compliance and other information via [our Web-site](https://adorsys.de/en/psd2.html).

## Features of adorsys/XS2A

* **XS2A-impl** is an implemetation of PSD2 XS2A Interface of Berlin Group. All mandatory API endpoints defined in Berlin Group specification V1.0 are implemented. The latest version of Berlin Group specification used in the project is V1.2.

* **Swagger documentation** is presented for all REST API modules: XS2A, ASPSP-Server, Consent Management System.

* **Consent Management System** is the system intended  to store and manage consents given by PSU to corresponding TPPs.  This system is developed for ASPSPs that don't have their own Consents Management System.

* **ASPSP-Profile** is REST API Module to store and read ASPSP-specific configurations for XS2A-features. The behavior of XS2A can change depending on the stored values, for example, using different SCA approaches.

* **Logging System** is the system that allows ASPSP operators to track all logically related calls in XS2A, starting from the requests from TPP to the final responses to TPP. This module is not in the Berlin Group specification. *(Current state: planned)*.
### Test services

* **ASPSP-Mock** is a simple example of ASPSP implementation. Mock-implementation of ASPSP exists for the purposes of testing and introspection of XS2A functionality. This module is not in the Berlin Group specification.

* **Prototype Online Banking** is a Web Demo application to show how the PSU - ASPSP Interface works. Through this Interface PSU passes SCA and provides the consents to TTP. This module is not in the Berlin Group specification. *(Current state: under construction)*.


## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisites

- Java JDK version 1.8.x, Maven 3.x
- IDP-Server (we recommend Keycloak)
- Docker (Optional)
- MongoDB for persistent mocks (Optional)

### Brief architecture documentation
Available in [the documentation](doc/architecture/README.md)

### Installing and running development environment

Download and run keycloak (port 8081):

`Windows`
```bash
> ...\bin\standalone.bat -Djboss.http.port=8081
```
`Linux`
```bash
$ .../bin/standalone.sh -Djboss.http.port=8081
```

Clone git repository and build a project:
```bash
$ git clone https://github.com/adorsys/xs2a.git
$ cd xs2a
$ mvn clean install
```

Run an ASPSP-Mock-Server:
```bash
$ cd aspsp-mock-server
$ mvn spring-boot:run -Drun.profiles=fongo
```
Open a browser on page [http://localhost:28080/swagger-ui.html](http://localhost:28080/swagger-ui.html)

Run an ASPSP-Profile:
```bash
$ cd aspsp-profile
$ mvn spring-boot:run
```

Run a XS2A-Server:
```bash
$ cd spi-mock
$ mvn spring-boot:run 
```
Open a browser on page [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

Now you may try to put some data using the mock-server and to access it using the xs2a-interface.
See [detailed documentation](doc/spec/README.md) for a requests specification.
If you are not using a real MongoDB server on port localhost:27017, the InMemory database will be used instead.
this means that all mock data will be lost upon restart.

## Deployment

Dockerfiles provided in the project allow to put the build artifacts into a docker images. Those images are to be
configured through your environment (documentation follows) to interact properly.

More details see in [instruction](doc/deployment.md)

## Built With

* [Java, version 1.8](http://java.oracle.com) - The main language of implementation
* [Maven, version 3.0](https://maven.apache.org/) - Dependency Management
* [Spring Boot](https://projects.spring.io/spring-boot/) - Spring boot as core Java framework

## Development and contributing

Please read [CONTRIBUTING](doc/CONTRIBUTING.md) for details on our code of conduct, and the process for submitting pull requests to us.

## Versioning and Releasing

We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/your/project/tags).

Release is being done using the [release scripts](https://github.com/borisskert/release-scripts) ( (C) by [Boris Skert](https://github.com/borisskert) ) located under scripts/release-scripts.
For detailed info see [README for release-scripts](scripts/release-scripts/README.md).

### Steps to make a release

**Release is made from local copy! Ensure that you have enough rights to push to master and develop branches**
```bash
$ git submodule update --init --remote
$ scripts/release-scripts/release.sh <release-version> <next-develop-version>
``` 
Example
```bash
$ scripts/release-scripts/release.sh 1.0 1.1
```
 

## Authors

* **[Francis Pouchata](mailto:fpo@adorsys.de)** - *Initial work* - [adorsys](https://www.adorsys.de)

See also the list of [contributors](doc/contributors.md) who participated in this project.

## License

This project is licensed under the Apache License version 2.0 - see the [LICENSE.md](LICENSE.md) file for details
