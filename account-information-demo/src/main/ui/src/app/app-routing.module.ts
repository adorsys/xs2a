import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { TanConfirmationPageComponent } from './components/tan-confirmation-page/tan-confirmation-page.component';
import { TanConfirmationErrorComponent } from './components/tan-confirmation-error/tan-confirmation-error.component';
import { TanConfirmationCanceledComponent } from './components/tan-confirmation-canceled/tan-confirmation-canceled.component';
import { ConsentConfirmationPageComponent } from './components/consent-confirmation-page/consent-confirmation-page.component';
import { ConsentConfirmationDeniedComponent } from './components/consent-confirmation-denied/consent-confirmation-denied.component';
import { AppAuthGuard } from './app.authguard';
import {TanConfirmationSuccessfulComponent} from "./components/tan-confirmation-successful/tan-confirmation-successful.component";
import {ConsentConfirmationErrorComponent} from "./components/consent-confirmation-error/consent-confirmation-error.component";



const routes: Routes = [
  { path: 'consentconfirmationerror', component: ConsentConfirmationErrorComponent },
  { path: 'consentconfirmationdenied', component: ConsentConfirmationDeniedComponent },
  { path: 'tanconfirmationpage', component: TanConfirmationPageComponent},
  { path: 'tanconfirmationcanceled', component: TanConfirmationCanceledComponent },
  { path: 'tanconfirmationerror', component: TanConfirmationErrorComponent },
  { path: 'tanconfirmationsuccessful', component: TanConfirmationSuccessfulComponent },
  { path: ':consentId', component: ConsentConfirmationPageComponent},
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
