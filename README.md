# Reference Java implementation of NextGenPSD2 XS2A Interface of Berlin Group
[![Build Status](https://travis-ci.com/adorsys/xs2a.svg?branch=develop)](https://travis-ci.com/adorsys/xs2a)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=adorsys_xs2a&metric=alert_status)](https://sonarcloud.io/dashboard?id=adorsys_xs2a)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=adorsys_xs2a&metric=coverage)](https://sonarcloud.io/dashboard?id=adorsys_xs2a)

## What is it

With **PSD2** 
```
Directive (EU) 2015/2366 of the European Parliament and of the Council on Payment Services in the
Internal Market, published 25 November 2016
```
 the European Union has forced Banking Market to open the Banking Services to Third Party Service Providers (TPP).
These services are accessible by TPP on behalf of a *Payment Service User* (PSU).

The 'Berlin Group' is a pan-European payments interoperability standards and harmonisation initiative.
Based on the PSD2 and EBA RTS requirements, Berlin Group NextGenPSD2 has worked on a detailed 
['Access to Account  (XS2A) Framework'](https://www.berlin-group.org/psd2-access-to-bank-accounts) with data model
(at conceptual, logical and physical data levels) and associated messaging.

[adorsys](https://adorsys-platform.de/solutions/xs2a-core/) is a company that works ever since the very beginning of PSD2 with its requirements and implicit tasks.

This XS2A Service provides an Open Source implementation of Berlin Group NextGenPSD2 Framework, that can be connected to ASPSPs middleware or core banking system.

### Quick facts about this XS2A Service

* Currently supported version of NextGenPSD2 XS2A API Specification is **1.3.8 20201106v1**.<br>
Currently supported version of NextGenPSD2 XS2A OpenAPI is 1.3.6 20200814.<br>
You can check out our [Swagger API yaml file](xs2a-impl/src/main/resources/static/psd2-api_1.3.8_2020-11-06v1.yaml).
* All mandatory API endpoints defined in Berlin Group specification are IMPLEMENTED.
* Comes with pluggable **Consent Management System** to store and manage consents given by PSU to corresponding TPPs.
* **ASPSP-Profile** module allows you to configure ASPSP-specific configuration for XS2A-features, for example used SCA approaches, payment products, consent types etc.
* Proven [NISP](https://nisp.online)-compliant implementation.

## Project documentation

[Documentation site](https://adorsys.github.io/xs2a/) provides actual automatically build documentation, such as:
* Architecture documentation
* Use-cases diagrams
* Developer guides
* Release policy
* Roadmap
* Release notes

### Getting Started

* If you want to play with the framework in isolated environment, check out [XS2A-Sandbox](https://github.com/adorsys/xs2a-sandbox) Repository.
* To know how to write a connector to your banking system see our [xs2a-connector-examples](https://github.com/adorsys/xs2a-connector-examples) repository and [SPI Developer Guide](https://adorsys.github.io/xs2a/SPI%20Developer%20Guide/SPI_Developer_Guide.html)
* [These instructions](doc/GETTING_STARTED.adoc) will help you to get a copy of the project up and running on your local machine for development and testing purposes. 

## Development and contributing

Please read [CONTRIBUTING](doc/CONTRIBUTING.md) for details on our code of conduct, and the process for submitting pull requests to us.
List of contributors can be found [here](doc/contributors.md).

## Contact

For commercial support please contact **[adorsys Team](https://adorsys-platform.de/solutions/)**.

## License

This project is licensed under the Apache License version 2.0 - see the [LICENSE](LICENSE) file for details
