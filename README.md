# Reference Java implementation of NextGenPSD2 XS2A Interface of Berlin Group
[![Build Status](https://travis-ci.com/adorsys/xs2a.svg?branch=develop)](https://travis-ci.com/adorsys/xs2a)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=adorsys_xs2a&metric=alert_status)](https://sonarcloud.io/dashboard?id=adorsys_xs2a)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=adorsys_xs2a&metric=coverage)](https://sonarcloud.io/dashboard?id=adorsys_xs2a)

## Licensing model change to dual license: AGPL v.3 or commercial license

**Attention: this open-source project will change its licensing model as of 01.01.2022!**

Constantly evolving and extending scope, production traffic and support in open banking
world call for high maintenance and service investments on our part.
Henceforth, adorsys will offer all versions higher than v.12.4 of this XS2A-CORE framework
under a dual-license model. Thus, this repository will be available either under Affero GNU
General Public License v.3 (AGPL v.3) or alternatively under a commercial license agreement.

We always strive to provide highest quality solutions for our users and customers. Our XS2A
Core has helped many financial institutions to achieve the necessary regulatory compliance,
which places us among the most proven and reliable service providers in the market.

We would like to thank all our users for their trust so far and are convinced that we will be
able to provide an even better service going forward.

For more information, advice for your XS2A implementation project or if your use case
requires more time to adapt this change, please contact us at psd2@adorsys.com.

For additional details please see the section [FAQ on Licensing Change](https://github.com/adorsys/xs2a#faq-on-licensing-change).

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

* Currently supported version of NextGenPSD2 XS2A Implementation Guidelines **1.3.11_20210924** and OpenAPI .yaml file **1.3.11_20211001v1**.<br>
You can check out our [Swagger API yaml file](xs2a-impl/src/main/resources/static/psd2-api_v1.3.11-2021-10-01v1.yaml).
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

This project is licensed under the Apache License version 2.0 **(until 01.01.2022)** - see the [LICENSE](LICENSE) file for details

## FAQ on Licensing Change

**What is a dual-licensing model?**

Under a dual-licensing model, our product is available under two licenses:
1. [The Affero GNU General Public License v3 (AGPL v3)](https://www.gnu.org/licenses/agpl-3.0.en.html)
2. A proprietary commercial license. 
   
If you are a developer or business that would like to review our products in detail, test and
   implement in your open-source projects and share the changes back to the community, the product
   repository is freely available under AGPL v3.
   
If you are a business that would like to implement our products in a commercial setting and would
   like to protect your individual changes, we offer the option to license our products under a
   commercial license.
   
This change will still allow free access and ensure openness under AGPL v3 but with assurance of
   committing any alterations or extensions back to the project and preventing redistribution of such
   implementations under commercial license.

**Will there be any differences between the open-source and commercially licensed versions of your products?**

Our public release frequency will be reduced as our focus shifts towards the continuous
   maintenance of the commercial version. Nevertheless, we are committed to also provide
   open-source releases of our products on a regular basis as per our release policy.
   For customers with a commercial license, we will offer new intermediate releases in a more
   frequent pace.
   
**Does this mean that this product is no longer open source?**
   
No, the product will still be published and available on GitHub under an OSI-approved open-source
   license (AGPL v3).

**What about adorsys’ commitment to open source? Will adorsys provide future product releases on GitHub?**

We at adorsys are committed to continue actively participating in the open-source community. Our
products remain licensed under OSI-approved open-source licenses, and we are looking forward to
expanding our product portfolio on GitHub even further.

**How does the change impact me if I already use the open-source edition of your product?**

All currently published versions until v.12.4 will remain under their current Apache 2.0 license and its
respective requirements and you may continue using it as-is. To upgrade to future versions, you will
be required to either abide by the requirements of AGPL v3, including documenting and sharing your
implemented changes to the product when distributing, or alternatively approach us to obtain a
commercial license.

**What if I cannot adjust to the new licensing model until 01.01.2022? Can I extend the deadline?**

We understand that adjustment to licensing changes can take time and therefore are open to discuss
extension options on an individual basis. For inquiries please contact us as psd2@adorsys.com.

**Which versions of the product are affected?**

All versions of XS2A-Core starting after v.12.4 will be affected by the licensing changes and move to a
dual-licensing model.

**What will happen to older, Apache 2.0 licensed product versions?**

All older Apache 2.0 licensed versions prior and including v.12.4 will remain available under their
existing license.

**What open-source products from Adorsys are affected by the licensing change?**

The following products are affected:
- [XS2A Core](https://github.com/adorsys/xs2a)
- [XS2A Sandbox & ModelBank](https://github.com/adorsys/XS2A-Sandbox)
- [Open Banking Gateway](https://github.com/adorsys/open-banking-gateway) incl. [XS2A Adapters](https://github.com/adorsys/xs2a-adapter)
- [SmartAnalytics](https://github.com/adorsys/smartanalytics)
- [Datasafe](https://github.com/adorsys/datasafe)

**I’m using one of these products indirectly via some software integrator. How does the licensing
change affect me?**

The licensing change does not affect you as user, but it is relevant to your provider who has used our
product in their solution implementation. In case of uncertainty please contact your service provider
or approach us at psd2@adorsys.com.
