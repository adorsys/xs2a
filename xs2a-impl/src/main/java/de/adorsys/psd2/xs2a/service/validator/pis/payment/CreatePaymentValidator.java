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

package de.adorsys.psd2.xs2a.service.validator.pis.payment;

import de.adorsys.psd2.validator.payment.PaymentBusinessValidator;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationParameters;
import de.adorsys.psd2.xs2a.service.mapper.ValidationResultMapper;
import de.adorsys.psd2.xs2a.service.validator.BusinessValidator;
import de.adorsys.psd2.xs2a.service.validator.PsuDataInInitialRequestValidator;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.pis.PaymentTypeAndProductValidator;
import de.adorsys.psd2.xs2a.service.validator.pis.payment.dto.CreatePaymentRequestObject;
import de.adorsys.psd2.xs2a.validator.payment.CountryPaymentValidatorResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

/**
 * Validator to be used for validating create payment request according to some business rules
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreatePaymentValidator implements BusinessValidator<CreatePaymentRequestObject> {
    private final PsuDataInInitialRequestValidator psuDataInInitialRequestValidator;
    private final PaymentTypeAndProductValidator paymentProductAndTypeValidator;
    private final CountryPaymentValidatorResolver countryPaymentValidatorResolver;
    private final ValidationResultMapper validationResultMapper;

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
        PaymentType paymentType = paymentInitiationParameters.getPaymentType();
        String paymentProduct = paymentInitiationParameters.getPaymentProduct();

        ValidationResult productAndTypeValidationResult = paymentProductAndTypeValidator.validateTypeAndProduct(paymentType, paymentProduct);
        if (productAndTypeValidationResult.isNotValid()) {
            return productAndTypeValidationResult;
        }

        ValidationResult psuDataValidationResult = psuDataInInitialRequestValidator.validate(paymentInitiationParameters.getPsuData());
        if (psuDataValidationResult.isNotValid()) {
            return psuDataValidationResult;
        }

        PaymentBusinessValidator countrySpecificBusinessValidator = countryPaymentValidatorResolver.getPaymentBusinessValidator();
        de.adorsys.psd2.xs2a.core.service.validator.ValidationResult countrySpecificValidationResult = countrySpecificBusinessValidator.validate(createPaymentRequestObject.getPayment(), paymentProduct, paymentType);
        return validationResultMapper.mapToXs2aValidationResult(countrySpecificValidationResult);
    }
}
