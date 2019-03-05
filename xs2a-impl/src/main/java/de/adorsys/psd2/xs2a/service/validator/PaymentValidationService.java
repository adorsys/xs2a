/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service.validator;

import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.pis.BulkPayment;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.service.AccountReferenceValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.FORMAT_ERROR;
import static de.adorsys.psd2.xs2a.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.PIS_400;

@Service
@RequiredArgsConstructor
public class PaymentValidationService {
    private final AccountReferenceValidationService referenceValidationService;

    public ResponseObject validateSinglePayment(SinglePayment singePayment) {

        ResponseObject accountReferenceValidationResponse = referenceValidationService.validateAccountReferences(singePayment.getAccountReferences());

        return accountReferenceValidationResponse.hasError()
                   ? buildErrorResponseIbanValidation()
                   : ResponseObject.builder().build();
    }

    public ResponseObject validatePeriodicPayment(PeriodicPayment periodicPayment) {

        ResponseObject accountReferenceValidationResponse = referenceValidationService.validateAccountReferences(periodicPayment.getAccountReferences());

        return accountReferenceValidationResponse.hasError()
                   ? buildErrorResponseIbanValidation()
                   : ResponseObject.builder().build();
    }

    public ResponseObject validateBulkPayment(BulkPayment bulkPayment) {

        ResponseObject accountReferenceValidationResponse = referenceValidationService.validateAccountReferences(Collections.singleton(bulkPayment.getDebtorAccount()));

        return accountReferenceValidationResponse.hasError()
                   ? buildErrorResponseIbanValidation()
                   : ResponseObject.builder().build();
    }

    private ResponseObject buildErrorResponseIbanValidation() {
        return ResponseObject.builder()
                   .fail(PIS_400, of(FORMAT_ERROR))
                   .build();
    }

}
