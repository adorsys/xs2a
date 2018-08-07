# PrototypeBanking

This project was generated with [Angular CLI](https://github.com/angular/angular-cli) version 6.0.5.

## Installation
- Install dependencies with `npm install`
- Start the webapp with the commands described below
- To customize the keycloak login page. Copy the `psd2-login` folder in `src/assets/keycloak-login-theme/` to your themes folder of keycloak and select it in Keycloak
- The keycloak api path can be configured in the `environment.ts` file located in `src/environments`
- The xs2a mockserver and xs2a can be configured in the `environment.ts` file located in `src/environments`
- To run the project in IntelliJ, add a new npm run configuration with `start` command and select the package.json of the project.

## Development server

Run `ng serve` for a dev server. Navigate to `http://localhost:4200/`. The app will automatically reload if you change any of the source files.

## Code scaffolding

Run `ng generate component component-name` to generate a new component. You can also use `ng generate directive|pipe|service|class|guard|interface|enum|module`.

## Build

Run `ng build` to build the project. The build artifacts will be stored in the `dist/` directory. Use the `--prod` flag for a production build.

## Running unit tests

Run `ng test` to execute the unit tests via [Karma](https://karma-runner.github.io).

## Running end-to-end tests

Run `ng e2e` to execute the end-to-end tests via [Protractor](http://www.protractortest.org/).

## Further help

To get more help on the Angular CLI use `ng help` or go check out the [Angular CLI README](https://github.com/angular/angular-cli/blob/master/README.md).
