# PrototypeBanking

This project was generated with [Angular CLI](https://github.com/angular/angular-cli) version 6.0.5.

## How to start
1. Start all spring boot servers, database in docker, a local keycloak instance and our pis-webapp on port 4200 (as described in Setup/Development server)
2. Start a fakesmtp server to catch the TAN, which will be sent to the PSU email account. You can use `http://nilhcem.com/FakeSMTP/download.html`. Start it with sudo java -jar fakesmtp.jar`.
3. In `aspsp-xs2a/aspsp-mock-server/src/main/resources/application.properties` set the following properties:
    - spring.mail.host=localhost
    - spring.mail.port=25
    - spring.mail.password=
    - spring.mail.username=
    - spring.mail.properties.mail.smtp.auth=false
    - spring.mail.properties.mail.smtp.starttls.enable=false
4. Get the psu user credentials (login + password) in `http://localhost:28080/swagger-ui.html`. You can find these credentials with the `GET /psu/` endpoint. The user has to match the debtor iban from step 5. A user with these credentials must also exist in your local keycloak instance.
5. Create a new payment in `http://localhost:8080/swagger-ui.html`. The endpoint for the creation is `Payment Initiation Service (PIS)` -> `POST /v1/{payment-service}/{payment-product}`
    - Fill the data as like as the following example:

          "header": {
            "x-request-id": "2f77a125-aa7a-45c0-b414-cea25a116035",
            "psu-ip-address": "192.168.0.26",
            "date": "Sun, 11 Aug 2019 15:02:37 GMT"
          },
          "body": {
            "endToEndIdentification": "WBG-123456789",
            "debtorAccount": {
              "currency": "EUR",
              "iban": "DE52500105173911841934"
            },
            "instructedAmount": {
              "currency": "EUR",
              "amount": "520.00"
            },
            "creditorAccount": {
              "currency": "EUR",
              "iban": "DE15500105172295759744"
            },
            "creditorAgent" : "AAAADEBBXXX",
            "creditorName": "WBG",
            "creditorAddress": {
              "buildingNumber": "56",
              "city": "Nürnberg",
              "country": "DE",
              "postalCode": "90543",
              "street": "WBG Straße"
            },
            "remittanceInformationUnstructured": "Ref. Number WBG-1222"
          }
        
     - The data above creates a payment for the PSU with the IBAN "DE52500105173911841934".
     
6. There is a redirect_link in the response. Open this link in your browser. You should be redirected to our pis-webapp. If your not yet logged in via keycloak, the webapp should redirect you automatically to keycloak, where you have to login with the PSU credentials.
    - ATTENTION: You have to login with the PSU credentials from step 4. 
7. Follow the instructions on the screen.
8. After you have confirmed the payment, you should find an email with the TAN in your fakeSMTP application. Insert the TAN in the TAN input. You have three attempts until the consent will be set revoked and you will be redirected to an error page.
9. After clicking on the Submit button your payment will be confirmed and you should be redirected to the swagger page.

## Setup

### Installation
- Install dependencies with `npm install`
- Start the webapp with the commands described below
- To customize the keycloak login page. Copy the `psd2-login` folder in `src/assets/keycloak-login-theme/` to your themes folder of keycloak and select it in Keycloak
- The keycloak api path can be configured in the `environment.ts` file located in `src/environments`
- The xs2a mockserver and xs2a can be configured in the `environment.ts` file located in `src/environments`
- To run the project in IntelliJ, add a new npm run configuration with `start` command and select the package.json of the project.

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
