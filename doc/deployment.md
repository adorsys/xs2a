# Configuring WildFly behind a reverse proxy with TLS
To allow keycloak to act under a secured reverse proxy we should set enviroment key in Docker configuration file  
```
ENV PROXY_ADDRESS_FORWARDING true
```
