/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { PisService } from '../../service/pis.service';
import { Banking } from '../../model/banking.model';
import { AccountConsent } from '../../model/accountConsent';
import ConsentStatusEnum = AccountConsent.ConsentStatusEnum;

@Component({
  selector: 'app-tan-confirmation-page',
  templateUrl: './pis-tan-confirmation-page.component.html'
})
export class PisTanConfirmationPageComponent implements OnInit {
  tan: string;
  consentId: string;
  paymentId: string;
  tanError: boolean;



  constructor(private route: ActivatedRoute, private router: Router, private bankingService: PisService) { }


  ngOnInit() {
    this.route.queryParams
      .subscribe(params => { this.getBankingDetailsFromUrl(params); });
    let bankingData = <Banking>({ tan: this.tan, consentId: this.consentId, paymentId: this.paymentId });
    this.bankingService.saveData(bankingData);
    // this.bankingService.getSinglePayments().subscribe();
    // this.bankingService.generateTan().subscribe();
  }

  getBankingDetailsFromUrl(params: Params) {
    this.consentId = params['consentId'];
    this.paymentId = params['paymentId'];
  }


  onClickContinue() {
    this.bankingService.validateTan(this.tan)
      .subscribe(
        success => {
          this.bankingService.updateConsentStatus(ConsentStatusEnum.VALID).subscribe();
          this.router.navigate(['pis/consentconfirmationsuccessful']);
        },
        error => {
          if (error.error.message === 'WRONG_TAN') {
              this.tanError = true;
            }
            if (error.error.message === 'LIMIT_EXCEEDED') {
              this.router.navigate(['pis/tanconfirmationerror']);
            }
        }
      );
  }

  onClickCancel() {
    this.bankingService.updateConsentStatus(ConsentStatusEnum.REVOKEDBYPSU)
      .subscribe();
    this.router.navigate(['pis/tanconfirmationcanceled']);
  }
}
