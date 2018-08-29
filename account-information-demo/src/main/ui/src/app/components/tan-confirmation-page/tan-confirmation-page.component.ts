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
  tan: string;


  constructor(private route: ActivatedRoute, private router: Router, private aisService: AisService) { }

  ngOnInit() {
  }

  onClickContinue() {
    this.aisService.validateTan(this.tan)
      .subscribe(
        success => {
          this.router.navigate(['/tanconfirmationsuccessful']);
        },
        error => {
          this.router.navigate(['/tanconfirmationerror'])
        }
      );
    this.aisService.updateConsentStatus('VALID').subscribe();
  }
}
