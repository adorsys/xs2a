/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { Routes } from '@angular/router';
import { PisConsentConfirmationPageComponent } from './pis-consent-confirmation-page/pis-consent-confirmation-page.component';
import { PisTanConfirmationCanceledComponent } from './pis-tan-confirmation-canceled/pis-tan-confirmation-canceled.component';
import { PisTanConfirmationErrorComponent } from './pis-tan-confirmation-error/pis-tan-confirmation-error.component';
import { PisTanConfirmationPageComponent } from './pis-tan-confirmation-page/pis-tan-confirmation-page.component';
import { PisConsentConfirmationDeniedComponent } from './pis-consent-confirmation-denied/pis-consent-confirmation-denied.component';
import { PisConsentConfirmationSuccessfulComponent } from './pis-consent-confirmation-successful/pis-consent-confirmation-successful.component';
import { PisHelpPageComponent } from './pis-help-page/pis-help-page.component';

export const routes: Routes = [
  { path: '', component: PisHelpPageComponent },
  { path: ':consentId/:paymentId', component: PisConsentConfirmationPageComponent },
  { path: ':consentId/:paymentId/:psuId', component: PisConsentConfirmationPageComponent },
  { path: 'tanconfirmationcanceled', component: PisTanConfirmationCanceledComponent },
  { path: 'tanconfirmationerror', component: PisTanConfirmationErrorComponent },
  { path: 'tanconfirmation', component: PisTanConfirmationPageComponent },
  { path: 'consentconfirmationdenied', component: PisConsentConfirmationDeniedComponent },
  { path: 'consentconfirmationsuccessful', component: PisConsentConfirmationSuccessfulComponent },
];
