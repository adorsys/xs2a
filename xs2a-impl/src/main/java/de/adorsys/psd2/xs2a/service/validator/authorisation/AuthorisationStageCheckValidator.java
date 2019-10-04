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

package de.adorsys.psd2.xs2a.service.validator.authorisation;

import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationServiceType;
import de.adorsys.psd2.xs2a.domain.authorisation.UpdateAuthorisationRequest;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.SERVICE_INVALID_400;
import static de.adorsys.psd2.xs2a.core.sca.ScaStatus.*;
import static de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationServiceType.AIS;

/**
 * Checks whether the incoming request authorisation data matches the current authorisation stage
 */
@Component
public class AuthorisationStageCheckValidator {

    public ValidationResult validate(@NotNull UpdateAuthorisationRequest updateRequest, @NotNull ScaStatus scaStatus, @NotNull AuthorisationServiceType authType) {
        if (EnumSet.of(PSUIDENTIFIED, RECEIVED).contains(scaStatus) && updateRequest.getPassword() == null) {
            return ValidationResult.invalid(resolveErrorType(authType), SERVICE_INVALID_400);
        }

        if (scaStatus == PSUAUTHENTICATED && updateRequest.getAuthenticationMethodId() == null) {
            return ValidationResult.invalid(resolveErrorType(authType), SERVICE_INVALID_400);
        }

        if (scaStatus == SCAMETHODSELECTED && updateRequest.getScaAuthenticationData() == null) {
            return ValidationResult.invalid(resolveErrorType(authType), SERVICE_INVALID_400);
        }

        return ValidationResult.valid();
    }

    private ErrorType resolveErrorType(AuthorisationServiceType authType) {
        return authType == AIS ? ErrorType.AIS_400 : ErrorType.PIS_400;
    }
}
