# <p align="center"><img src="xs2a_core.png" height="200"></p>

# Reference Java implementation of NextGenPSD2 XS2A Interface of Berlin Group

This is the try-out version of adorsys XS2A Core: an open source (AGPL v.3) solution to get acquainted with adorsys’ implementation of the NextGenPSD2 specification. This software is delivered as-is and we're happy about any contributions done by the community to improve it.

We currently focus on collaborative projects with our customers where our frameworks can be used as a basis for building new individual XS2A solutions.

For try-out version you can build the sources on your local. To do so, download or checkout the definite branch (branch name is actually a release name) and build it. For any further assistance, please contact us directly.

If you are an organisation that would like to commercially use our solutions beyond AGPL v3 requirements, we are open to discuss alternative individual licensing options. If you are interested in working with us or have any other inquiries, please contact us under [email](psd2@adorsys.com).

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

[adorsys](https://adorsys.com/de/produkte/xs2a-core/) is a company that works ever since the very beginning of PSD2 with its requirements and implicit tasks.

This XS2A Service provides an Open Source implementation of Berlin Group NextGenPSD2 Framework, that can be connected to ASPSPs middleware or core banking system.

### Quick facts about this XS2A Service

* Currently supported latest version of NextGenPSD2 XS2A Implementation Guidelines **1.3.11** and OpenAPI .yaml file **1.3.11_2021-09-24**.<br>
  You can check out our [Swagger API yaml file](xs2a-impl/src/main/resources/static/psd2-api_v1.3.11-2021-10-01v1.yaml).
* All mandatory API endpoints defined in Berlin Group specification are IMPLEMENTED.
* Comes with pluggable **Consent Management System** to store and manage consents given by PSU to corresponding TPPs.
* **ASPSP-Profile** module allows you to configure ASPSP-specific configuration for XS2A-features, for example used SCA approaches, payment products, consent types etc.
* Proven [NISP](https://nisp.online)-compliant implementation.

## Project documentation

[Documentation](doc/index.adoc) provides actual automatically build documentation, such as:
* Architecture documentation
* Use-cases diagrams
* Developer guides
* Release policy
* Roadmap
* Release notes

### Getting Started

* If you want to play with the framework in isolated environment, check out [Modelbank](https://github.com/adorsys/XS2A-Sandbox) Repository.
* To know how to write a connector to your banking system see our [XS2A Connector Examples](https://github.com/adorsys/xs2a-connector-examples) repository and [SPI Developer Guide](<doc/SPI Developer Guide/SPI_Developer_Guide.adoc>)
* [These instructions](doc/GETTING_STARTED.adoc) will help you to get a copy of the project up and running on your local machine for development and testing purposes.

## Development and contributing

Please read [CONTRIBUTING](doc/CONTRIBUTING.md) for details on our code of conduct, and the process for submitting pull requests to us.
List of contributors can be found [here](doc/contributors.md).

## Contact

For commercial support please contact **[adorsys team](https://adorsys.com/en/products/)**.

## License

This project is licensed under Affero GNU General Public License v.3 (AGPL v.3). See the [LICENSE](LICENSE) file for details. For alternative individual licensing options please contact us at psd2@adorsys.com.

