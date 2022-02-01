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
