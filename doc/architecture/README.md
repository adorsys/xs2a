# Architecture documentation

**About arc42**

arc42, the Template for documentation of software and system
architecture.

By Dr. Gernot Starke, Dr. Peter Hruschka and contributors.

Template Revision: 7.0 EN (based on asciidoc), January 2017

© We acknowledge that this document uses material from the arc 42
architecture template, <http://www.arc42.de>. Created by Dr. Peter
Hruschka & Dr. Gernot Starke.

Introduction and Goals
======================
This implementation provides a REST-interface and corresponding services, that could be operated as a proxy to 
underlying ASPSP-Systems,
providing capabilities to interoperate with TPP by defined XS2A Standard Interface of Berlin Group.


[//]: # (Requirements Overview)
[//]: # (---------------------)

Quality Goals
-------------

| ID    | Prio | Goal                   | Description                                                                                                                                                                                                                                                                     |
|-------|------|------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| QG-01 |   1  | Functional Compliance  | The system should comply (in interface and behaviour) with XS2A Specifications designed and published by Berlin Group. |
| QG-02 |   1  | Adaptability           | The system should be able to perform in the technical environment of ASPSP, regardless of its topology and HW/SW used. This especially concerns database solutions.|
| QG-03 |   2  | Security               | The system should be functional under strict security restrictions provided by ASPSP to environment, deployment and data storage.                                                                                                                                      |
| QG-04 |   3  | Scalability            | Depending on size and number of customers of ASPSP the system environment should be able to scale deployment to satisfy quantitive metric of number of requests (here meant complete use-case, which may contain several technical requests) per user per hour                  |

[//]: # (Stakeholders)
[//]: # (------------)

[//]: # (| Role/Name        | Contact                   | Expectations              |)
[//]: # (|------------------|---------------------------|---------------------------|)
[//]: # (| *&lt;Role-1&gt;* | *&lt;Contact-1&gt;*       | *&lt;Expectation-1&gt;*   |)
[//]: # (| *&lt;Role-2&gt;* | *&lt;Contact-2&gt;*       | *&lt;Expectation-2&gt;*   |)


[//]: # (Architecture Constraints)
[//]: # (========================)

System Scope and Context
========================

Business Context
----------------

![Context diagram](L00-Context.png)

| System                  | Description                                                                                                                                |
|-------------------------|--------------------------------------------------------------------------------------------------------------------------------------------|
| XS2A Service            | The service, implementing XS2A interface of Berlin Group and corresponding BL, including Consent Management.                               |
| TPP                     | Third-party provider, acting on behalf of PSU, operating the accounts/payment data of PSU provided by ASPSP through XS2A Interface.        |
| ASPSP                   | Account Servicing Payment Service Provider, normally a banking system that controls accounts of PSU and performs "classic" banking on it.  |
| ASPSP-Auth              | ASPSP Authentification/Authorisation system. Performs a Strong Customer Authorisation using the security mechanisms of concrete ASPSP.     |
| Certification Authority | External QWAC Certificate provider for TPPs. Defined by local government authority for each EU country.                                    |


[//]: # (**&lt;optionally: Explanation of external domain interfaces&gt;**)

[//]: # (Technical Context)
[//]: # (-----------------)

[//]: # (**&lt;Diagram or Table&gt;**)

[//]: # (**&lt;optionally: Explanation of technical interfaces&gt;**)

[//]: # (**&lt;Mapping Input/Output to Channels&gt;**)

[//]: # (Solution Strategy)
[//]: # (=================)

Building Block View
===================

Whitebox Overall System
-----------------------

![Whitebox overall system](L01-Whitebox.png)

| Component                                          | Description                                                                                                                                |
|----------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------|
| [XS2A-Impl](../../xs2a-impl/README.md)             | an external interface and corresponding validators and services operating to serve TPPs according to a Berlin Group operational rules      |
| [CMS](../../consent-management-system/README.md)   | Third-party provider, acting on behalf of PSU, operating the accounts/payment data of PSU provided by ASPSP through XS2A Interface.        |
| [ASPSP Profile](../../aspsp-profile/README.md)     | a service that provides static configuration of features, those are supported by actual ASPSP                                              |
| [SPI-API](../../spi-api/README.md)                 | internal Java interface to provide a universal way to implement connectors to underlying ASPSP Systems                                     |
| ASPSP-Connector                                    | an implementation of spi-api interface to connect XS2A-services with ASPSP internal system to process requests                             |

[Old component diagram](Whitebox.png)

### Components description

#### TLS Client Certificate Validator
According to the operational rules, connections between TPP and ASPSP must be secured by TLS/HTTPS connection with a 
client certificate. This component should provide the possibility to end SSL-Connection at the first edge before the 
XS2A Service itself.
While this functionality is typical for such firewall services, this module is not included into this project. However 
the client certificate might be required to be included into a HTTP header for following processing on XS2A endpoints.

#### XS2A-Service
The major component that utilizes XS2A-compliant requests to ASPSP, performing their processing according to operational
rules specified by Berlin Group. Based on the features supported by the ASPSP and configured by the ASPSP profile 
perfoms all necessary operations to process TPP request, including its technical and business validation, logging and 
serving corresponding redirect links in case of need.
Includes ASPSP-specific implementation of SPI-API interface to communicate processing orders to ASPSP systems.
 
Technically utilizes connections to ASPSP systems, as well as to ASPSP Profile and Consent Management System.

#### ASPSP-Profile
ASPSP-Profile serves a configuration of XS2A features and options supported by the actual ASPSP to other components. 
Normally this configuration is highly dependend from the core banking systems, therefore is not changed very often.
A list of supported features should be provided by a developers guide of ASPSP.

#### Consent Web-App
Provides end-user interface to perform operations on the user's consents, including granting a consents, listing given 
consents with their status and attributes and revoking consents. Due to high coupling with the other banking systems 
this application is to be developed by every ASPSP itself.

#### Consent Management System
Stores and manages (granting, listening, using and revoking) various consents granted by the PSU to TPP to access 
resources owned by PSU at the ASPSP. Normally this includes also logging access and usage of consents. 
Provides a relational DB storage connector.

#### ASPSP (Core banking systems)
References to core banking systems responsible for processing payments, accounts management and other typical banking 
processes. In XS2A Service ASPSP acts as an end-actor, who is reponsible to do the end-job with user data, based on TPP 
request and consents given TPP by PSU.

***&lt;Overview Diagram&gt;***

Motivation

:   *&lt;text explanation&gt;*

Contained Building Blocks

:   *&lt;Description of contained building block (black boxes)&gt;*

Important Interfaces

:   *&lt;Description of important interfaces&gt;*

### &lt;Name black box 1&gt; {#__name_black_box_1}

*&lt;Purpose/Responsibility&gt;*

*&lt;Interface(s)&gt;*

*&lt;(Optional) Quality/Performance Characteristics&gt;*

*&lt;(Optional) Directory/File Location&gt;*

*&lt;(Optional) Fulfilled Requirements&gt;*

*&lt;(optional) Open Issues/Problems/Risks&gt;*

### &lt;Name black box 2&gt; {#__name_black_box_2}

*&lt;black box template&gt;*

### &lt;Name black box n&gt; {#__name_black_box_n}

*&lt;black box template&gt;*

### &lt;Name interface 1&gt; {#__name_interface_1}

…

### &lt;Name interface m&gt; {#__name_interface_m}

Level 2 {#_level_2}
-------

### White Box *&lt;building block 1&gt;* {#_white_box_emphasis_building_block_1_emphasis}

*&lt;white box template&gt;*

### White Box *&lt;building block 2&gt;* {#_white_box_emphasis_building_block_2_emphasis}

*&lt;white box template&gt;*

…

### White Box *&lt;building block m&gt;* {#_white_box_emphasis_building_block_m_emphasis}

*&lt;white box template&gt;*

Level 3 {#_level_3}
-------

### White Box &lt;\_building block x.1\_&gt; {#_white_box_building_block_x_1}

*&lt;white box template&gt;*

### White Box &lt;\_building block x.2\_&gt; {#_white_box_building_block_x_2}

*&lt;white box template&gt;*

### White Box &lt;\_building block y.1\_&gt; {#_white_box_building_block_y_1}

*&lt;white box template&gt;*

Runtime View {#section-runtime-view}
============

&lt;Runtime Scenario 1&gt; {#__runtime_scenario_1}
--------------------------

-   *&lt;insert runtime diagram or textual description of the
    scenario&gt;*

-   *&lt;insert description of the notable aspects of the interactions
    between the building block instances depicted in this diagram.&gt;*

&lt;Runtime Scenario 2&gt; {#__runtime_scenario_2}
--------------------------

… {#_}
-

&lt;Runtime Scenario n&gt; {#__runtime_scenario_n}
--------------------------

Deployment View {#section-deployment-view}
===============

Infrastructure Level 1 {#_infrastructure_level_1}
----------------------

***&lt;Overview Diagram&gt;***

Motivation

:   *&lt;explanation in text form&gt;*

Quality and/or Performance Features

:   *&lt;explanation in text form&gt;*

Mapping of Building Blocks to Infrastructure

:   *&lt;description of the mapping&gt;*

Infrastructure Level 2 {#_infrastructure_level_2}
----------------------

### *&lt;Infrastructure Element 1&gt;* {#__emphasis_infrastructure_element_1_emphasis}

*&lt;diagram + explanation&gt;*

### *&lt;Infrastructure Element 2&gt;* {#__emphasis_infrastructure_element_2_emphasis}

*&lt;diagram + explanation&gt;*

…

### *&lt;Infrastructure Element n&gt;* {#__emphasis_infrastructure_element_n_emphasis}

*&lt;diagram + explanation&gt;*

Cross-cutting Concepts {#section-concepts}
======================

*&lt;Concept 1&gt;* {#__emphasis_concept_1_emphasis}
-------------------

*&lt;explanation&gt;*

*&lt;Concept 2&gt;* {#__emphasis_concept_2_emphasis}
-------------------

*&lt;explanation&gt;*

…

*&lt;Concept n&gt;* {#__emphasis_concept_n_emphasis}
-------------------

*&lt;explanation&gt;*

Design Decisions {#section-design-decisions}
================

Quality Requirements {#section-quality-scenarios}
====================

Quality Tree {#_quality_tree}
------------

Quality Scenarios {#_quality_scenarios}
-----------------

Risks and Technical Debts {#section-technical-risks}
=========================

Glossary {#section-glossary}
========

| Term                  | Definition                                                                                                                                  |
|-----------------------|---------------------------------------------------------------------------------------------------------------------------------------------|
| ASPSP                 | *Account Servicing Payment Service Provider*, a banking system that holds account data of PSU and peforms corresponding operations over it.          |
| PSD2                  | [Payment Service Directive 2](https://ec.europa.eu/info/law/payment-services-psd-2-directive-eu-2015-2366_en), a european law for payments services.  |
| PSU                   | *Payment Service User*, end user, on behalf of and in the interests of which, the interaction is carried out.                                           |
| TPP                   | *Third-party Provider*, *Third-party Payment Service provider*, a system that acts on behalf of PSU on its data hold by ASPSP.                          |
| XS2A                  | A name of an interface, defined by [Berlin Group](https://www.berlin-group.org/psd2-access-to-bank-accounts) to comply banks to the requirements of PSD2|







