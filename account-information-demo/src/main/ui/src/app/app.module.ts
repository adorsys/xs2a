import { BrowserModule } from '@angular/platform-browser';
import { NgModule, APP_INITIALIZER, LOCALE_ID } from '@angular/core';
import { HttpClientModule } from '@angular/common/http';
import { AppRoutingModule } from './app-routing.module';

import { AppComponent } from './app.component';
import { ConsentConfirmationPageComponent } from './components/consent-confirmation-page/consent-confirmation-page.component';
import { TanConfirmationPageComponent } from './components/tan-confirmation-page/tan-confirmation-page.component';
import { ConsentConfirmationErrorComponent } from './components/consent-confirmation-error/consent-confirmation-error.component';
import { ConsentConfirmationDeniedComponent } from './components/consent-confirmation-denied/consent-confirmation-denied.component';
import { TanConfirmationCanceledComponent } from './components/tan-confirmation-canceled/tan-confirmation-canceled.component';
import { TanConfirmationErrorComponent } from './components/tan-confirmation-error/tan-confirmation-error.component';
import { TanConfirmationSuccessfulComponent } from './components/tan-confirmation-successful/tan-confirmation-successful.component';
import { initializer } from './utils/app-init';
import { KeycloakAngularModule, KeycloakService } from '../../node_modules/keycloak-angular';
import { FormsModule } from '@angular/forms';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { registerLocaleData } from '@angular/common'
import localeDE from '@angular/common/locales/de';
import { HelpPageComponent } from './components/help-page/help-page.component';

registerLocaleData(localeDE)



@NgModule({
  declarations: [
    AppComponent,
    TanConfirmationPageComponent,
    TanConfirmationErrorComponent,
    TanConfirmationCanceledComponent,
    TanConfirmationSuccessfulComponent,
    ConsentConfirmationPageComponent,
    ConsentConfirmationDeniedComponent,
    ConsentConfirmationErrorComponent,
    HelpPageComponent,
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
    deps: [KeycloakService],
  }, {
    provide: LOCALE_ID, useValue: 'de'
  },
  ],
  bootstrap: [AppComponent],
})
export class AppModule { }
