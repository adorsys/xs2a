import { Component, OnInit } from '@angular/core';
import {AisService} from '../../service/ais.service';
import {ActivatedRoute, Router} from '@angular/router';
import {AccountConsent} from "../../model/aspsp/accountConsent";
import ConsentStatusEnum = AccountConsent.ConsentStatusEnum;

@Component({
  selector: 'app-tan-confirmation-page',
  templateUrl: './tan-confirmation-page.component.html',
  styleUrls: ['./tan-confirmation-page.component.scss']
})
export class TanConfirmationPageComponent implements OnInit {
  tan: string;
  wrongTanAttempt: boolean;


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
          if (error.error.message == "WRONG_TAN") {
            this.wrongTanAttempt = true;
          }
          else {
            this.router.navigate(['/tanconfirmationerror']);
          }
        }
      );
    this.aisService.updateConsentStatus(ConsentStatusEnum.VALID).subscribe();
  }
}
