import { Component, OnInit } from '@angular/core';
import {ActivatedRoute, Params, Router} from '@angular/router';
import { BankingService } from '../../service/banking.service';
import { Banking } from '../../models/banking.model';

@Component({
  selector: 'app-tan-confirmation-page',
  templateUrl: './tan-confirmation-page.component.html'
})
export class TanConfirmationPageComponent implements OnInit {
  tan: string;
  iban: string;
  consentId: string;
  paymentId: string;
  tanError: boolean;


  constructor(private route: ActivatedRoute, private router: Router, private bankingService: BankingService) { }


  ngOnInit() {
    this.route.queryParams
      .subscribe(params => { this.getBankingDetailsFromUrl(params); });
    let bankingData = <Banking>({ tan: this.tan, iban: this.iban, consentId: this.consentId, paymentId: this.paymentId });
    this.bankingService.saveData(bankingData);
    this.bankingService.getSinglePayments().subscribe(data => {
      this.iban = data.debtorAccount.iban;
      bankingData = <Banking>({ tan: this.tan, iban: this.iban, consentId: this.consentId, paymentId: this.paymentId });
      this.bankingService.saveData(bankingData);
      this.bankingService.generateTan().subscribe();
    });
  }

  getBankingDetailsFromUrl(params: Params) {
    this.consentId = params['consentId'];
    this.paymentId = params['paymentId'];
  }



  onClickContinue() {
    this.bankingService.validateTan(this.tan)
      .subscribe(
        success => {
          this.bankingService.setConsentStatus('VALID').subscribe();
          this.router.navigate(['/consentconfirmationsuccessful']);
        },
        error => {
          if (error.error.message === 'WRONG_TAN') {
              this.tanError = true;
            }
            if (error.error.message === 'LIMIT_EXCEEDED') {
              this.router.navigate(['/tanconfirmationerror']);
            }
        }
      );
  }

  onClickCancel() {
    this.bankingService.setConsentStatus('REVOKED_BY_PSU')
      .subscribe();
    this.router.navigate(['/tanconfirmationcanceled']);
  }
}
