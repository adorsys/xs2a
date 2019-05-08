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

package de.adorsys.psd2.xs2a.web.validator.body.payment.type;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.domain.pis.BulkPayment;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.body.payment.mapper.PaymentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class BulkPaymentTypeValidatorImpl extends SinglePaymentTypeValidatorImpl {

    private PaymentMapper paymentMapper;

    @Autowired
    public BulkPaymentTypeValidatorImpl(ErrorBuildingService errorBuildingService, ObjectMapper objectMapper,
                                        PaymentMapper paymentMapper) {
        super(errorBuildingService, objectMapper, paymentMapper);
        this.paymentMapper = paymentMapper;
    }

    @Override
    public PaymentType getPaymentType() {
        return PaymentType.BULK;
    }

    @Override
    public void validate(Object body, MessageError messageError) {
        try {
            doBulkValidation(paymentMapper.getBulkPayment(body), messageError);
        } catch (IllegalArgumentException e) {
            errorBuildingService.enrichMessageError(messageError, e.getMessage());
        }
    }

    void doBulkValidation(BulkPayment bulkPayment, MessageError messageError) {

        if (Objects.nonNull(bulkPayment.getDebtorAccount())) {
            validateAccount(bulkPayment.getDebtorAccount(), messageError);
        }

        List<SinglePayment> payments = bulkPayment.getPayments();

        payments.forEach(singlePayment -> super.doSingleValidation(singlePayment, messageError));

        if (isDateInThePast(bulkPayment.getRequestedExecutionDate())) {
            errorBuildingService.enrichMessageError(
                messageError, TppMessageInformation.of(MessageErrorCode.EXECUTION_DATE_INVALID, "Value 'requestedExecutionDate' should not be in the past"));
        }
    }
}
