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

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Banking } from '../model/banking.model';
import { SinglePayment } from '../model/singlePayment';
import { ConfigService } from './config.service';
import { KeycloakService } from 'keycloak-angular';

@Injectable({
  providedIn: 'root'
})
export class PisService {
  data = new Banking();
  psuId: string;
  private CM_CONSENT_URI =  '/consent-management/api/v1/pis/consent';
  private MOCK_CONSENT_CONFIRMATION_URI = '/mockserver/consent/confirmation/pis';

  constructor(private httpClient: HttpClient, private configService: ConfigService, private keycloak: KeycloakService) {
  }

  validateTan(tan: string): Observable<string> {
    const body = {
      tanNumber: tan,
      psuId: this.keycloak.getUsername(),
      consentId: this.data.consentId,
      paymentId: this.data.paymentId
    };
    return this.httpClient.put<string>(`${this.MOCK_CONSENT_CONFIRMATION_URI}`, body);
  }

  updateConsentStatus(status: string) {
    return this.httpClient.put(`${this.MOCK_CONSENT_CONFIRMATION_URI}/${this.data.consentId}/${status}`, {});
  }

  setData(data) {
    this.data = data;
  }

  generateTan(): Observable<string> {
    return this.httpClient.post<string>(`${this.MOCK_CONSENT_CONFIRMATION_URI}/${this.keycloak.getUsername()}/SMS_OTP`, {});
  }

  getConsentById(): Observable<SinglePayment> {
    return this.httpClient.get<SinglePayment>(`${this.CM_CONSENT_URI}/${this.data.consentId}`);
  }
}
