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

package de.adorsys.psd2.xs2a.service.validator.pis.payment.raw;

import de.adorsys.psd2.validator.payment.PaymentBusinessValidator;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.profile.StandardPaymentProductsResolver;
import de.adorsys.psd2.xs2a.service.validator.SupportedAccountReferenceValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Set;

@Primary
@Component
@RequiredArgsConstructor
public class DefaultPaymentBusinessValidatorImpl implements PaymentBusinessValidator {
    private final PaymentAccountReferenceExtractor paymentAccountReferenceExtractor;
    private final StandardPaymentProductsResolver standardPaymentProductsResolver;
    private final SupportedAccountReferenceValidator supportedAccountReferenceValidator;

    @Override
    public ValidationResult validate(byte[] body, String paymentProduct, PaymentType paymentType) {
        if (standardPaymentProductsResolver.isRawPaymentProduct(paymentProduct)) {
            return ValidationResult.valid();
        }

        Set<AccountReference> accountReferences = paymentAccountReferenceExtractor.extractAccountReferences(body, paymentType);
        return supportedAccountReferenceValidator.validate(accountReferences);
    }
}
