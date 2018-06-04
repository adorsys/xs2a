# Architecture documentation

### Brief architecture description

This implementation provides a REST-interface and underlying services, that could be operated as a proxy to underlying ASPSP-Systems,
providing capabilities to interoperate with TPP by defined XS2A Standard Interface of Berlin Group.
![Component diagram](Whitebox.png)

[Whitebox.png]: doc/architecture/Whitebox.png "Component diagram"

The modules provided by this implementation are:
* [**xs2a-impl**](../../xs2a-impl/README.md) - a REST-interface and services operating to serve TPPs
* [**aspsp-idp**](../../aspsp-idp/README.md) - reference implementation of ASPSP Identity provider (IDP) based on Keycloak
* [**aspsp-mock-server**](../../aspsp-mock-server/README.md) - a mock-implementation of ASPSP for the purposes of testing and introspecting of xs2a-functionality
* **spi-api** - internal Java interface to provide a universal way to implement connectors to underlying ASPSP Systems
* **spi-mock** - an implementation of spi-api interface to connect XS2A-services with ASPSP-Mock-Server

