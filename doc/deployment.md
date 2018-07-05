# Extension and deployment

To use the XS2A implementation in your environment, following steps to be perfomed:
1. Choose a SCA Approach, that follows your needs and capabilities. Currently we support OAuth and Redirect approaches.
2. Depending on the approach make a project to implement a required part on the ASPSP side, preparing your systems to interact with XS2A
3. Setup the environment and configure it using ASPSP Profile
4. Make own implementation for SPI-API interface to connect XS2A Service to your core systems

## Deployment using Docker containers

We suggest you to deploy the services using the Docker container. We provide Dockerfiles for every module we develop.
We use Docker images in the OpenShift environment for development and testing purposes.

Since most of our services are written in pure Java, you can also choose another ways to deploy the applications in your environment.
Please refer to the documentation pages of each service to perform its configuration.

## Services for OAuth2 SCA Approach
Following our services are required to be deployed for OAuth2 approach:
* Identity provider. You can use any provider that supports OAuth2 protocol. We recommend to use our **[aspsp-idp](../aspsp-idp/README.md)** module, which is based on a Keycloak with some extensions. Our solution is being tested using this module as IDP.
* **[aspsp-profile](../aspsp-profile/README.md)** - A service that provides a static configuration of optional PSD2 features and corresponding internal configuration that your ASPSP supports. I.e. one can configure redirect URLs or supported payment types.
More information about optional features you can find in the Berlin Group XS2A Guidelines and Operational rules.
* **[xs2a-impl](../xs2a-impl/README.md)** - The XS2A service itself
* **[consent-management-system](../consent-management-system/README.md)** - Consents storage and corresponding managing services. A key component for PSD2 Compliance.
Requires underlying relative database (We use PostgreSQL for development and testing).

## Services for Redirect Approach
Following our services are required to be deployed for Redirect approach:
* **[aspsp-profile](../aspsp-profile/README.md)** - A service that provides a static configuration of optional PSD2 features and corresponding internal configuration that your ASPSP supports. I.e. one can configure redirect URLs or supported payment types.
More information about optional features you can find in the Berlin Group XS2A Guidelines and Operational rules.
* **[xs2a-impl](../xs2a-impl/README.md)** - The XS2A service itself
* **[consent-management-system](../consent-management-system/README.md)** - Consents storage and corresponding managing services. A key component for PSD2 Compliance.
Requires underlying relative database (We use PostgreSQL for development and testing).
* You would also need to implement a consent confirmation and consent listing pages on your local online application to be able to redirect TPP requests to that pages.

## Services for development and testing purposes
* [aspsp-mock-server](../aspsp-mock-server/README.md) To have a quick start you may deploy the mock server (requires undelying MongoDB). In that case you'd need to use mockspi profile in xs2a service


