import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Banking } from '../models/banking.model';
import { SinglePayments } from '../models/models';
import { environment as env } from '../../environments/environment';


@Injectable({
  providedIn: 'root'
})
export class BankingService {
  savedData = new Banking();
  TAN_URL = `${env.mockServerUrl}/consent/confirmation/pis/`;
  SET_CONSENT_STATUS_URL = `${env.mockServerUrl}/consent/confirmation/pis`;
  GET_SINGLE_PAYMENTS_URL = `${env.mockServerUrl}/payments/`;

  constructor(private httpClient: HttpClient) {
  }

  validateTan(tan: string): Observable<any> {
    const body = {
      tanNumber: tan,
      consentId: this.savedData.consentId,
      paymentId: this.savedData.paymentId
    };
    const headers = new HttpHeaders({ 'Content-Type': 'application/json' });
    return this.httpClient.put(this.TAN_URL, body, { headers: headers });
  }

  setConsentStatus(decision: string) {
    const headers = new HttpHeaders({ 'Content-Type': 'application/json' });
    return this.httpClient.put(`${this.SET_CONSENT_STATUS_URL}/${this.savedData.consentId}/${decision}`, {}, { headers: headers });
  }

  saveData(data) {
    this.savedData = data;
  }

  generateTan(): Observable<any> {
    return this.httpClient.post(this.TAN_URL, {});
  }

  getSinglePayments(): Observable<SinglePayments> {
    return this.httpClient.get<SinglePayments>(this.GET_SINGLE_PAYMENTS_URL + this.savedData.paymentId);
  }
}
