import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AccountConsent } from '../../model/aspsp/accountConsent';
import ConsentStatusEnum = AccountConsent.ConsentStatusEnum;
import { AisService } from '../ais.service';

@Component({
  selector: 'app-tan-confirmation-page',
  templateUrl: './ais-tan-confirmation-page.component.html',
  styleUrls: ['./ais-tan-confirmation-page.component.scss']
})
export class AisTanConfirmationPageComponent implements OnInit {
  tan: string;
  wrongTanAttempt: boolean;


  constructor(private route: ActivatedRoute, private router: Router, private aisService: AisService) { }

  ngOnInit() {
  }

  onClickContinue() {
    this.aisService.validateTan(this.tan)
      .subscribe(
        success => {
          this.router.navigate(['ais/tanconfirmationsuccessful']);
        },
        error => {
          if (error.error.message === 'WRONG_TAN') {
            this.wrongTanAttempt = true;
          } else {
            this.router.navigate(['ais/tanconfirmationerror']);
          }
        }
      );
    this.aisService.updateConsentStatus(ConsentStatusEnum.VALID).subscribe();
  }

  onClickCancel() {
    this.aisService.updateConsentStatus(ConsentStatusEnum.REVOKEDBYPSU).subscribe();
    this.router.navigate(['ais/tanconfirmationcanceled']);
  }
}
