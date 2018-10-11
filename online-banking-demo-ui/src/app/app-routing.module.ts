import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AisTanConfirmationPageComponent } from './components/ais-tan-confirmation-page/ais-tan-confirmation-page.component';
import { AisTanConfirmationErrorComponent } from './components/ais-tan-confirmation-error/ais-tan-confirmation-error.component';
import { AisTanConfirmationCanceledComponent } from './components/ais-tan-confirmation-canceled/ais-tan-confirmation-canceled.component';
import { AisConsentConfirmationPageComponent } from './components/ais-consent-confirmation-page/ais-consent-confirmation-page.component';
import { AisConsentConfirmationDeniedComponent } from './components/ais-consent-confirmation-denied/ais-consent-confirmation-denied.component';
import { AppAuthGuard } from './app.authguard';
import { AisTanConfirmationSuccessfulComponent } from './components/ais-tan-confirmation-successful/ais-tan-confirmation-successful.component';
import { AisConsentConfirmationErrorComponent } from './components/ais-consent-confirmation-error/ais-consent-confirmation-error.component';
import { AisHelpPageComponent } from './components/ais-help-page/ais-help-page.component';
import { PisHelpPageComponent } from './components/pis-help-page/pis-help-page.component';
import { PisConsentConfirmationPageComponent } from './components/pis-consent-confirmation-page/pis-consent-confirmation-page.component';
import { PisTanConfirmationCanceledComponent } from './components/pis-tan-confirmation-canceled/pis-tan-confirmation-canceled.component';
import { PisTanConfirmationErrorComponent } from './components/pis-tan-confirmation-error/pis-tan-confirmation-error.component';
import { PisTanConfirmationPageComponent } from './components/pis-tan-confirmation-page/pis-tan-confirmation-page.component';
import { PisConsentConfirmationDeniedComponent } from './components/pis-consent-confirmation-denied/pis-consent-confirmation-denied.component';
import { PisConsentConfirmationSuccessfulComponent } from './components/pis-consent-confirmation-successful/pis-consent-confirmation-successful.component';



const routes: Routes = [
  { path: 'ais', component: AisHelpPageComponent},
  { path: 'ais/consentconfirmationerror', component: AisConsentConfirmationErrorComponent },
  { path: 'ais/consentconfirmationdenied', component: AisConsentConfirmationDeniedComponent },
  { path: 'ais/tanconfirmation', component: AisTanConfirmationPageComponent},
  { path: 'ais/tanconfirmationcanceled', component: AisTanConfirmationCanceledComponent },
  { path: 'ais/tanconfirmationerror', component: AisTanConfirmationErrorComponent },
  { path: 'ais/tanconfirmationsuccessful', component: AisTanConfirmationSuccessfulComponent },
  { path: 'ais/:consentId', component: AisConsentConfirmationPageComponent},
  { path: 'pis', component: PisHelpPageComponent},
  { path: 'pis/:consentId/:paymentId', component: PisConsentConfirmationPageComponent },
  { path: 'pis/tanconfirmationcanceled', component: PisTanConfirmationCanceledComponent },
  { path: 'pis/tanconfirmationerror', component: PisTanConfirmationErrorComponent },
  { path: 'pis/tanconfirmation', component: PisTanConfirmationPageComponent },
  { path: 'pis/consentconfirmationdenied', component: PisConsentConfirmationDeniedComponent },
  { path: 'pis/consentconfirmationsuccessful', component: PisConsentConfirmationSuccessfulComponent },

];

@NgModule({
  imports: [
    RouterModule.forRoot(routes)
  ],
  exports: [
    RouterModule
  ],
  providers: [
    AppAuthGuard
  ]
})
export class AppRoutingModule { }
