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

import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.domain.pis.PaymentAuthorisationType;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.validator.PisEndpointAccessCheckerService;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.pis.AbstractPisTppValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.RESOURCE_EXPIRED_403;
import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.SERVICE_BLOCKED;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.PIS_403;

/**
 * Validator to be used for validating update PSU Data in payment authorisation request according to some business rules
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UpdatePisCommonPaymentPsuDataValidator extends AbstractPisTppValidator<UpdatePisCommonPaymentPsuDataPO> {
    private final PisEndpointAccessCheckerService pisEndpointAccessCheckerService;
    private final RequestProviderService requestProviderService;

    /**
     * Validates update PSU Data in payment authorisation request by checking whether:
     * <ul>
     * <li>endpoint is accessible for given authorisation</li>
     * <li>payment is not expired</li>
     * </ul>
     *
     * @param paymentObject payment information object
     * @return valid result if the payment is valid, invalid result with appropriate error otherwise
     */
    @Override
    protected ValidationResult executeBusinessValidation(UpdatePisCommonPaymentPsuDataPO paymentObject) {
        String authorisationId = paymentObject.getAuthorisationId();
        if (!pisEndpointAccessCheckerService.isEndpointAccessible(authorisationId, PaymentAuthorisationType.INITIATION)) {
            log.info("X-Request-ID: [{}], Authorisation ID: [{}]. Updating PIS initiation authorisation PSU Data  has failed: endpoint is not accessible for authorisation",
                     requestProviderService.getRequestId(), authorisationId);
            return ValidationResult.invalid(PIS_403, TppMessageInformation.of(SERVICE_BLOCKED));
        }

        // TODO temporary solution: CMS should be refactored to return response objects instead of Strings, Enums, Booleans etc., so we should receive this error from CMS https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/581
        if (paymentObject.getPisCommonPaymentResponse().getTransactionStatus() == TransactionStatus.RJCT) {
            log.info("X-Request-ID: [{}], Authorisation ID: [{}]. Updating PIS initiation authorisation PSU Data has failed: payment has been rejected",
                     requestProviderService.getRequestId(), authorisationId);
            return ValidationResult.invalid(PIS_403, TppMessageInformation.of(RESOURCE_EXPIRED_403));
        }

        return ValidationResult.valid();
    }
}
