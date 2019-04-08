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

package de.adorsys.psd2.xs2a.service.validator.pis.authorisation.cancellation;

import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentResponse;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.pis.AbstractPisTppValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.PSU_CREDENTIALS_INVALID;

/**
 * Validator to be used for validating create PIS cancellation authorisation request according to some business rules
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreatePisCancellationAuthorisationValidator extends AbstractPisTppValidator<CreatePisCancellationAuthorisationPO> {
    private final RequestProviderService requestProviderService;

    /**
     * Validates create PIS cancellation authorisation request by checking whether:
     * <ul>
     * <li>PSU Data in request matches the PSU Data in payment</li>
     * </ul>
     *
     * @param paymentObject payment information object
     * @return valid result if the payment is valid, invalid result with appropriate error otherwise
     */
    @Override
    protected ValidationResult executeBusinessValidation(CreatePisCancellationAuthorisationPO paymentObject) {
        PsuIdData psuData = paymentObject.getPsuData();
        PisCommonPaymentResponse pisCommonPaymentResponse = paymentObject.getPisCommonPaymentResponse();
        if (psuData.isNotEmpty() && !isPsuDataCorrect(pisCommonPaymentResponse, psuData)) {
            log.info("X-Request-ID: [{}], Payment ID: [{}]. Creation of PIS cancellation authorisation has failed: PSU Data in request doesn't match PSU Data in payment",
                     requestProviderService.getRequestId(), pisCommonPaymentResponse.getExternalId());
            return ValidationResult.invalid(ErrorType.PIS_401, TppMessageInformation.of(PSU_CREDENTIALS_INVALID));
        }

        return ValidationResult.valid();
    }

    private boolean isPsuDataCorrect(PisCommonPaymentResponse pisCommonPaymentResponse, PsuIdData psuData) {
        List<PsuIdData> psuIdDataList = pisCommonPaymentResponse.getPsuData();

        return psuIdDataList.stream()
                   .anyMatch(psu -> psu.contentEquals(psuData));
    }
}
