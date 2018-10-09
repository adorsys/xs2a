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
4. Get the psu user credentials (login + password) in `http://localhost:28080/swagger-ui.html`. You can find these credentials with the `GET /psu/` endpoint. The user has to match the iban of the user (Step 5: dedicated accounts consent). A user with these credentials must also exist in your local keycloak instance.
5. Create a new consent in `http://localhost:8080/swagger-ui.html`. The endpoint for the creation is `AISP, Consents` -> `POST /api/v1/consents`. There are two different types of consents. *Bank Offered Consents* and *Dedicated Accounts Consents*
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
    ```
   - **Dedicated Accounts Consent**
   ```
   {
      "access": {
        "accounts": [
          {
            "currency": "EUR",
            "iban": "DE52500105173911841934"
          }
        ],
        "balances": [
          {
            "currency": "EUR",
            "iban": "DE52500105173911841934"
          }
        ],
        "transactions": [
          {
            "currency": "EUR",
            "iban": "DE52500105173911841934"
          }
        ]
      },
      "combinedServiceIndicator": false,
      "frequencyPerDay": 400,
      "recurringIndicator": false,
      "validUntil": "2018-11-30"
    }
   ```

6. There is a redirect_link in the response. Open this link in your browser. You should be redirected to our ais-webapp. If your not yet logged in via keycloak, the webapp should redirect you automatically to keycloak, where you have to login with the PSU credentials.
    - ATTENTION: You have to login with the PSU credentials from step 4. 
7. Follow the instructions on the screen. If you have created a *Bank offered Consent* you should find checkboxes for every account the user owns. Select the accounts you want to give the tpp access to.
8. After you have confirmed your accounts, you should find an email with the TAN in your fakeSMTP application. Insert the TAN in the TAN input. You have three attempts until the consent will be set revoked and you will be redirected to an error page.
9. After clicking on the Submit button your payment will be confirmed and you should be redirected to the swagger page.
    

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

## Deployment
To deploy the webapp on a deployment environment, the following steps are needed:
- The webapp uses a proxy (e.g. nginx ) which redirects backend service calls to the correct urls.
- The following services are available and must be redirected: 
``` 
  - XS2A_URL
  - MOCKSERVER_URL
  - ONLINE_BANKING_SERVER_URL
  - CONSENT_MANAGEMENT_URL
  - PROFILE_SERVER_URL
  ```
- Build the docker container: `docker build -t online-banking-demo-ui .`
- Run the docker container with the environment variables you need: 
`docker run -e XS2A_URL='http://docker.for.mac.localhost:8080' -e MOCKSERVER_URL='http://docker.for.mac.localhost:28080' -e ONLINE_BANKING_SERVER_URL='http://docker.for.mac.localhost:28081' -e CONSENT_MANAGEMENT_URL='http://docker.for.mac.localhost:38080' -e PROFILE_SERVER_URL='http://docker.for.mac.localhost:48080' -p 4200:4200 --name online-banking-demo-ui -d online-banking-demo-ui`
- The docker container should now serve the content on port 4200
