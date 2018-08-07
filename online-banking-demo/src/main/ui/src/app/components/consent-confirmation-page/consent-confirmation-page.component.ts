import { Component, OnInit } from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {BankingService} from '../../service/banking.service';
import {Observable} from 'rxjs';
import { SinglePayments } from '../../models/models';

@Component({
  selector: 'app-consent-confirmation-page',
  templateUrl: './consent-confirmation-page.component.html'
})
export class ConsentConfirmationPageComponent implements OnInit {
  singlePayments$: Observable<SinglePayments>;
  id: string;
  decision: string;
  paymentId: string;
  iban: string;
  consentId: string;

  constructor(private route: ActivatedRoute, private router: Router, private bankingService: BankingService){ }

  onClickPaymentAccepted(paymentIsAccepted) {
    this.decision = paymentIsAccepted ? "confirmed" : "revoked"
    this.bankingService.postConsent(this.decision)
      .subscribe(
        success => {
          var nextState = paymentIsAccepted ? '/consentconfirmationsuccessful' : '/consentconfirmationdenied'
          this.router.navigate([nextState])
        },
        error => {
        this.router.navigate(['/consentconfirmationerror'])
        }
    )
  }

  ngOnInit() {
    this.route.queryParams
    .subscribe(params => {
      this.readBankingDataFromUrl(params)
    })
    this.checkAndSaveData()
    this.singlePayments$ = this.bankingService.getSinglePayments();
  }

  readBankingDataFromUrl(params) {
    this.iban = params['iban']
    this.consentId = params['consentId']
    this.paymentId = params['paymentId']
  }

  checkAndSaveData() {
    var data ={iban: this.iban, consentId: this.consentId, paymentId: this.paymentId}
    if (Object.keys(this.bankingService.loadData()).length === 0) {
      this.bankingService.saveData(data)
    }
  }

}
