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

import { AisConsentConfirmationErrorComponent } from './ais-consent-confirmation-error/ais-consent-confirmation-error.component';
import { AisConsentConfirmationDeniedComponent } from './ais-consent-confirmation-denied/ais-consent-confirmation-denied.component';
import { AisTanConfirmationPageComponent } from './ais-tan-confirmation-page/ais-tan-confirmation-page.component';
import { AisTanConfirmationCanceledComponent } from './ais-tan-confirmation-canceled/ais-tan-confirmation-canceled.component';
import { AisTanConfirmationErrorComponent } from './ais-tan-confirmation-error/ais-tan-confirmation-error.component';
import { AisTanConfirmationSuccessfulComponent } from './ais-tan-confirmation-successful/ais-tan-confirmation-successful.component';
import { AisConsentConfirmationPageComponent } from './ais-consent-confirmation-page/ais-consent-confirmation-page.component';
import { AisHelpPageComponent } from './ais-help-page/ais-help-page.component';

export const routes: Routes = [
  { path: '', component: AisHelpPageComponent },
  { path: 'consentconfirmationerror', component: AisConsentConfirmationErrorComponent },
  { path: 'consentconfirmationdenied', component: AisConsentConfirmationDeniedComponent },
  { path: 'tanconfirmation', component: AisTanConfirmationPageComponent},
  { path: 'tanconfirmationcanceled', component: AisTanConfirmationCanceledComponent },
  { path: 'tanconfirmationerror', component: AisTanConfirmationErrorComponent },
  { path: 'tanconfirmationsuccessful', component: AisTanConfirmationSuccessfulComponent },
  { path: ':consentId', component: AisConsentConfirmationPageComponent},
];
