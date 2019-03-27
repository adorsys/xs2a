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

package de.adorsys.psd2.xs2a.service.validator;

import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentResponse;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class GetCommonPaymentByIdResponseValidator {
    private final RequestProviderService requestProviderService;

    /**
     * Validates get payment by id response for cancel payment, get payment and payment status calls according to:
     * <ul>
     * <li>if get payment by id result contains data</li>
     * <li>if payment type is valid</li>
     * <li>if payment type is valid</li>
     * </ul>
     * If there are new get payment by id response validation requirements, this method has to be updated.
     *
     * @param pisCommonPayment unwrapped response of Xs2aPisCommonPaymentService#getPisCommonPaymentById call
     * @param paymentType      type of payment (payments, bulk-payments, periodic-payments)
     * @param paymentProduct   payment product used for payment creation (e.g. sepa-credit-transfers, instant-sepa-credit-transfers...)
     * @return ValidationResult instance, that contains boolean isValid, that shows if request is valid
     * and MessageError for invalid case
     */
    public ValidationResult validateRequest(@Nullable PisCommonPaymentResponse pisCommonPayment, PaymentType paymentType, String paymentProduct) {
        //In case of removing this validation please, place it back before all invocations of this class and remove @Nullable from the method parameter
        if (pisCommonPayment == null) {
            log.info("X-Request-ID: [{}]. Payment validation has failed: payment was not found", requestProviderService.getRequestId());
            return ValidationResult.invalid(ErrorType.PIS_404, TppMessageInformation.of(RESOURCE_UNKNOWN_404, "Payment not found"));
        }

        if (isPaymentTypeIncorrect(paymentType, pisCommonPayment)) {
            log.info("X-Request-ID: [{}], Payment ID: [{}]. Payment validation has failed: payment type [{}] is incorrect",
                     requestProviderService.getRequestId(), pisCommonPayment.getExternalId(), paymentType);
            return ValidationResult.invalid(ErrorType.PIS_405, TppMessageInformation.of(SERVICE_INVALID_405, "Service invalid for addressed payment"));
        }

        if (isPaymentProductIncorrect(paymentProduct, pisCommonPayment)) {
            log.info("X-Request-ID: [{}], Payment ID: [{}]. Payment validation has failed: payment product [{}] is incorrect",
                     requestProviderService.getRequestId(), pisCommonPayment.getExternalId(), paymentProduct);
            return ValidationResult.invalid(ErrorType.PIS_403, TppMessageInformation.of(PRODUCT_INVALID, "Payment product invalid for addressed payment"));
        }

        return ValidationResult.valid();
    }

    private boolean isPaymentTypeIncorrect(PaymentType paymentType, PisCommonPaymentResponse commonPaymentResponse) {
        return commonPaymentResponse.getPaymentType() != paymentType;
    }

    private boolean isPaymentProductIncorrect(String paymentProduct, PisCommonPaymentResponse commonPaymentResponse) {
        return !commonPaymentResponse.getPaymentProduct().equalsIgnoreCase(paymentProduct);
    }
}

