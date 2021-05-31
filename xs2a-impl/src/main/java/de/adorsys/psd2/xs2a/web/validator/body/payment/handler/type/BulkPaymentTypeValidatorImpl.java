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

package de.adorsys.psd2.xs2a.web.validator.body.payment.handler.type;

import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.domain.pis.BulkPayment;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.body.AmountValidator;
import de.adorsys.psd2.xs2a.web.validator.body.FieldLengthValidator;
import de.adorsys.psd2.xs2a.web.validator.body.IbanValidator;
import de.adorsys.psd2.xs2a.web.validator.body.payment.handler.config.PaymentValidationConfig;
import de.adorsys.psd2.xs2a.web.validator.body.payment.handler.service.CustomPaymentValidationService;
import de.adorsys.psd2.xs2a.web.validator.body.payment.mapper.PaymentMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class BulkPaymentTypeValidatorImpl extends SinglePaymentTypeValidatorImpl {
    private CustomPaymentValidationService customPaymentValidationService;

    @Autowired
    public BulkPaymentTypeValidatorImpl(ErrorBuildingService errorBuildingService, Xs2aObjectMapper xs2aObjectMapper,
                                        PaymentMapper paymentMapper, AmountValidator amountValidator,
                                        IbanValidator ibanValidator, CustomPaymentValidationService customPaymentValidationService,
                                        FieldLengthValidator fieldLengthValidator, AspspProfileServiceWrapper aspspProfileServiceWrapper) {
        super(errorBuildingService, xs2aObjectMapper, paymentMapper, amountValidator, ibanValidator, customPaymentValidationService,
              fieldLengthValidator, aspspProfileServiceWrapper);
        this.customPaymentValidationService = customPaymentValidationService;
    }

    @Override
    public PaymentType getPaymentType() {
        return PaymentType.BULK;
    }

    @Override
    public MessageError validate(Object body, MessageError messageError, PaymentValidationConfig validationConfig) {
        try {
            doBulkValidation(paymentMapper.mapToBulkPayment(body), messageError, validationConfig);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().startsWith("Unrecognized field")) {
                errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR_EXTRA_FIELD, extractErrorField(e.getMessage())));
            } else {
                errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR));
            }
        }
        return messageError;
    }

    void doBulkValidation(BulkPayment bulkPayment, MessageError messageError, PaymentValidationConfig validationConfig) {

        if (bulkPayment.getDebtorAccount() == null) {
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR_NULL_VALUE, "debtorAccount"));
        } else {
            validateAccount(bulkPayment.getDebtorAccount(), messageError, validationConfig);
        }

        List<SinglePayment> payments = bulkPayment.getPayments();

        if (CollectionUtils.isEmpty(payments)) {
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR_BULK));
        } else {
            payments.forEach(singlePayment -> super.doSingleValidation(singlePayment, messageError, validationConfig));
        }

        if (isDateInThePast(bulkPayment.getRequestedExecutionDate())) {
            errorBuildingService.enrichMessageError(
                messageError, TppMessageInformation.of(MessageErrorCode.EXECUTION_DATE_INVALID_IN_THE_PAST));
        }

        customPaymentValidationService.performCustomBulkValidation(bulkPayment, messageError, validationConfig);
    }
}
