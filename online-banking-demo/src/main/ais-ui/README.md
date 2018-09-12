# Ui

This project was generated with [Angular CLI](https://github.com/angular/angular-cli) version 6.0.5.

## How to start
1. Start all spring boot servers, database in docker, a local keycloak instance and our ais-webapp on port 4200 (as described in Setup/Development server)
2. Start a fakesmtp server to catch the TAN, which will be sent to the PSU email account. You can use `http://nilhcem.com/FakeSMTP/download.html`. Start it with sudo java -jar fakesmtp.jar`.
3. In `aspsp-xs2a/aspsp-mock-server/src/main/resources/application.properties` set the following properties:
    - spring.mail.host=localhost
    - spring.mail.port=25
    - spring.mail.password=
    - spring.mail.username=
    - spring.mail.properties.mail.smtp.auth=false
    - spring.mail.properties.mail.smtp.starttls.enable=false
3. Get the psu user credentials (login + password) in `http://localhost:28080/swagger-ui.html`. You can find these credentials with the `GET /psu/` endpoint. The user has to match the debtor iban from step 3.
4. Create a new consent in `http://localhost:8080/swagger-ui.html`. The endpoint for the creation is `AISP, Consents` -> `POST /api/v1/consents`. There are two different types of consents. *Bank Offered Consents* and *Dedicated Accounts Consents*
    - **Bank Offered Consent**
    ```
    {
    "access": {
      "accounts": [
    
      ],
      "balances": [
    
      ],
      "transactions": [
    
      ]
    },
    "combinedServiceIndicator": true,
    "frequencyPerDay": 4,
    "recurringIndicator": true,
    "validUntil": "2019-10-30"
    }
    ``
    






## Setup

### Development server

Run `ng serve` for a dev server. Navigate to `http://localhost:4200/`. The app will automatically reload if you change any of the source files.

### Code scaffolding

Run `ng generate component component-name` to generate a new component. You can also use `ng generate directive|pipe|service|class|guard|interface|enum|module`.

### Build

Run `ng build` to build the project. The build artifacts will be stored in the `dist/` directory. Use the `--prod` flag for a production build.

### Running unit tests

Run `ng test` to execute the unit tests via [Karma](https://karma-runner.github.io).

### Running end-to-end tests

Run `ng e2e` to execute the end-to-end tests via [Protractor](http://www.protractortest.org/).

### Further help

To get more help on the Angular CLI use `ng help` or go check out the [Angular CLI README](https://github.com/angular/angular-cli/blob/master/README.md).
