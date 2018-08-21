import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import {AccountConsent} from "../model/aspsp/accountConsent";
import {Account} from "../model/aspsp/account";


@Injectable({
  providedIn: 'root'
})
export class AisService {
  aspspServerUrl = environment.aspspServerUrl;
  savedConsentId : string;



  constructor(private httpClient: HttpClient) {
  }

  saveConsentId(consentId) {
    this.savedConsentId = consentId;
  }


  getConsent(consentId): Observable<AccountConsent> {
    console.log('iio url', this.aspspServerUrl);
    // let date = new Date();
    const headers = new HttpHeaders({
      'x-request-id': environment.xRequestId,
      // 'date': date.toUTCString(),
    });
    return this.httpClient.get<AccountConsent>(this.aspspServerUrl + '/api/v1/consents/' + consentId, {headers: headers})
      .pipe(
      map(data => {
        console.log(data);
        return data;
      })
    );
  }

  getAccounts(): Observable<any> {
    // let date = new Date();
    const headers = new HttpHeaders( {
      'x-request-id': environment.xRequestId,
      // 'date': date.toUTCString(),
      'consent-id': this.savedConsentId
    });
    return this.httpClient.get <any>(this.aspspServerUrl + '/api/v1/accounts', {headers: headers})
      .pipe(
        map( data =>{
          console.log('account iio', data);
          return data;
        })
      );
  }
}


