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
import { ActivatedRoute, Router, UrlSegment } from '@angular/router';
import { PisService } from '../../service/pis.service';
import { Banking } from '../../model/banking.model';
import { SinglePayment } from '../../model/singlePayment';
import { AccountConsent } from '../../model/accountConsent';
import ConsentStatusEnum = AccountConsent.ConsentStatusEnum;

@Component({
  selector: 'app-consent-confirmation-page',
  templateUrl: './pis-consent-confirmation-page.component.html'
})
export class PisConsentConfirmationPageComponent implements OnInit {
  singlePayments: SinglePayment;
  tan: string;
  paymentId: string;
  consentId: string;
  amount: number;

  constructor(private route: ActivatedRoute, private router: Router, private bankingService: PisService) {
  }


  ngOnInit() {
    this.route.url
      .subscribe(params => {
        this.getBankingDetailsFromUrl(params);
      });

    let bankingData = <Banking>({tan: this.tan, consentId: this.consentId, paymentId: this.paymentId});
    this.bankingService.saveData(bankingData);
    this.getSinglePayments();
  }

  getSinglePayments(){
    this.bankingService.getConsentById().subscribe(data => {
      this.singlePayments = data;
    });
  }

  getBankingDetailsFromUrl(params: UrlSegment[]) {
    this.consentId = params[1].toString();
    this.paymentId = atob(params[2].toString());
  }

  createQueryParams() {
    return {
      consentId: this.consentId,
      paymentId: this.paymentId,
    };
  }

  onClickContinue() {
    this.bankingService.updateConsentStatus(ConsentStatusEnum.RECEIVED)
      .subscribe();
    this.bankingService.generateTan().subscribe();
    this.router.navigate(['pis/tanconfirmation'], {
      queryParams: this.createQueryParams()
    });
  }

  onClickCancel() {
    this.bankingService.updateConsentStatus(ConsentStatusEnum.REVOKEDBYPSU)
      .subscribe();
    this.router.navigate(['pis/consentconfirmationdenied']);
  }
}
