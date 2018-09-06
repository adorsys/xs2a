import { Component, OnInit } from '@angular/core';
import {ActivatedRoute, Router, UrlSegment} from '@angular/router';
import {BankingService} from '../../service/banking.service';
import { SinglePayments } from '../../models/models';
import { Banking } from '../../models/banking.model';

@Component({
  selector: 'app-consent-confirmation-page',
  templateUrl: './consent-confirmation-page.component.html'
})
export class ConsentConfirmationPageComponent implements OnInit {
  singlePayments: SinglePayments;
  tan: string;
  paymentId: string;
  iban: string;
  consentId: string;

  constructor(private route: ActivatedRoute, private router: Router, private bankingService: BankingService) {
  }


  ngOnInit() {
    this.route.url
      .subscribe(params => {
        this.getBankingDetailsFromUrl(params);
      });

    let bankingData = <Banking>({tan: this.tan, iban: this.iban, consentId: this.consentId, paymentId: this.paymentId});
    this.bankingService.saveData(bankingData);
    this.bankingService.getSinglePayments().subscribe(data => {
      this.iban = data.debtorAccount.iban;
      bankingData = <Banking>({tan: this.tan, iban: this.iban, consentId: this.consentId, paymentId: this.paymentId});
      this.bankingService.saveData(bankingData);
      this.singlePayments = data;
    })
  }

  getBankingDetailsFromUrl(params: UrlSegment[]) {
    this.consentId = params[0].toString();
    this.paymentId = atob(params[1].toString());
  }

  createQueryParams() {
    return {
      consentId: this.consentId,
      paymentId: this.paymentId,
    };
  }

  onClickContinue() {
    this.router.navigate(['/tanconfirmation'], {
      queryParams: this.createQueryParams()
    });
  }
}
