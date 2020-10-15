/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.web.validator.body.payment.handler.service;

import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.domain.pis.BulkPayment;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.web.validator.body.payment.handler.config.PaymentValidationConfig;
import org.springframework.stereotype.Service;

@Service
public class CustomPaymentValidationService {
    public void performCustomSingleValidation(SinglePayment payment, MessageError messageError, PaymentValidationConfig validationConfig) {
        // could be extended on connector side for custom validation
    }

    public void performCustomPeriodicValidation(PeriodicPayment payment, MessageError messageError, PaymentValidationConfig validationConfig) {
        // could be extended on connector side for custom validation
    }

    public void performCustomBulkValidation(BulkPayment payment, MessageError messageError, PaymentValidationConfig validationConfig) {
        // could be extended on connector side for custom validation
    }
}
