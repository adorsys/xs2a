import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import {AccountConsent} from '../model/aspsp/accountConsent';
import {Account} from '../model/aspsp/account';
import {AccountsResponse} from '../model/aspsp/AccountsResponse';
import {AspspSettings} from '../model/profile/aspspSettings';


@Injectable({
  providedIn: 'root'
})
export class AisService {
  GET_CONSENT_URL = `${environment.aspspServerUrl}/api/v1/consents`;
  GET_ACCOUNTS_URL = `${environment.aspspServerUrl}/api/v1/accounts?with-balance=true`;
  GENERATE_TAN_URL = `${environment.mockServerUrl}/consent/confirmation/ais`;
  UPDATE_CONSENT_STATUS_URL = `${environment.mockServerUrl}/consent/confirmation/ais`;
  VALIDATE_TAN_URL = `${environment.mockServerUrl}/consent/confirmation/ais`;
  GET_PROFILE_URL = `${environment.profileServerUrl}/api/v1/aspsp-profile`;
  savedConsentId: string;
  savedIban: string;

  constructor(private httpClient: HttpClient) {
  }

  saveConsentId(consentId) {
    this.savedConsentId = consentId;
  }

  saveIban(iban) {
    this.savedIban = iban;
  }

  getConsent(consentId): Observable<AccountConsent> {
    const headers = new HttpHeaders({
      'x-request-id': environment.xRequestId,
      'tpp-qwac-certificate': environment.tppQwacCertificate,
    });
    return this.httpClient.get<AccountConsent>(`${this.GET_CONSENT_URL}/${consentId}` , {headers: headers})
      .pipe(
        map(data => {
          return data;
        })
      );
  }

  getAccounts(): Observable<Account[]> {
    const headers = new HttpHeaders({
      'x-request-id': environment.xRequestId,
      'consent-id': this.savedConsentId,
      'tpp-qwac-certificate': environment.tppQwacCertificate,
      'accept': 'application/json'
    });
    return this.httpClient.get <AccountsResponse>(this.GET_ACCOUNTS_URL, {headers: headers})
      .pipe(
        map(data => {
          return data.accountList;
        })
      );
  }

  getProfile(): Observable<AspspSettings> {
    // TODO: Activate when CORS issue is resolved
    return this.httpClient.get<AspspSettings>(`${this.GET_PROFILE_URL}`)
      .pipe(
        map(data => {
          return data;
        })
      );
  }

  generateTan(): Observable<string> {
    return this.httpClient.post<string>(`${this.GENERATE_TAN_URL}`, {});
  }

  updateConsentStatus(consentStatus): Observable<any> {
    return this.httpClient.put(`${this.UPDATE_CONSENT_STATUS_URL}/${this.savedConsentId}/${consentStatus}`, {});
  }

  validateTan(tan: string): Observable<string> {
    const body = {
      tanNumber: tan,
      consentId: this.savedConsentId,
      iban: this.savedIban,
    };
    return this.httpClient.put<string>(this.VALIDATE_TAN_URL, body);
  }
}
