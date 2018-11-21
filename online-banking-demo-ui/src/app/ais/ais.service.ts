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
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { AccountConsent } from '../model/aspsp/accountConsent';
import { Account } from '../model/aspsp/account';
import { AccountsResponse } from '../model/aspsp/AccountsResponse';
import { AspspSettings } from '../model/profile/aspspSettings';
import { AccountReference } from '../model/aspsp/accountReference';
import { SelectedAccountConsent } from '../model/aspsp/selectedAccountConsent';
import { AccountAccess } from '../model/aspsp/accountAccess';
import { Config } from '../model/Config';
import { KeycloakService } from 'keycloak-angular';
import { ConfigService } from '../service/config.service';

@Injectable({
  providedIn: 'root'
})
export class AisService {
  consentId: string;
  private MOCK_AIS_URI = 'mockserver/consent/confirmation/ais';
  private XS2A_CONSENT_URI = 'xs2a/v1/consents';
  private XS2A_ACCOUNTS_URI = 'xs2a/v1/accounts';
  private CM_AIS_CONSENT_URI = 'consent-management/api/v1/ais/consent';
  private PROFILE_ASPSP_PROFILE_URI = `profile-server/api/v1/aspsp-profile`;
  private psuId: string;

  constructor(private httpClient: HttpClient, private configService: ConfigService, private keycloak: KeycloakService) {
  }

  setConsentId(consentId) {
    this.consentId = consentId;
  }

  getConsent(consentId): Observable<AccountConsent> {
    const headers = new HttpHeaders({
      // TODO: Dont use hard coded data
      'x-request-id': environment.xRequestId,
      'tpp-qwac-certificate': environment.tppQwacCertificate,
    });
    return this.httpClient.get<AccountConsent>(`${this.XS2A_CONSENT_URI}/${consentId}` , {headers: headers});
  }

  getAccountsWithConsentID(): Observable<Account[]> {
    const headers = new HttpHeaders({
      'x-request-id': environment.xRequestId,
      'consent-id': this.consentId,
      'tpp-qwac-certificate': environment.tppQwacCertificate,
      'accept': 'application/json'
    });
    return this.httpClient.get <AccountsResponse>(this.XS2A_ACCOUNTS_URI + '?withBalance=true', {headers: headers})
      .pipe(
        map(data => {
          return data.accounts;
        })
      );
  }

  getProfile(): Observable<AspspSettings> {
    return this.httpClient.get<AspspSettings>(this.PROFILE_ASPSP_PROFILE_URI);
  }

  generateTan(): Observable<string> {
    return this.httpClient.post<string>(`${this.MOCK_AIS_URI}/${this.keycloak.getUsername()}`, {});
  }

  updateConsentStatus(consentStatus): Observable<any> {
    return this.httpClient.put(`${this.MOCK_AIS_URI}/${this.consentId}/${consentStatus}`, {});
  }

  validateTan(tan: string): Observable<string> {
    const body = {
      tanNumber: tan,
      consentId: this.consentId,
      psuId: this.keycloak.getUsername()
    };
    return this.httpClient.put<string>(this.MOCK_AIS_URI, body);
  }

  updateConsent(selectedAccounts: Account[]) {
    const selectedAccountConsent: SelectedAccountConsent = this.buildAccountConsent(selectedAccounts);
    return this.httpClient.put(`${this.CM_AIS_CONSENT_URI}/${this.consentId}/${'access'}`, selectedAccountConsent);
  }

  private buildAccountConsent(selectedAccounts: Account[]) {
    const accountReferencesArray: AccountReference[] = this.convertToAccountReferenceArray(selectedAccounts);
    const accountAccess: AccountAccess = {
      accounts: accountReferencesArray,
      balances: accountReferencesArray,
      transactions: accountReferencesArray
    };
    const accountConsent: SelectedAccountConsent = { access: accountAccess};

    return accountConsent;
  }

  private convertToAccountReferenceArray(selectedAccounts: Account[]): AccountReference[] {
    const accountReferencesArray = new Array<AccountReference>();

    selectedAccounts.forEach(account => {
      const accountReference: AccountReference = {iban: account.iban, currency: account.currency};
      accountReferencesArray.push(accountReference);
    });
    return accountReferencesArray;
  }
}
