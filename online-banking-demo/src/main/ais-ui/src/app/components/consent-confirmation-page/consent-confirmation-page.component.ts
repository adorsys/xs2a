import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, UrlSegment } from '@angular/router';
import { AisService } from '../../service/ais.service';
import { Account } from '../../model/aspsp/account';
import { AccountConsent } from '../../model/aspsp/accountConsent';
import { AspspSettings } from '../../model/profile/aspspSettings';
import ConsentStatusEnum = AccountConsent.ConsentStatusEnum;
import { Observable } from 'rxjs';

@Component({
  selector: 'app-consent-confirmation-page',
  templateUrl: './consent-confirmation-page.component.html',
  styleUrls: ['./consent-confirmation-page.component.scss']
})
export class ConsentConfirmationPageComponent implements OnInit {
  consentId: string;
  accounts: Account[];
  selectedAccounts = new Array<Account>();
  consent: AccountConsent;
  profile$: Observable<AspspSettings>;
  iban: string;
  bankOffered: boolean;

  constructor(private route: ActivatedRoute, private router: Router, private aisService: AisService) {
  }

  ngOnInit() {
    this.route.url
      .subscribe(params => {
        this.getConsentIdFromUrl(params);
      });
    this.aisService.saveConsentId(this.consentId);
    this.aisService.getConsent(this.consentId)
      .subscribe(consent => {
        this.consent = consent;
        if (consent.access.accounts.length === 0) {
          this.bankOffered = true;
          this.getAllPsuAccounts();
        } else {
          this.getAccountsWithConsentId();
        }
      });
    this.profile$ = this.aisService.getProfile();
  }

  onSelectAccount(selectedAccount: Account):void {
    if (this.selectedAccounts.includes(selectedAccount)) {
      this.selectedAccounts = this.selectedAccounts.filter(account => account !== selectedAccount);
    } else {
      this.selectedAccounts.push(selectedAccount);
    }
  }

  onClickContinue() {
    this.aisService.updateConsent(this.selectedAccounts).subscribe();
    this.aisService.saveIban(this.iban);
    this.aisService.generateTan().subscribe();
    this.router.navigate(['/tanconfirmation'], {queryParams: this.createQueryParams});
  }

  onClickCancel() {
    this.aisService.updateConsentStatus(ConsentStatusEnum.REVOKEDBYPSU).subscribe();
    this.router.navigate(['/consentconfirmationdenied'], {queryParams: this.createQueryParams});
  }

  getConsentIdFromUrl(params: UrlSegment[]) {
    this.consentId = params[0].toString();
  }

  createQueryParams() {
    return {
      consentId: this.consentId,
    };
  }

  getAllPsuAccounts() {
    this.aisService.getAllPsuAccounts().subscribe( accounts => {
      this.iban = accounts[0].iban;
      this.accounts = accounts;
    });
  }

  getAccountsWithConsentId() {
    this.aisService.getAccountsWithConsentID()
      .subscribe(accounts => {
        this.iban = accounts[0].iban;
        this.accounts = accounts;
      });
  }
}
