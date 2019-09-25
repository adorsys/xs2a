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

package de.adorsys.psd2.xs2a.service.validator.pis.authorisation;

import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentResponse;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.validator.PsuDataUpdateAuthorisationChecker;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;
import static de.adorsys.psd2.xs2a.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.PIS_401;

@Slf4j
@Component
@RequiredArgsConstructor
public class PisAuthorisationValidator {
    private final RequestProviderService requestProviderService;
    private final PisAuthorisationStatusValidator pisAuthorisationStatusValidator;
    private final PsuDataUpdateAuthorisationChecker psuDataUpdateAuthorisationChecker;

    @NotNull
    public ValidationResult validate(@NotNull String authorisationId, @NotNull PisCommonPaymentResponse commonPaymentResponse, @NotNull PsuIdData psuIdData) {
        Optional<Authorisation> authorisationOptional = findAuthorisationInPayment(authorisationId, commonPaymentResponse);
        if (!authorisationOptional.isPresent()) {
            log.info("InR-ID: [{}], X-Request-ID: [{}], Payment ID: [{}], Authorisation ID: [{}]. Updating PIS initiation authorisation PSU Data has failed: couldn't find authorisation with given authorisationId for payment",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), commonPaymentResponse.getExternalId(), authorisationId);
            return ValidationResult.invalid(ErrorType.PIS_403, RESOURCE_UNKNOWN_403);
        }

        Authorisation authorisation = authorisationOptional.get();
        if (psuDataUpdateAuthorisationChecker.areBothPsusAbsent(psuIdData, authorisation.getPsuData())) {
            log.info("InR-ID: [{}], X-Request-ID: [{}], Payment ID: [{}], Authorisation ID: [{}]. Updating PIS initiation authorisation PSU Data has failed: PSU from authorisation and PSU from request are absent",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), commonPaymentResponse.getExternalId(), authorisationId);
            return ValidationResult.invalid(new MessageError(ErrorType.PIS_400, of(FORMAT_ERROR_NO_PSU)));
        }

        if (!psuDataUpdateAuthorisationChecker.canPsuUpdateAuthorisation(psuIdData, authorisation.getPsuData())) {
            log.info("InR-ID: [{}], X-Request-ID: [{}], Payment ID: [{}], Authorisation ID: [{}]. Updating PIS initiation authorisation PSU Data has failed: PSU from authorisation and PSU from request are different",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), commonPaymentResponse.getExternalId(), authorisationId);
            return ValidationResult.invalid(new MessageError(PIS_401, of(PSU_CREDENTIALS_INVALID)));
        }

        ValidationResult authorisationValidationResult = pisAuthorisationStatusValidator.validate(authorisation.getScaStatus());
        if (authorisationValidationResult.isNotValid()) {
            return authorisationValidationResult;
        }

        return ValidationResult.valid();
    }

    private Optional<Authorisation> findAuthorisationInPayment(String authorisationId, PisCommonPaymentResponse paymentResponse) {
        return paymentResponse.getAuthorisations().stream()
                   .filter(auth -> auth.getId().equals(authorisationId))
                   .findFirst();
    }
}
