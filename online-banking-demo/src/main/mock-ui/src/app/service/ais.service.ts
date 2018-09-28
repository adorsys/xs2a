import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable} from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { AccountConsent } from '../model/aspsp/accountConsent';
import { Account } from '../model/aspsp/account';
import { AccountsResponse } from '../model/aspsp/AccountsResponse';
import { AspspSettings } from '../model/profile/aspspSettings';
import { AccountReference } from '../model/aspsp/accountReference';
import { SelectedAccountConsent } from '../model/aspsp/selectedAccountConsent';
import { AccountAccess } from '../model/aspsp/accountAccess';

@Injectable({
  providedIn: 'root'
})
export class AisService {
  savedConsentId: string;
  consentManagementServerUrl =  environment.consentManagementServerUrl+'/ais/consent';
  xs2aConsentServerUrl= environment.aspspXs2aServerUrl + '/v1/consents';
  xs2aAccountServerUrl= environment.aspspXs2aServerUrl + '/v1/accounts';
  mockServerUrl = environment.mockServerUrl + '/ais';

  constructor(private httpClient: HttpClient) {
  }

  saveConsentId(consentId) {
    this.savedConsentId = consentId;
  }

  getConsent(consentId): Observable<AccountConsent> {
    const headers = new HttpHeaders({
      'x-request-id': environment.xRequestId,
      'tpp-qwac-certificate': environment.tppQwacCertificate,
    });
    return this.httpClient.get<AccountConsent>(`${this.xs2aConsentServerUrl}/${consentId}` , {headers: headers});
  }

  getAccountsWithConsentID(): Observable<Account[]> {
    const headers = new HttpHeaders({
      'x-request-id': environment.xRequestId,
      'consent-id': this.savedConsentId,
      'tpp-qwac-certificate': environment.tppQwacCertificate,
      'accept': 'application/json'
    });
    return this.httpClient.get <AccountsResponse>(this.xs2aAccountServerUrl + '?withBalance=true', {headers: headers})
      .pipe(
        map(data => {
          return data.accounts;
        })
      );
  }

  getProfile(): Observable<AspspSettings> {
    return this.httpClient.get<AspspSettings>(`${environment.profileServerUrl}`);
  }

  generateTan(): Observable<string> {
    return this.httpClient.post<string>(`${this.mockServerUrl+ '/aspsp'}`, {});
  }

  updateConsentStatus(consentStatus): Observable<any> {
    return this.httpClient.put(`${this.mockServerUrl}/${this.savedConsentId}/${consentStatus}`, {});
  }

  validateTan(tan: string): Observable<string> {
    const body = {
      tanNumber: tan,
      consentId: this.savedConsentId,
      psuId: 'aspsp'
    };
    return this.httpClient.put<string>(this.mockServerUrl, body);
  }

  updateConsent(selectedAccounts: Account[]) {
    const selectedAccountConsent: SelectedAccountConsent = this.buildAccountConsent(selectedAccounts);
    return this.httpClient.put(`${this.consentManagementServerUrl}/${this.savedConsentId}/${'access'}`, selectedAccountConsent);
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
