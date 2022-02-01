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

package de.adorsys.psd2.xs2a.service.validator.pis.payment;

import de.adorsys.psd2.validator.payment.PaymentBusinessValidator;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationParameters;
import de.adorsys.psd2.xs2a.service.validator.BusinessValidator;
import de.adorsys.psd2.xs2a.service.validator.PsuDataInInitialRequestValidator;
import de.adorsys.psd2.xs2a.service.validator.TppNotificationDataValidator;
import de.adorsys.psd2.xs2a.service.validator.TppUriHeaderValidator;
import de.adorsys.psd2.xs2a.service.validator.pis.payment.dto.CreatePaymentRequestObject;
import de.adorsys.psd2.xs2a.validator.payment.CountryPaymentValidatorResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Validator to be used for validating create payment request according to some business rules
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreatePaymentValidator implements BusinessValidator<CreatePaymentRequestObject> {
    private final PsuDataInInitialRequestValidator psuDataInInitialRequestValidator;
    private final CountryPaymentValidatorResolver countryPaymentValidatorResolver;
    private final TppUriHeaderValidator tppUriHeaderValidator;
    private final TppNotificationDataValidator tppNotificationDataValidator;

    /**
     * Validates create payment request by checking whether:
     * <ul>
     * <li>Payment product and payment type are correct</li>
     * <li>PSU Data is present in the request if it's mandated by the profile</li>
     * <li>Account references are supported by ASPSP</li>
     * </ul>
     *
     * @param createPaymentRequestObject payment request object with information about the payment
     * @return valid result if the parameters are valid, invalid result with appropriate error otherwise
     */
    @NotNull
    @Override
    public ValidationResult validate(@NotNull CreatePaymentRequestObject createPaymentRequestObject) {
        PaymentInitiationParameters paymentInitiationParameters = createPaymentRequestObject.getPaymentInitiationParameters();

        ValidationResult psuDataValidationResult = psuDataInInitialRequestValidator.validate(paymentInitiationParameters.getPsuData());
        if (psuDataValidationResult.isNotValid()) {
            return psuDataValidationResult;
        }

        PaymentType paymentType = paymentInitiationParameters.getPaymentType();
        String paymentProduct = paymentInitiationParameters.getPaymentProduct();

        PaymentBusinessValidator countrySpecificBusinessValidator = countryPaymentValidatorResolver.getPaymentBusinessValidator();
        return countrySpecificBusinessValidator.validate(createPaymentRequestObject.getPayment(), paymentProduct, paymentType);
    }

    @Override
    public @NotNull Set<TppMessageInformation> buildWarningMessages(@NotNull CreatePaymentRequestObject createPaymentRequestObject) {
        Set<TppMessageInformation> warnings = new HashSet<>();

        warnings.addAll(tppUriHeaderValidator.buildWarningMessages(createPaymentRequestObject.getPaymentInitiationParameters().getTppRedirectUri()));
        warnings.addAll(tppNotificationDataValidator.buildWarningMessages(createPaymentRequestObject.getPaymentInitiationParameters().getTppNotificationData()));

        return warnings;
    }
}
