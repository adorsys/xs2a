import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, UrlSegment } from '@angular/router';
import { AisService } from '../../service/ais.service';
import { AccountConsent} from "../../model/aspsp/accountConsent";


@Component({
  selector: 'app-consent-confirmation-page',
  templateUrl: './consent-confirmation-page.component.html',
  styleUrls: ['./consent-confirmation-page.component.scss']
})
export class ConsentConfirmationPageComponent implements OnInit {
  consentId: string;


  constructor(private route: ActivatedRoute, private router: Router, private aisService: AisService) { }

  ngOnInit() {
    this.route.url
      .subscribe(params => { this.getConsentIdFromUrl(params) });
    this.aisService.saveConsentId(this.consentId);
    this.aisService.getConsent(this.consentId)
      .subscribe(data => console.log(data));
    this.aisService.getAccounts()
  .subscribe(data => console.log('accounts', data));
  }

  onClickContinue() {
    this.router.navigate(['/tanconfirmation'], {queryParams: this.createQueryParams});
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
