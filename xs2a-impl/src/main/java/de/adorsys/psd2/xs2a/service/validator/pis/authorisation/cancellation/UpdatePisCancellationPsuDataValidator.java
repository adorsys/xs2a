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

import de.adorsys.psd2.xs2a.core.pis.PaymentAuthorisationType;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.validator.PisEndpointAccessCheckerService;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.pis.AbstractPisTppValidator;
import de.adorsys.psd2.xs2a.service.validator.pis.authorisation.PisAuthorisationValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.SERVICE_BLOCKED;

/**
 * Validator to be used for validating create PIS cancellation authorisation request according to some business rules
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UpdatePisCancellationPsuDataValidator extends AbstractPisTppValidator<UpdatePisCancellationPsuDataPO> {
    private final PisEndpointAccessCheckerService pisEndpointAccessCheckerService;
    private final RequestProviderService requestProviderService;
    private final PisAuthorisationValidator pisAuthorisationValidator;

    /**
     * Validates update PSU Data in payment authorisation request by checking whether:
     * <ul>
     * <li>endpoint is accessible for given authorisation</li>
     * </ul>
     *
     * @param paymentObject payment information object
     * @return valid result if the payment is valid, invalid result with appropriate error otherwise
     */
    @Override
    protected ValidationResult executeBusinessValidation(UpdatePisCancellationPsuDataPO paymentObject) {
        String authorisationId = paymentObject.getAuthorisationId();
        if (!pisEndpointAccessCheckerService.isEndpointAccessible(authorisationId, PaymentAuthorisationType.CANCELLED)) {
            log.info("InR-ID: [{}], X-Request-ID: [{}], Authorisation ID: [{}]. Updating PIS cancellation authorisation PSU Data has failed: endpoint is not accessible for authorisation",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), authorisationId);
            return ValidationResult.invalid(ErrorType.PIS_403, TppMessageInformation.of(SERVICE_BLOCKED));
        }

        ValidationResult authorisationValidationResult = pisAuthorisationValidator.validate(authorisationId, paymentObject.getPisCommonPaymentResponse());
        if (authorisationValidationResult.isNotValid()) {
            return authorisationValidationResult;
        }

        return ValidationResult.valid();
    }
}
