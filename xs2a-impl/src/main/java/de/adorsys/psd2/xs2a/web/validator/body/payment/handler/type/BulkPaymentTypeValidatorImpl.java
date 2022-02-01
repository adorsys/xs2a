/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
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
 * contact us at psd2@adorsys.com.
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
    private final CustomPaymentValidationService customPaymentValidationService;
    private final AspspProfileServiceWrapper aspspProfileServiceWrapper;

    @Autowired
    public BulkPaymentTypeValidatorImpl(ErrorBuildingService errorBuildingService, Xs2aObjectMapper xs2aObjectMapper,
                                        PaymentMapper paymentMapper, AmountValidator amountValidator,
                                        IbanValidator ibanValidator, CustomPaymentValidationService customPaymentValidationService,
                                        FieldLengthValidator fieldLengthValidator, AspspProfileServiceWrapper aspspProfileServiceWrapper) {
        super(errorBuildingService, xs2aObjectMapper, paymentMapper, amountValidator, ibanValidator, customPaymentValidationService,
              fieldLengthValidator, aspspProfileServiceWrapper);
        this.customPaymentValidationService = customPaymentValidationService;
        this.aspspProfileServiceWrapper = aspspProfileServiceWrapper;
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

        if (bulkPayment.getDebtorAccount() != null) {
            validateAccount(bulkPayment.getDebtorAccount(), messageError, validationConfig);
        } else if (!aspspProfileServiceWrapper.isDebtorAccountOptionalInInitialRequest()) {
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR_NULL_VALUE, "debtorAccount"));
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
