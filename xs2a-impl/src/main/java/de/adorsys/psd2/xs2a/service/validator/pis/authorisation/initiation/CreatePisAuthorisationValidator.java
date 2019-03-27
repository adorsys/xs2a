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

package de.adorsys.psd2.xs2a.service.validator.pis.authorisation.initiation;

import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentResponse;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.pis.AbstractPisTppValidator;
import de.adorsys.psd2.xs2a.service.validator.pis.CommonPaymentObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.RESOURCE_EXPIRED_403;
import static de.adorsys.psd2.xs2a.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.PIS_403;

/**
 * Validator to be used for validating create PIS authorisation request according to some business rules
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreatePisAuthorisationValidator extends AbstractPisTppValidator<CommonPaymentObject> {
    private final RequestProviderService requestProviderService;

    /**
     * Validates create PIS authorisation request by checking whether:
     * <ul>
     * <li>payment is not expired</li>
     * </ul>
     *
     * @param paymentObject payment information object
     * @return valid result if the payment is valid, invalid result with appropriate error otherwise
     */
    @Override
    protected ValidationResult executeBusinessValidation(CommonPaymentObject paymentObject) {
        // TODO temporary solution: CMS should be refactored to return response objects instead of Strings, Enums, Booleans etc., so we should receive this error from CMS https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/581
        PisCommonPaymentResponse pisCommonPaymentResponse = paymentObject.getPisCommonPaymentResponse();
        if (pisCommonPaymentResponse.getTransactionStatus() == TransactionStatus.RJCT) {
            log.info("X-Request-ID: [{}], Payment ID: [{}]. Creation of PIS authorisation has failed: payment has been rejected",
                     requestProviderService.getRequestId(), pisCommonPaymentResponse.getExternalId());
            return ValidationResult.invalid(PIS_403, of(RESOURCE_EXPIRED_403));
        }

        return ValidationResult.valid();
    }
}
