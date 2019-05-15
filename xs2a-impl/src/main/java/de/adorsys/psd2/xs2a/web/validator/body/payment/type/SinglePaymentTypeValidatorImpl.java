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
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.domain.Xs2aAmount;
import de.adorsys.psd2.xs2a.domain.address.Xs2aAddress;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.body.AbstractBodyValidatorImpl;
import de.adorsys.psd2.xs2a.web.validator.body.payment.mapper.PaymentMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.IBANValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.EXECUTION_DATE_INVALID;

@Component
public class SinglePaymentTypeValidatorImpl extends AbstractBodyValidatorImpl implements PaymentTypeValidator {

    private PaymentMapper paymentMapper;

    @Autowired
    public SinglePaymentTypeValidatorImpl(ErrorBuildingService errorBuildingService, ObjectMapper objectMapper,
                                          PaymentMapper paymentMapper) {
        super(errorBuildingService, objectMapper);
        this.paymentMapper = paymentMapper;
    }

    @Override
    public PaymentType getPaymentType() {
        return PaymentType.SINGLE;
    }

    @Override
    public void validate(Object body, MessageError messageError) {
        try {
            doSingleValidation(paymentMapper.getSinglePayment(body), messageError);
        } catch (IllegalArgumentException e) {
            errorBuildingService.enrichMessageError(messageError, e.getMessage());
        }
    }

    void doSingleValidation(SinglePayment singlePayment, MessageError messageError) {
        checkOptionalFieldForMaxLength(singlePayment.getEndToEndIdentification(), "endToEndIdentification", 35, messageError);

        if (Objects.isNull(singlePayment.getDebtorAccount())) {
            errorBuildingService.enrichMessageError(messageError, "Value 'debtorAccount' should not be null");
        } else {
            validateAccount(singlePayment.getDebtorAccount(), messageError);
        }

        if (Objects.isNull(singlePayment.getInstructedAmount())) {
            errorBuildingService.enrichMessageError(messageError, "Value 'instructedAmount' should not be null");
        } else {
            validateInstructedAmount(singlePayment.getInstructedAmount(), messageError);
        }

        if (Objects.isNull(singlePayment.getCreditorAccount())) {
            errorBuildingService.enrichMessageError(messageError, "Value 'creditorAccount' should not be null");
        } else {
            validateAccount(singlePayment.getCreditorAccount(), messageError);
        }

        checkRequiredFieldForMaxLength(singlePayment.getCreditorName(), "creditorName", 70, messageError);

        if (Objects.nonNull(singlePayment.getCreditorAddress())) {
            validateAddress(singlePayment.getCreditorAddress(), messageError);
        }

        if (isDateInThePast(singlePayment.getRequestedExecutionDate())) {
            errorBuildingService.enrichMessageError(messageError, TppMessageInformation.of(EXECUTION_DATE_INVALID, "Value 'requestedExecutionDate' should not be in the past"));
        }
    }

    void validateAddress(Xs2aAddress address, MessageError messageError) {
        checkOptionalFieldForMaxLength(address.getStreet(), "street", 100, messageError);
        checkOptionalFieldForMaxLength(address.getBuildingNumber(), "buildingNumber", 20, messageError);
        checkOptionalFieldForMaxLength(address.getCity(), "city", 100, messageError);
        checkOptionalFieldForMaxLength(address.getPostalCode(), "postalCode", 5, messageError);

        if (Objects.isNull(address.getCountry()) || StringUtils.isBlank(address.getCountry().getCode())) {
            errorBuildingService.enrichMessageError(messageError, "Value 'address.country' is required");
        } else if (!Arrays.asList(Locale.getISOCountries()).contains(address.getCountry().getCode())) {
            errorBuildingService.enrichMessageError(messageError, "Value 'address.country' should be ISO 3166 ALPHA2 country code");
        }
    }

    private void validateInstructedAmount(Xs2aAmount instructedAmount, MessageError messageError) {
        if (Objects.isNull(instructedAmount.getCurrency())) {
            errorBuildingService.enrichMessageError(messageError, "Value 'currency' has wrong format");
        }
        if (Objects.isNull(instructedAmount.getAmount())) {
            errorBuildingService.enrichMessageError(messageError, "Value 'amount' should not be null");
        } else {
            validateAmount(instructedAmount.getAmount(), messageError);
        }
    }

    private void validateAmount(String amount, MessageError messageError) {
        if (!Pattern.matches("-?[0-9]{1,14}(.[0-9]{1,3})?", amount)) {
            errorBuildingService.enrichMessageError(messageError, "Value 'amount' has wrong format");
        }
    }

    void validateAccount(AccountReference accountReference, MessageError messageError) {
        if (StringUtils.isNotBlank(accountReference.getIban()) && !isValidIban(accountReference.getIban())) {
            errorBuildingService.enrichMessageError(messageError, "Invalid IBAN format");
        }
        if (StringUtils.isNotBlank(accountReference.getBban()) && !isValidBban(accountReference.getBban())) {
            errorBuildingService.enrichMessageError(messageError, "Invalid BBAN format");
        }

        checkOptionalFieldForMaxLength(accountReference.getPan(), "PAN", 35, messageError);
        checkOptionalFieldForMaxLength(accountReference.getMaskedPan(), "Masked PAN", 35, messageError);
        checkOptionalFieldForMaxLength(accountReference.getMsisdn(), "MSISDN", 35, messageError);
    }

    private boolean isValidIban(String iban) {
        IBANValidator validator = IBANValidator.getInstance();
        return validator.isValid(iban);
    }

    private boolean isValidBban(String bban) {
        return normalizeString(bban).length() >= 11
                   && normalizeString(bban).length() <= 28; // Can be extended with aprox 50 country specific masks
    }

    private String normalizeString(String string) {
        return string.replaceAll("[^a-zA-Z0-9]", "");
    }

    boolean isDateInThePast(LocalDate dateToCheck) {
        return Optional.ofNullable(dateToCheck)
                   .map(date -> date.isBefore(LocalDate.now()))
                   .orElse(false);
    }
}
