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
import { Banking } from '../../model/banking.model';
import { SinglePayment } from '../../model/singlePayment';
import { AccountConsent } from '../../model/accountConsent';
import ConsentStatusEnum = AccountConsent.ConsentStatusEnum;
import { PisService } from '../pis.service';

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

  constructor(private route: ActivatedRoute, private router: Router, private pisService: PisService) {
  }

  ngOnInit() {
    this.route.url
      .subscribe(params => {
        this.getBankingDetailsFromUrl(params);
      });

    const bankingData = <Banking>({tan: this.tan, consentId: this.consentId, paymentId: this.paymentId});
    this.pisService.setData(bankingData);
    this.getSinglePayments();
  }

  getSinglePayments() {
    this.pisService.getConsentById().subscribe(data => {
      this.singlePayments = data;
    });
  }

  getBankingDetailsFromUrl(params: UrlSegment[]) {
    this.consentId = params[0].toString();
    this.paymentId = params[1].toString();
  }

  createQueryParams() {
    return {
      consentId: this.consentId,
      paymentId: this.paymentId,
    };
  }

  onClickContinue() {
    this.pisService.updateConsentStatus(ConsentStatusEnum.RECEIVED)
      .subscribe();
    this.pisService.generateTan().subscribe();
    this.router.navigate(['pis/tanconfirmation'], {
      queryParams: this.createQueryParams()
    });
  }

  onClickCancel() {
    this.pisService.updateConsentStatus(ConsentStatusEnum.REVOKEDBYPSU)
      .subscribe();
    this.router.navigate(['pis/consentconfirmationdenied']);
  }
}
