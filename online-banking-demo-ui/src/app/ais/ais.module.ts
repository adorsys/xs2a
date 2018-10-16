import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { routes } from './ais.routing';
import { AisTanConfirmationPageComponent } from './ais-tan-confirmation-page/ais-tan-confirmation-page.component';
import { AisTanConfirmationErrorComponent } from './ais-tan-confirmation-error/ais-tan-confirmation-error.component';
import { AisTanConfirmationCanceledComponent } from './ais-tan-confirmation-canceled/ais-tan-confirmation-canceled.component';
import { AisTanConfirmationSuccessfulComponent } from './ais-tan-confirmation-successful/ais-tan-confirmation-successful.component';
import { AisConsentConfirmationPageComponent } from './ais-consent-confirmation-page/ais-consent-confirmation-page.component';
import { AisConsentConfirmationErrorComponent } from './ais-consent-confirmation-error/ais-consent-confirmation-error.component';
import { AisHelpPageComponent } from './ais-help-page/ais-help-page.component';
import { AisConsentConfirmationDeniedComponent } from './ais-consent-confirmation-denied/ais-consent-confirmation-denied.component';
import { FormsModule } from '@angular/forms';

@NgModule({
  imports: [
    CommonModule,
    RouterModule.forChild(routes),
    FormsModule
  ],
  declarations: [
    AisTanConfirmationPageComponent,
    AisTanConfirmationErrorComponent,
    AisTanConfirmationCanceledComponent,
    AisTanConfirmationSuccessfulComponent,
    AisConsentConfirmationPageComponent,
    AisConsentConfirmationDeniedComponent,
    AisConsentConfirmationErrorComponent,
    AisHelpPageComponent
  ]
})
export class AisModule { }
