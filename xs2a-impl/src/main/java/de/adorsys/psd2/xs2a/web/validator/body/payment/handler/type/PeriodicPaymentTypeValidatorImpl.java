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
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.body.AmountValidator;
import de.adorsys.psd2.xs2a.web.validator.body.FieldLengthValidator;
import de.adorsys.psd2.xs2a.web.validator.body.IbanValidator;
import de.adorsys.psd2.xs2a.web.validator.body.payment.handler.config.PaymentValidationConfig;
import de.adorsys.psd2.xs2a.web.validator.body.payment.handler.service.CustomPaymentValidationService;
import de.adorsys.psd2.xs2a.web.validator.body.payment.mapper.PaymentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;

@Component
public class PeriodicPaymentTypeValidatorImpl extends SinglePaymentTypeValidatorImpl {
    private CustomPaymentValidationService customPaymentValidationService;

    @Autowired
    public PeriodicPaymentTypeValidatorImpl(ErrorBuildingService errorBuildingService, Xs2aObjectMapper xs2aObjectMapper,
                                            PaymentMapper paymentMapper, AmountValidator amountValidator,
                                            IbanValidator ibanValidator, CustomPaymentValidationService customPaymentValidationService,
                                            FieldLengthValidator fieldLengthValidator, AspspProfileServiceWrapper aspspProfileServiceWrapper) {
        super(errorBuildingService, xs2aObjectMapper, paymentMapper, amountValidator, ibanValidator, customPaymentValidationService,
              fieldLengthValidator, aspspProfileServiceWrapper);
        this.customPaymentValidationService = customPaymentValidationService;
    }

    @Override
    public PaymentType getPaymentType() {
        return PaymentType.PERIODIC;
    }

    @Override
    public MessageError validate(Object body, MessageError messageError, PaymentValidationConfig validationConfig) {
        try {
            doPeriodicValidation(paymentMapper.mapToPeriodicPayment(body), messageError, validationConfig);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().startsWith("Unrecognized field")) {
                errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_EXTRA_FIELD, extractErrorField(e.getMessage())));
            } else {
                errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR));
            }
        }

        return messageError;
    }

    void doPeriodicValidation(PeriodicPayment periodicPayment, MessageError messageError, PaymentValidationConfig validationConfig) {
        super.doSingleValidation(periodicPayment, messageError, validationConfig);

        if (periodicPayment.getStartDate() == null) {
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_NULL_VALUE, "startDate"));
        } else {
            validateStartDate(periodicPayment.getStartDate(), messageError);
        }

        if (periodicPayment.getExecutionRule() != null) {
            checkFieldForMaxLength(periodicPayment.getExecutionRule().getValue(), "executionRule", validationConfig.getExecutionRule(), messageError);
        }

        if (periodicPayment.getFrequency() == null) {
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_NULL_VALUE, "frequency"));
        }
        if (areDatesInvalidInPeriodicPayment(periodicPayment)) {
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(PERIOD_INVALID_WRONG_ORDER));
        }
        if (validationConfig.getDayOfExecution().isRequired() && periodicPayment.getDayOfExecution() == null) {
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_NULL_VALUE, "dayOfExecution"));
        }
        customPaymentValidationService.performCustomPeriodicValidation(periodicPayment, messageError, validationConfig);
    }

    private void validateStartDate(LocalDate startDate, MessageError messageError) {
        if (startDate.isBefore(LocalDate.now())) {
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_DATE_IN_THE_PAST, "startDate"));
        }
    }

    private boolean areDatesInvalidInPeriodicPayment(PeriodicPayment periodicPayment) {
        LocalDate paymentStartDate = periodicPayment.getStartDate();

        // Validate if start date is valid
        if (paymentStartDate == null || paymentStartDate.isBefore(LocalDate.now())) {
            return false;
        }

        LocalDate paymentEndDate = periodicPayment.getEndDate();

        return isDateInThePast(paymentStartDate)
                   || Optional.ofNullable(paymentEndDate)
                          .map(dt -> dt.isBefore(paymentStartDate))
                          .orElse(false);
    }
}
