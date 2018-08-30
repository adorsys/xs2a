import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, UrlSegment } from '@angular/router';
import { AisService } from '../../service/ais.service';
import { Account} from '../../model/aspsp/account';
import {Observable} from 'rxjs';
import {AccountConsent} from '../../model/aspsp/accountConsent';
import {AspspSettings} from '../../model/profile/aspspSettings';

@Component({
  selector: 'app-consent-confirmation-page',
  templateUrl: './consent-confirmation-page.component.html',
  styleUrls: ['./consent-confirmation-page.component.scss']
})
export class ConsentConfirmationPageComponent implements OnInit {
  consentId: string;
  accounts: Account[];
  consent$: Observable<AccountConsent>;
  profile$: Observable<AspspSettings>;
  iban: string;

  constructor(private route: ActivatedRoute, private router: Router, private aisService: AisService) { }

  ngOnInit() {
    this.route.url
      .subscribe(params => { this.getConsentIdFromUrl(params); });
    this.aisService.saveConsentId(this.consentId);
    this.consent$ = this.aisService.getConsent(this.consentId);
    this.aisService.getAccounts()
      .subscribe(data => {
        this.iban = data[0].iban;
        this.accounts = data;
      });
    this.profile$ = this.aisService.getProfile();
  }

  onClickContinue() {
    this.aisService.saveIban(this.iban);
    this.aisService.generateTan().subscribe();
    this.router.navigate(['/tanconfirmation'], {queryParams: this.createQueryParams});
  }

  onClickCancel() {
    this.aisService.updateConsentStatus('REVOKED_BY_PSU').subscribe();
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
}
