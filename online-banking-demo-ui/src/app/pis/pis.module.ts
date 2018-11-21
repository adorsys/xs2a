import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { routes } from './pis.routing';
import { PisConsentConfirmationDeniedComponent } from './pis-consent-confirmation-denied/pis-consent-confirmation-denied.component';
import { PisConsentConfirmationErrorComponent } from './pis-consent-confirmation-error/pis-consent-confirmation-error.component';
import { PisConsentConfirmationPageComponent } from './pis-consent-confirmation-page/pis-consent-confirmation-page.component';
import { PisConsentConfirmationSuccessfulComponent } from './pis-consent-confirmation-successful/pis-consent-confirmation-successful.component';
import { PisHelpPageComponent } from './pis-help-page/pis-help-page.component';
import { PisTanConfirmationCanceledComponent } from './pis-tan-confirmation-canceled/pis-tan-confirmation-canceled.component';
import { PisTanConfirmationErrorComponent } from './pis-tan-confirmation-error/pis-tan-confirmation-error.component';
import { PisTanConfirmationPageComponent } from './pis-tan-confirmation-page/pis-tan-confirmation-page.component';
import { FormsModule } from '@angular/forms';

@NgModule({
  imports: [
    CommonModule,
    RouterModule.forChild(routes),
    FormsModule
  ],
  declarations: [
    PisConsentConfirmationDeniedComponent,
    PisConsentConfirmationErrorComponent,
    PisConsentConfirmationPageComponent,
    PisConsentConfirmationSuccessfulComponent,
    PisHelpPageComponent,
    PisTanConfirmationCanceledComponent,
    PisTanConfirmationErrorComponent,
    PisTanConfirmationPageComponent
  ]
})
export class PisModule { }
