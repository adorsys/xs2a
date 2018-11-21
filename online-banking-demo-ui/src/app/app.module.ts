import { BrowserModule } from '@angular/platform-browser';
import { APP_INITIALIZER, LOCALE_ID, NgModule } from '@angular/core';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { registerLocaleData } from '@angular/common';
import localeDE from '@angular/common/locales/de';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { initializer } from './utils/app-init';
import { KeycloakAngularModule, KeycloakService } from 'keycloak-angular';
import { ConfigService } from './service/config.service';

registerLocaleData(localeDE);

@NgModule({
  declarations: [
    AppComponent
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
