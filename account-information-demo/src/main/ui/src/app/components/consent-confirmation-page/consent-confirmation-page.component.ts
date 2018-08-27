import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, UrlSegment } from '@angular/router';
import { AisService } from '../../service/ais.service';
import { Account} from "../../model/aspsp/account";
import {Observable} from 'rxjs';
import {AccountConsent} from "../../model/aspsp/accountConsent";

@Component({
  selector: 'app-consent-confirmation-page',
  templateUrl: './consent-confirmation-page.component.html',
  styleUrls: ['./consent-confirmation-page.component.scss']
})
export class ConsentConfirmationPageComponent implements OnInit {
  consentId: string;
  Accounts: Account[];
  Consent$: Observable<AccountConsent>
  TransactionLimit$: Observable<number>



  constructor(private route: ActivatedRoute, private router: Router, private aisService: AisService) { }

  ngOnInit() {
    this.route.url
      .subscribe(params => { this.getConsentIdFromUrl(params) });
    this.aisService.saveConsentId(this.consentId);
    this.Consent$ = this.aisService.getConsent(this.consentId);
    this.aisService.getAccounts()
      .subscribe(data => {
        this.Accounts = data;
      });
    this.TransactionLimit$ = this.aisService.getTransactionLimit();

  }

  onClickContinue() {
    this.aisService.generateTan(this.Accounts[0].iban)
    this.aisService.updateConsentStatus('confirmed')
    this.router.navigate(['/tanconfirmation'], {queryParams: this.createQueryParams});
  }

  onClickCancel() {
    this.aisService.updateConsentStatus('revoked')
    this.router.navigate(['/consentconfirmationdenied'], {queryParams: this.createQueryParams});
  }

  getConsentIdFromUrl(params: UrlSegment[]) {
    this.consentId = params[0].toString()
  }

  createQueryParams() {
    return {
      consentId: this.consentId,
    }
  }
}
