# Deployment using Docker containers

Your deployment scheme depends on the SCA approach you are using.

## OAuth2 SCA Approach
Currently to perform deployment we use oauth2 approach.
To use it you need to deploy following components (please see configuration instructions in the each component documentation):
* [xs2a-impl](../xs2a-impl/README.md) (the XS2A service itself)
* [aspsp-idp](../aspsp-idp/README.md) (a reference implementation of identity provider)
* [aspsp-mock-server](../aspsp-mock-server/README.md) (a mock that simulates behaviour of ASPSP internal systems)


