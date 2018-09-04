import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, UrlSegment } from '@angular/router';
import { BankingService } from '../../service/banking.service';
import { Banking } from '../../models/banking.model';
import {SinglePayments} from "../../models/singlePayments";

@Component({
  selector: 'app-tan-confirmation-page',
  templateUrl: './tan-confirmation-page.component.html'
})
export class TanConfirmationPageComponent implements OnInit {
  tan: string;
  iban: string;
  consentId: string;
  paymentId: string;
  wrongTanCount = 0;
  // aspspPayment: SinglePayments;


  constructor(private route: ActivatedRoute, private router: Router, private bankingService: BankingService) { }

  onClickContinue() {
    this.bankingService.postTan()
      .subscribe(
        success => {
          this.router.navigate(['/consentconfirmation'], { queryParams: this.createQueryParams() });
        },
        error => {
          if (error.error.message === 'WRONG_TAN') {
            if (this.wrongTanCount >= 2) {
              // redirect to ttp-site
              this.router.navigate(['/tanconfirmationerror'])
            }
            this.tan = '';
            this.wrongTanCount += 1;
          } else {
            this.router.navigate(['/tanconfirmationerror'])
          }
        }
      );
  }

  onClickCancel() {
    this.bankingService.postConsent('revoked')
      .subscribe();
  }

  ngOnInit() {
    this.route.url
      .subscribe(params => { this.getBankingDetailsFromUrl(params); });
    let bankingData = <Banking>({ tan: this.tan, iban: this.iban, consentId: this.consentId, paymentId: this.paymentId });
    this.bankingService.saveData(bankingData);
    this.bankingService.getSinglePayments().subscribe(data => {
      // this.aspspPayment = data;
      console.log('awi debtorAccount: ', data.debtorAccount.iban);
      this.iban = data.debtorAccount.iban;
      bankingData = <Banking>({ tan: this.tan, iban: this.iban, consentId: this.consentId, paymentId: this.paymentId });
      this.bankingService.saveData(bankingData);
      this.bankingService.generateTan().subscribe();
    });
  }

  getBankingDetailsFromUrl(params: UrlSegment[]) {
    this.consentId = params[0].toString();
    this.paymentId = atob(params[1].toString());
  }

  createQueryParams() {
    return {
      iban: this.iban,
      consentId: this.consentId,
      paymentId: this.paymentId,
    };
  }
}
