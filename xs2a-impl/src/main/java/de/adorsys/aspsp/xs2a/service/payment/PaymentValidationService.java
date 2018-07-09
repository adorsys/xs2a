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

package de.adorsys.aspsp.xs2a.service.payment;

import de.adorsys.aspsp.xs2a.domain.MessageErrorCode;
import de.adorsys.aspsp.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayments;
import de.adorsys.aspsp.xs2a.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static de.adorsys.aspsp.xs2a.domain.MessageErrorCode.*;
import static java.util.Optional.empty;
import static java.util.Optional.of;

@Service
@RequiredArgsConstructor
public class PaymentValidationService {
    private final AccountService accountService;

    public Optional<MessageErrorCode> validatePeriodicPayment(PeriodicPayment payment, String paymentProduct) {
        if (payment == null) { //TODO Should be removed with https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/167
            return of(FORMAT_ERROR);
        }
        return payment.isValidDate()
                   ? containsPaymentRelatedErrors(payment, paymentProduct)
                   : of(EXECUTION_DATE_INVALID);
    }

    public Optional<MessageErrorCode> validateSinglePayment(SinglePayments payment, String paymentProduct) {
        if (payment == null) { //TODO Should be removed with https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/167
            return of(FORMAT_ERROR);
        }
        return payment.isValidDated() //TODO Should be removed with https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/167
                   ? containsPaymentRelatedErrors(payment, paymentProduct)
                   : of(EXECUTION_DATE_INVALID);
    }

    private Optional<MessageErrorCode> containsPaymentRelatedErrors(SinglePayments payment, String paymentProduct) {
        if (!accountService.getAccountDetailsByAccountReference(payment.getDebtorAccount()).isPresent()) {
            return of(RESOURCE_UNKNOWN_400);
        }
        if (accountService.isInvalidPaymentProductForPsu(payment.getDebtorAccount(), paymentProduct)) {
            return of(PRODUCT_INVALID);
        }
        return empty();
    }
}
