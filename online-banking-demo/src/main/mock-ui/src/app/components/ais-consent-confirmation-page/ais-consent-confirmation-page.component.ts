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
  templateUrl: './ais-consent-confirmation-page.component.html',
  styleUrls: ['./ais-consent-confirmation-page.component.scss']
})
export class AisConsentConfirmationPageComponent implements OnInit {
  consentId: string;
  accounts: Account[];
  selectedAccounts = new Array<Account>();
  consent: AccountConsent;
  profile$: Observable<AspspSettings>;
  bankOffered: boolean;

  constructor(private route: ActivatedRoute, private router: Router, private aisService: AisService) {
  }

  ngOnInit() {
    this.route.url
      .subscribe(params => {
        this.getConsentIdFromUrl(params);
      });
    this.aisService.saveConsentId(this.consentId);
    this.getAccountsWithConsentId();
    this.aisService.getConsent(this.consentId)
      .subscribe(consent => {
        this.consent = consent;
        if (consent.access.accounts === undefined) {
          this.bankOffered = true;
        }
      });
    this.profile$ = this.aisService.getProfile();
  }

  onSelectAllAccounts(): void {
    if (this.selectedAccounts.length === this.accounts.length) {
      this.selectedAccounts = [];
    } else {
      this.selectedAccounts = this.accounts;
    }
  }

  onSelectAccount(selectedAccount: Account):void {
    if (this.selectedAccounts.includes(selectedAccount)) {
      this.selectedAccounts = this.selectedAccounts.filter(account => account !== selectedAccount);
    } else {
      this.selectedAccounts.push(selectedAccount);
    }
  }

  isAccountSelected(selectedAccount: Account): boolean {
    return this.selectedAccounts.includes(selectedAccount);
  }

  onClickContinue() {
    this.aisService.updateConsent(this.selectedAccounts).subscribe();
    this.aisService.generateTan().subscribe();
    this.router.navigate(['ais/tanconfirmation'], {queryParams: this.createQueryParams});
  }

  onClickCancel() {
    this.aisService.updateConsentStatus(ConsentStatusEnum.REVOKEDBYPSU).subscribe();
    this.router.navigate(['ais/consentconfirmationdenied'], {queryParams: this.createQueryParams});
  }

  getConsentIdFromUrl(params: UrlSegment[]) {
    this.consentId = params[1].toString();
  }

  createQueryParams() {
    return {
      consentId: this.consentId,
    };
  }

  getAccountsWithConsentId() {
    this.aisService.getAccountsWithConsentID()
      .subscribe(accounts => {
      this.accounts = accounts;
      });
  }
}
