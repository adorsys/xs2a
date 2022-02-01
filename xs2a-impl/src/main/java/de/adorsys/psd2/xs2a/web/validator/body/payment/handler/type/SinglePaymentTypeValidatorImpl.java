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
import de.adorsys.psd2.xs2a.core.domain.address.Xs2aAddress;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.pis.Xs2aAmount;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.body.AbstractBodyValidatorImpl;
import de.adorsys.psd2.xs2a.web.validator.body.AmountValidator;
import de.adorsys.psd2.xs2a.web.validator.body.FieldLengthValidator;
import de.adorsys.psd2.xs2a.web.validator.body.IbanValidator;
import de.adorsys.psd2.xs2a.web.validator.body.payment.handler.config.PaymentValidationConfig;
import de.adorsys.psd2.xs2a.web.validator.body.payment.handler.service.CustomPaymentValidationService;
import de.adorsys.psd2.xs2a.web.validator.body.payment.mapper.PaymentMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;

@Component
public class SinglePaymentTypeValidatorImpl extends AbstractBodyValidatorImpl implements PaymentTypeValidator {

    protected PaymentMapper paymentMapper;
    private final AmountValidator amountValidator;
    private final IbanValidator ibanValidator;
    private final CustomPaymentValidationService customPaymentValidationService;
    private final AspspProfileServiceWrapper aspspProfileServiceWrapper;

    @Autowired
    public SinglePaymentTypeValidatorImpl(ErrorBuildingService errorBuildingService, Xs2aObjectMapper xs2aObjectMapper,
                                          PaymentMapper paymentMapper, AmountValidator amountValidator,
                                          IbanValidator ibanValidator, CustomPaymentValidationService customPaymentValidationService,
                                          FieldLengthValidator fieldLengthValidator, AspspProfileServiceWrapper aspspProfileServiceWrapper) {
        super(errorBuildingService, xs2aObjectMapper, fieldLengthValidator);
        this.paymentMapper = paymentMapper;
        this.amountValidator = amountValidator;
        this.ibanValidator = ibanValidator;
        this.customPaymentValidationService = customPaymentValidationService;
        this.aspspProfileServiceWrapper = aspspProfileServiceWrapper;
    }

    @Override
    public PaymentType getPaymentType() {
        return PaymentType.SINGLE;
    }

    @Override
    public MessageError validate(Object body, MessageError messageError, PaymentValidationConfig validationConfig) {
        try {
            doSingleValidation(paymentMapper.mapToSinglePayment(body), messageError, validationConfig);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().startsWith("Unrecognized field")) {
                errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_EXTRA_FIELD, extractErrorField(e.getMessage())));
            } else {
                errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR));
            }
        }

        return messageError;
    }

    void doSingleValidation(SinglePayment singlePayment, MessageError messageError, PaymentValidationConfig validationConfig) {
        checkFieldForMaxLength(singlePayment.getEndToEndIdentification(), "endToEndIdentification", validationConfig.getEndToEndIdentification(), messageError);

        if (singlePayment.getDebtorAccount() != null) {
            validateAccount(singlePayment.getDebtorAccount(), messageError, validationConfig);
        } else if (!aspspProfileServiceWrapper.isDebtorAccountOptionalInInitialRequest()) {
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_NULL_VALUE, "debtorAccount"));
        }

        if (singlePayment.getInstructedAmount() == null) {
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_NULL_VALUE, "instructedAmount"));
        } else {
            validateInstructedAmount(singlePayment.getInstructedAmount(), messageError);
        }

        if (singlePayment.getCreditorAccount() == null) {
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_NULL_VALUE, "creditorAccount"));
        } else {
            validateAccount(singlePayment.getCreditorAccount(), messageError, validationConfig);
        }

        checkFieldForMaxLength(singlePayment.getCreditorName(), "creditorName", validationConfig.getCreditorName(), messageError);

        if (singlePayment.getCreditorAddress() != null) {
            validateAddress(singlePayment.getCreditorAddress(), messageError, validationConfig);
        }

        if (isDateInThePast(singlePayment.getRequestedExecutionDate())) {
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(EXECUTION_DATE_INVALID_IN_THE_PAST));
        }

        checkFieldForMaxLength(singlePayment.getCreditorId(), "creditorId", validationConfig.getCreditorId(), messageError);
        checkFieldForMaxLength(singlePayment.getUltimateDebtor(), "ultimateDebtor", validationConfig.getUltimateDebtor(), messageError);
        checkFieldForMaxLength(singlePayment.getUltimateCreditor(), "ultimateCreditor", validationConfig.getUltimateDebtor(), messageError);
        checkFieldForMaxLength(singlePayment.getInstructionIdentification(), "instructionIdentification", validationConfig.getInstructionIdentification(), messageError);
        checkFieldForMaxLength(singlePayment.getDebtorName(), "debtorName", validationConfig.getDebtorName(), messageError);
        checkFieldForMaxLength(singlePayment.getRemittanceInformationStructured(), "remittanceInformationStructured", validationConfig.getRemittanceInformationStructured(), messageError);
        validateRemittanceInformationStructuredArray(singlePayment.getRemittanceInformationStructuredArray(), messageError, validationConfig);
        customPaymentValidationService.performCustomSingleValidation(singlePayment, messageError, validationConfig);
    }

    void validateAddress(Xs2aAddress address, MessageError messageError, PaymentValidationConfig validationConfig) {
        checkFieldForMaxLength(address.getStreetName(), "streetName", validationConfig.getStreetName(), messageError);
        checkFieldForMaxLength(address.getBuildingNumber(), "buildingNumber", validationConfig.getBuildingNumber(), messageError);
        checkFieldForMaxLength(address.getTownName(), "townName", validationConfig.getTownName(), messageError);
        checkFieldForMaxLength(address.getPostCode(), "postCode", validationConfig.getPostCode(), messageError);

        if (address.getCountry() == null || StringUtils.isBlank(address.getCountry().getCode())) {
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_VALUE_REQUIRED, "address.country"));
        } else if (!Arrays.asList(Locale.getISOCountries()).contains(address.getCountry().getCode())) {
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_ADDRESS_COUNTRY_INCORRECT));
        }
    }

    private void validateInstructedAmount(Xs2aAmount instructedAmount, MessageError messageError) {
        if (instructedAmount.getCurrency() == null) {
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_WRONG_FORMAT_VALUE, "currency"));
        }
        if (instructedAmount.getAmount() == null) {
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_NULL_VALUE, "amount"));
        } else {
            amountValidator.validateAmount(instructedAmount.getAmount(), messageError);
        }
    }

    void validateAccount(AccountReference accountReference, MessageError messageError, PaymentValidationConfig validationConfig) {
        ibanValidator.validate(accountReference.getIban(), messageError);

        if (StringUtils.isNotBlank(accountReference.getBban()) && !isValidBban(accountReference.getBban())) {
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(FORMAT_ERROR_INVALID_FIELD, "BBAN"));
        }

        checkFieldForMaxLength(accountReference.getPan(), "PAN", validationConfig.getPan(), messageError);
        checkFieldForMaxLength(accountReference.getMaskedPan(), "Masked PAN", validationConfig.getMaskedPan(), messageError);
        checkFieldForMaxLength(accountReference.getMsisdn(), "MSISDN", validationConfig.getMsisdn(), messageError);
    }

    private boolean isValidBban(String bban) {
        return normalizeString(bban).length() >= 11
                   && normalizeString(bban).length() <= 28; // Can be extended with aprox 50 country specific masks
    }

    private String normalizeString(String string) {
        return string.replaceAll("[^a-zA-Z0-9]", "");
    }

    private void validateRemittanceInformationStructuredArray(List<String> remittanceList, MessageError messageError, PaymentValidationConfig validationConfig) {
        if (CollectionUtils.isNotEmpty(remittanceList)) {
            remittanceList.forEach(remittance -> checkFieldForMaxLength(remittance, "remittanceInformationStructured", validationConfig.getRemittanceInformationStructured(), messageError));
        }
    }

    boolean isDateInThePast(LocalDate dateToCheck) {
        return Optional.ofNullable(dateToCheck)
                   .map(date -> date.isBefore(LocalDate.now()))
                   .orElse(false);
    }
}
