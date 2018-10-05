import { BrowserModule } from '@angular/platform-browser';
import { APP_INITIALIZER, LOCALE_ID, NgModule } from '@angular/core';
import { HttpClientModule } from '@angular/common/http';
import { AppRoutingModule } from './app-routing.module';

import { AppComponent } from './app.component';
import { AisConsentConfirmationPageComponent } from './components/ais-consent-confirmation-page/ais-consent-confirmation-page.component';
import { AisTanConfirmationPageComponent } from './components/ais-tan-confirmation-page/ais-tan-confirmation-page.component';
import { AisConsentConfirmationErrorComponent } from './components/ais-consent-confirmation-error/ais-consent-confirmation-error.component';
import { AisConsentConfirmationDeniedComponent } from './components/ais-consent-confirmation-denied/ais-consent-confirmation-denied.component';
import { AisTanConfirmationCanceledComponent } from './components/ais-tan-confirmation-canceled/ais-tan-confirmation-canceled.component';
import { AisTanConfirmationErrorComponent } from './components/ais-tan-confirmation-error/ais-tan-confirmation-error.component';
import { AisTanConfirmationSuccessfulComponent } from './components/ais-tan-confirmation-successful/ais-tan-confirmation-successful.component';
import { initializer } from './utils/app-init';
import { KeycloakAngularModule, KeycloakService } from 'keycloak-angular';
import { FormsModule } from '@angular/forms';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { registerLocaleData } from '@angular/common';
import { AisHelpPageComponent } from './components/ais-help-page/ais-help-page.component';
import { PisTanConfirmationErrorComponent } from './components/pis-tan-confirmation-error/pis-tan-confirmation-error.component';
import { PisConsentConfirmationDeniedComponent } from './components/pis-consent-confirmation-denied/pis-consent-confirmation-denied.component';
import { PisConsentConfirmationPageComponent } from './components/pis-consent-confirmation-page/pis-consent-confirmation-page.component';
import { PisTanConfirmationPageComponent } from './components/pis-tan-confirmation-page/pis-tan-confirmation-page.component';
import { PisTanConfirmationCanceledComponent } from './components/pis-tan-confirmation-canceled/pis-tan-confirmation-canceled.component';
import { PisConsentConfirmationSuccessfulComponent } from './components/pis-consent-confirmation-successful/pis-consent-confirmation-successful.component';
import { PisConsentConfirmationErrorComponent } from './components/pis-consent-confirmation-error/pis-consent-confirmation-error.component';
import { PisHelpPageComponent } from './components/pis-help-page/pis-help-page.component';
import { ConfigService } from './service/config.service';
import localeDE from '@angular/common/locales/de';


registerLocaleData(localeDE);

@NgModule({
  declarations: [
    AppComponent,
    AisTanConfirmationPageComponent,
    AisTanConfirmationErrorComponent,
    AisTanConfirmationCanceledComponent,
    AisTanConfirmationSuccessfulComponent,
    AisConsentConfirmationPageComponent,
    AisConsentConfirmationDeniedComponent,
    AisConsentConfirmationErrorComponent,
    AisHelpPageComponent,
    PisTanConfirmationPageComponent,
    PisTanConfirmationErrorComponent,
    PisTanConfirmationCanceledComponent,
    PisConsentConfirmationPageComponent,
    PisConsentConfirmationDeniedComponent,
    PisConsentConfirmationSuccessfulComponent,
    PisConsentConfirmationErrorComponent,
    PisHelpPageComponent
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    AppRoutingModule,
    KeycloakAngularModule,
    FormsModule,
    NgbModule.forRoot(),
  ],
  providers: [{
    provide: APP_INITIALIZER,
    useFactory: initializer,
    multi: true,
    deps: [KeycloakService, ConfigService],
  }, {
    provide: LOCALE_ID, useValue: 'de'
  }],
  bootstrap: [AppComponent],
})
export class AppModule { }
