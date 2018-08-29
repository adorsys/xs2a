import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import {AccountConsent} from "../model/aspsp/accountConsent";
import {Account} from "../model/aspsp/account";
import {AccountsResponse} from "../model/aspsp/AccountsResponse";


@Injectable({
  providedIn: 'root'
})
export class AisService {
  aspspServerUrl = environment.aspspServerUrl;
  mockServerUrl = environment.aspspServerUrl;
  profileServerUrl = environment.profileServerUrl;
  savedConsentId: string;



  constructor(private httpClient: HttpClient) {
  }

  saveConsentId(consentId) {
    this.savedConsentId = consentId;
  }


  getConsent(consentId): Observable<AccountConsent> {
    console.log('iio url', this.aspspServerUrl);
    let date = new Date();
    const headers = new HttpHeaders({
      'x-request-id': environment.xRequestId,
      'date': date.toUTCString(),
      'tpp-qwac-certificate': environment.tppQwacCertificate,
    });
    console.log("iio headers consent", headers);
    return this.httpClient.get<AccountConsent>(this.aspspServerUrl + '/api/v1/consents/' + consentId, {headers: headers})
      .pipe(
        map(data => {
          console.log(data);
          return data;
        })
      );
  }

  getAccounts(): Observable<Account[]> {
    let date = new Date();
    const headers = new HttpHeaders({
      'x-request-id': environment.xRequestId,
      'date': date.toUTCString(),
      'consent-id': this.savedConsentId,
      'tpp-qwac-certificate': environment.tppQwacCertificate,
      'accept': 'application/json'
    });
    console.log("iio", headers);
    return this.httpClient.get <AccountsResponse>(this.aspspServerUrl + '/api/v1/accounts?with-balance=true', {headers: headers})
      .pipe(
        map(data => {
          return data.accountList;
        })
      );
  }

  getTransactionLimit(): Observable<number> {
    // TODO: Activate when CORS issue is resolved
    // return this.httpClient.get<number>(`${this.profileServerUrl}/api/v1/transaction-lifetime`)
    //   .pipe(
    //     map(data => {
    //       console.log('account iio', data);
    //       return data;
    //     })
    //   )
    return of(5)
  }

  generateTan(iban: string): Observable<string> {
    return this.httpClient.post<string>(`${this.mockServerUrl}/consent/confirmation/tan/${iban}`, {})
  }

  updateConsentStatus(consentStatus): any {
    return this.httpClient.put(`${this.aspspServerUrl}/consent/confirmation/status/${this.savedConsentId}/${consentStatus}`, {})
  }

  validateTan(tan: string): Observable<string> {
    const body = {
      tanNumber: tan,
    };
    return this.httpClient.post<string>(`${this.aspspServerUrl}/consent/confirmation/tan/validate`, {})
  }

}
