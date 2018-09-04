import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { TanConfirmationPageComponent } from './components/tan-confirmation-page/tan-confirmation-page.component';
import { TanConfirmationErrorComponent } from './components/tan-confirmation-error/tan-confirmation-error.component';
import { TanConfirmationCanceledComponent } from './components/tan-confirmation-canceled/tan-confirmation-canceled.component';
import { ConsentConfirmationPageComponent } from './components/consent-confirmation-page/consent-confirmation-page.component';
import { ConsentConfirmationDeniedComponent } from './components/consent-confirmation-denied/consent-confirmation-denied.component';
import { AppAuthGuard } from './app.authguard';
import {TanConfirmationSuccessfulComponent} from './components/tan-confirmation-successful/tan-confirmation-successful.component';
import {ConsentConfirmationErrorComponent} from './components/consent-confirmation-error/consent-confirmation-error.component';
import {HelpPageComponent} from './components/help-page/help-page.component';



const routes: Routes = [
  { path: '', component: HelpPageComponent},
  { path: ':consentId', component: ConsentConfirmationPageComponent},
  { path: 'consentconfirmationerror', component: ConsentConfirmationErrorComponent },
  { path: 'consentconfirmationdenied', component: ConsentConfirmationDeniedComponent },
  { path: 'tanconfirmation', component: TanConfirmationPageComponent},
  { path: 'tanconfirmationcanceled', component: TanConfirmationCanceledComponent },
  { path: 'tanconfirmationerror', component: TanConfirmationErrorComponent },
  { path: 'tanconfirmationsuccessful', component: TanConfirmationSuccessfulComponent },

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
