import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { AccountConsent } from '../model/aspsp/accountConsent';
import { Account } from '../model/aspsp/account';
import { AccountsResponse } from '../model/aspsp/AccountsResponse';
import { AspspSettings } from '../model/profile/aspspSettings';
import { SpiAccountDetails } from '../model/mock/spiAccountDetails';
import { AccountReference } from '../model/aspsp/accountReference';
import { SelectedAccountConsent } from '../model/aspsp/selectedAccountConsent';
import { AccountAccess } from '../model/aspsp/accountAccess';


@Injectable({
  providedIn: 'root'
})
export class AisService {
  GET_CONSENT_URL = `${environment.aspspServerUrl}/api/v1/consents`;
  GET_ACCOUNTS_WITH_CONSENTID_URL = `${environment.aspspServerUrl}/api/v1/accounts?with-balance=true`;
  GENERATE_TAN_URL = `${environment.mockServerUrl}/consent/confirmation/ais`;
  UPDATE_CONSENT_STATUS_URL = `${environment.mockServerUrl}/consent/confirmation/ais`;
  UPDATE_CONSENT = `${environment.cmsServerUrl}/api/v1/ais/consent`;
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
    return this.httpClient.get<AccountConsent>(`${this.GET_CONSENT_URL}/${consentId}` , {headers: headers});
  }

  getAccountsWithConsentID(): Observable<Account[]> {
    const headers = new HttpHeaders({
      'x-request-id': environment.xRequestId,
      'consent-id': this.savedConsentId,
      'tpp-qwac-certificate': environment.tppQwacCertificate,
      'accept': 'application/json'
    });
    return this.httpClient.get <AccountsResponse>(this.GET_ACCOUNTS_WITH_CONSENTID_URL, {headers: headers})
      .pipe(
        map(data => {
          return data.accountList;
        })
      );
  }

  // TODO Delete function when getAccount endpoint is ready for bank offered consent
  getAllPsuAccounts(): Observable<Account[]> {
    const headers = new HttpHeaders({
      'x-request-id': environment.xRequestId,
      'consent-id': 'c64e30fd-b814-4e37-b155-8acc3ac30fea',
      'tpp-qwac-certificate': environment.tppQwacCertificate,
      'accept': 'application/json'
    });
    return this.httpClient.get <AccountsResponse>(this.GET_ACCOUNTS_WITH_CONSENTID_URL, {headers: headers})
      .pipe(
        map(data => {
          return data.accountList;
        })
      );
  }

  getProfile(): Observable<AspspSettings> {
    return this.httpClient.get<AspspSettings>(`${this.GET_PROFILE_URL}`);
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

  updateConsent(selectedAccounts: Account[]) {
    const selectedAccountConsent: SelectedAccountConsent = this.buildAccountConsent(selectedAccounts);

    return this.httpClient.put(`${this.UPDATE_CONSENT}/${this.savedConsentId}/access`, selectedAccountConsent);
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
