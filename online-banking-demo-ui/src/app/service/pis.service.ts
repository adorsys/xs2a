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

@Injectable({
  providedIn: 'root'
})
export class PisService {
  savedData = new Banking();
  private CM_CONSENT_URI =  '/consent-management/api/v1/pis/consent';
  private MOCK_CONSENT_CONFIRMATION_URI = 'mockserver/consent/confirmation/pis';

  constructor(private httpClient: HttpClient, private configService: ConfigService) {
  }

  validateTan(tan: string): Observable<string> {
    const body = {
      tanNumber: tan,
      psuId: 'aspsp1',
      consentId: this.savedData.consentId,
      paymentId: this.savedData.paymentId
    };
    return this.httpClient.put<string>(`${this.MOCK_CONSENT_CONFIRMATION_URI}`, body);
  }

  updateConsentStatus(status: string) {
    return this.httpClient.put(`${this.MOCK_CONSENT_CONFIRMATION_URI}/${this.savedData.consentId}/${status}`, {});
  }

  saveData(data) {
    this.savedData = data;
  }

  generateTan(): Observable<string> {
    return this.httpClient.post<string>(`${this.MOCK_CONSENT_CONFIRMATION_URI}/aspsp1/SMS_OTP`, {});
  }

  getConsentById(): Observable<SinglePayment> {
    return this.httpClient.get<SinglePayment>(`${this.CM_CONSENT_URI}/${this.savedData.consentId}`);
  }
}
