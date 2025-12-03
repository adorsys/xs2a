/*
 * Copyright 2018-2025 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.xs2a.web.validator.body.payment.handler.type;
import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
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
import java.time.OffsetDateTime;
import java.util.Optional;

@Component
public class PeriodicPaymentTypeValidatorImpl extends SinglePaymentTypeValidatorImpl {
    private final CustomPaymentValidationService customPaymentValidationService;

    @Autowired
    public PeriodicPaymentTypeValidatorImpl(ErrorBuildingService errorBuildingService,
                                            Xs2aObjectMapper xs2aObjectMapper,
                                            PaymentMapper paymentMapper,
                                            AmountValidator amountValidator,
                                            IbanValidator ibanValidator,
                                            CustomPaymentValidationService customPaymentValidationService,
                                            FieldLengthValidator fieldLengthValidator,
                                            AspspProfileServiceWrapper aspspProfileServiceWrapper) {

        super(errorBuildingService, xs2aObjectMapper, paymentMapper, amountValidator,
            ibanValidator, customPaymentValidationService, fieldLengthValidator, aspspProfileServiceWrapper);

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
                errorBuildingService.enrichMessageError(
                    messageError,
                    TppMessageInformation.of(
                        MessageErrorCode.FORMAT_ERROR_EXTRA_FIELD,
                        extractErrorField(e.getMessage())
                    )
                );
            } else {
                errorBuildingService.enrichMessageError(
                    messageError,
                    TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR)
                );
            }
        }

        return messageError;
    }

    void doPeriodicValidation(PeriodicPayment periodicPayment,
                              MessageError messageError,
                              PaymentValidationConfig validationConfig) {

        super.doSingleValidation(periodicPayment, messageError, validationConfig);

        validateStartDate(periodicPayment.getStartDate(), periodicPayment.getStartDateTime(), messageError);
        validateEndDate(periodicPayment.getEndDate(), periodicPayment.getEndDateTime(), messageError);

        if (periodicPayment.getExecutionRule() != null) {
            checkFieldForMaxLength(
                periodicPayment.getExecutionRule().getValue(),
                "executionRule",
                validationConfig.getExecutionRule(),
                messageError
            );
        }

        if (periodicPayment.getFrequency() == null) {
            errorBuildingService.enrichMessageError(
                messageError,
                TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR_NULL_VALUE, "frequency")
            );
        }

        if (areDatesInvalidInPeriodicPayment(periodicPayment)) {
            errorBuildingService.enrichMessageError(
                messageError,
                TppMessageInformation.of(MessageErrorCode.PERIOD_INVALID_WRONG_ORDER)
            );
        }

        if (areDateTimesInvalidInPeriodicPayment(periodicPayment)) {
            errorBuildingService.enrichMessageError(
                messageError,
                TppMessageInformation.of(MessageErrorCode.PERIOD_INVALID_WRONG_ORDER)
            );
        }

        if (validationConfig.getDayOfExecution().isRequired()
            && periodicPayment.getDayOfExecution() == null) {
            errorBuildingService.enrichMessageError(
                messageError,
                TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR_NULL_VALUE, "dayOfExecution")
            );
        }

        customPaymentValidationService.performCustomPeriodicValidation(periodicPayment, messageError, validationConfig);
    }

    private void validateStartDate(LocalDate startDate,
                                   OffsetDateTime startDateTime,
                                   MessageError messageError) {

        if (startDate != null && startDateTime != null) {
            errorBuildingService.enrichMessageError(
                messageError,
                TppMessageInformation.of(
                    MessageErrorCode.PARAMETER_NOT_CONSISTENT,
                    "startDate, startDateTime"
                )
            );
        }

        if (startDate == null && startDateTime == null) {
            errorBuildingService.enrichMessageError(
                messageError,
                TppMessageInformation.of(
                    MessageErrorCode.FORMAT_ERROR_NULL_VALUE,
                    "startDate and startDateTime"
                )
            );
        } else {
            if (startDate != null && startDate.isBefore(LocalDate.now())) {
                errorBuildingService.enrichMessageError(
                    messageError,
                    TppMessageInformation.of(
                        MessageErrorCode.FORMAT_ERROR_DATE_IN_THE_PAST,
                        "startDate"
                    )
                );
            }
            if (startDateTime != null && startDateTime.isBefore(OffsetDateTime.now())) {
                errorBuildingService.enrichMessageError(
                    messageError,
                    TppMessageInformation.of(
                        MessageErrorCode.FORMAT_ERROR_DATE_IN_THE_PAST,
                        "startDateTime"
                    )
                );
            }
        }
    }

    private void validateEndDate(LocalDate endDate,
                                 OffsetDateTime endDateTime,
                                 MessageError messageError) {

        if (endDate != null && endDateTime != null) {
            errorBuildingService.enrichMessageError(
                messageError,
                TppMessageInformation.of(
                    MessageErrorCode.PARAMETER_NOT_CONSISTENT,
                    "endDate, endDateTime"
                )
            );
        }
    }

    private boolean areDatesInvalidInPeriodicPayment(PeriodicPayment periodicPayment) {
        LocalDate paymentStartDate = periodicPayment.getStartDate();

        if (paymentStartDate == null || paymentStartDate.isBefore(LocalDate.now())) {
            return false;
        }

        LocalDate paymentEndDate = periodicPayment.getEndDate();

        return isDateInThePast(paymentStartDate)
            || Optional.ofNullable(paymentEndDate)
            .map(dt -> dt.isBefore(paymentStartDate))
            .orElse(false);
    }

    private boolean areDateTimesInvalidInPeriodicPayment(PeriodicPayment periodicPayment) {
        OffsetDateTime paymentStartDateTime = periodicPayment.getStartDateTime();

        if (paymentStartDateTime == null || paymentStartDateTime.isBefore(OffsetDateTime.now())) {
            return false;
        }

        OffsetDateTime paymentEndDateTime = periodicPayment.getEndDateTime();

        return isTimeInThePast(paymentStartDateTime)
            || Optional.ofNullable(paymentEndDateTime)
            .map(dt -> dt.isBefore(paymentStartDateTime))
            .orElse(false);
    }
}
