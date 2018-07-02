# A reference implementation of Identity provider using [Keycloak](https://www.keycloak.org)

This module extends Keycloak with several features required by PSD2 and XS2A specifications:
* openid-connect dynamic client registration
* client certificate validation

## Deployment using Docker container

1. Build a module and a docker container
```bash
$ mvn clean install
$ docker build -t adorsys/keycloak-xs2a:dev .
```
2. Since this implemenation is based on a Keycloak server, please refer general keycloak documentaion to perform it's installation and configuration

### Configuring WildFly behind a reverse proxy with TLS
To allow keycloak to act under a secured reverse proxy we should set enviroment key in Docker configuration file or in the docker container runtime environment:  
```
ENV PROXY_ADDRESS_FORWARDING true
```

## Local configuration of a keycloak
1. Start keycloak container:
```
docker run -p 8080:8080 -e KEYCLOAK_USER=admin -e KEYCLOAK_PASSWORD=admin123 adorsys/keycloak-xs2a:dev
```
2. Create and configure clients
```
curl -X POST \
  http://localhost:8080/auth/realms/demobank/clients-registrations/openid-connect \
  -H 'content-type: application/json' \
  -d '{
	"client_name":"XS2A Client",
	"redirect_uris":["*"]
}’

{
    "redirect_uris": [
        "*"
    ],
    "token_endpoint_auth_method": "client_secret_basic",
    "grant_types": [
        "authorization_code",
        "refresh_token"
    ],
    "response_types": [
        "code",
        "none"
    ],
    "client_id": "9fee4b6e-6216-4e48-8839-dec2de38ac22",
    "client_secret": "bd270b52-b718-4eb0-ae68-734a6d9e18b0",
    "client_name": "XS2A Client",
    "subject_type": "public",
    "client_id_issued_at": 1520334677,
    "client_secret_expires_at": 0,
    "registration_client_uri": "http://localhost:8080/auth/realms/demobank/clients-registrations/openid-connect/9fee4b6e-6216-4e48-8839-dec2de38ac22",
    "registration_access_token": "eyJhbGciOiJSUzI1NiIsImtpZCIgOiAiRGg5NmZTZ2pLZnQyLXl4RFlSWmhPQy1TbFpoUGU2akRFdGxwTnplanJSYyJ9.eyJqdGkiOiI4NDMzYzY1Ny1kMjM0LTQ1MTUtOTZlYS0zZGE4MWJlMzNjYjYiLCJleHAiOjAsIm5iZiI6MCwiaWF0IjoxNTIwMzM0Njc3LCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvYXV0aC9yZWFsbXMvZGVtb2JhbmsiLCJhdWQiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvYXV0aC9yZWFsbXMvZGVtb2JhbmsiLCJ0eXAiOiJSZWdpc3RyYXRpb25BY2Nlc3NUb2tlbiIsInJlZ2lzdHJhdGlvbl9hdXRoIjoiYW5vbnltb3VzIn0.grel1LxPLJSeCilpxn09MLcllPqV2__0zPvM3VUypJV6q1cuEYqLaiLSI5oNPnXrcVIjS9Kko8ZnmmrxxOB157TGd2ZETJpUU7np5Kieo-k6-woxA4rOYmfLQuIj8MfhBY8rPnioMyWsN5Cg090aHkz_SMzFp9Qb8IDUEzUV6YGuZwllMErqDPg5_3GMYqYOs1yLyDDVAs0p4qF3pxS5StC4bw9abI_BRv2ouO_k9G_z0x6G7EzwTBu9-8slPwUyb4SPDYpyMADA57BcnCvu3D-G77Wrqkz_x18KaZG_UGk6DcgCfwEiX4Fr1p8YrMJh3QsP-EHejEI4-5MoEy3ajg"
}

```
