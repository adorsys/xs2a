import { Component, OnInit } from '@angular/core';
import {AccountConsent} from "../../model/aspsp/accountConsent";
import {Observable} from "rxjs/index";
import {AisService} from "../../service/ais.service";
import {ActivatedRoute, Router, UrlSegment} from "@angular/router";

@Component({
  selector: 'app-tan-confirmation-page',
  templateUrl: './tan-confirmation-page.component.html',
  styleUrls: ['./tan-confirmation-page.component.scss']
})
export class TanConfirmationPageComponent implements OnInit {
  Consent$: Observable<AccountConsent>
  consentId: string;

  constructor(private route: ActivatedRoute, private router: Router, private aisService: AisService) { }

  ngOnInit() {
    this.route.url
      .subscribe(params => { this.getConsentIdFromUrl(params) });
    this.aisService.saveConsentId(this.consentId);
    this.Consent$ = this.aisService.getConsent(this.consentId);
  }

  onClickContinue() {
    this.router.navigate(['/tanconfirmationsuccessful'], {queryParams: this.createQueryParams});
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
