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
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.body.AmountValidator;
import de.adorsys.psd2.xs2a.web.validator.body.payment.config.PaymentValidationConfig;
import de.adorsys.psd2.xs2a.web.validator.body.payment.mapper.PaymentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;

@Component
public class PeriodicPaymentTypeValidatorImpl extends SinglePaymentTypeValidatorImpl {

    @Autowired
    public PeriodicPaymentTypeValidatorImpl(ErrorBuildingService errorBuildingService, ObjectMapper objectMapper,
                                            PaymentMapper paymentMapper, AmountValidator amountValidator) {
        super(errorBuildingService, objectMapper, paymentMapper, amountValidator);
    }

    @Override
    public PaymentType getPaymentType() {
        return PaymentType.PERIODIC;
    }

    @Override
    public void validate(Object body, MessageError messageError, PaymentValidationConfig validationConfig) {
        try {
            doPeriodicValidation(paymentMapper.getPeriodicPayment(body), messageError, validationConfig);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().startsWith("Unrecognized field")) {
                errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_EXTRA_FIELD, extractErrorField(e.getMessage())));
            } else {
                errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR));
            }
        }
    }

    void doPeriodicValidation(PeriodicPayment periodicPayment, MessageError messageError, PaymentValidationConfig validationConfig) {
        super.doSingleValidation(periodicPayment, messageError, validationConfig);

        if (Objects.isNull(periodicPayment.getStartDate())) {
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_NULL_VALUE, "startDate"));
        } else {
            validateStartDate(periodicPayment.getStartDate(), messageError);
        }

        if (Objects.nonNull(periodicPayment.getExecutionRule())) {
            checkFieldForMaxLength(periodicPayment.getExecutionRule().getValue(), "executionRule", validationConfig.getExecutionRule(), messageError);
        }

        if (Objects.isNull(periodicPayment.getFrequency())) {
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_NULL_VALUE, "frequency"));
        }
        if (areDatesInvalidInPeriodicPayment(periodicPayment)) {
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(PERIOD_INVALID_WRONG_ORDER));
        }
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
