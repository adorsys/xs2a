# Release notes v. 1.12

## Extract CMS web-endpoints to different maven modules
A SPI Developer now can decide what endpoints on CMS side he needs.
There are three modules with different endpoints to serve different possible purposes of CMS.
* consent-xs2a-web is used to provide endpoints for xs2a-service. 
This is normally needed if CMS is deployed as a separate service.
* consent-psu-web is used to provide endpoints for presentation and work with consents in some PSU application, 
i.e. online-banking system. This module can be used in any setup of CMS (embedded and standalone).
* consent-aspsp-web is used to provide other endpoints available for banking systems.
This module can be used in any setup of CMS (embedded and standalone).

## Log TPP requests and responses
Now all the requests and responses from TPP to XS2A are logged.
To make logging work, one of the following options should be done:
* logback file (logback.xml, logback-spring.xml or logback.groovy (if Groovy is on the classpath)) should be created inside the project;
* for external logback location (one of the following): 
    * in application.property file path to logback file should be provided (pattern - 'file:/path/to/file/logback.xml' (e.g. - file:/Users/adorsys/Documents/configs/logback.xml)):
        ```text
        logging.config=file:/path/to/file/logback.xml
        ```
    * VM option should be added to ASPSPXs2aApplication:
        ```text
        -Dlogging.config=/path/to/file/logback.xml
        ```
