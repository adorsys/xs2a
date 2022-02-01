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

package de.adorsys.psd2.xs2a.service.validator.authorisation;

import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationServiceType;
import de.adorsys.psd2.xs2a.domain.authorisation.CommonAuthorisationParameters;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.SERVICE_INVALID_400;
import static de.adorsys.psd2.xs2a.core.sca.ScaStatus.*;

/**
 * Checks whether the incoming request authorisation data matches the current authorisation stage
 */
@Component
public class AuthorisationStageCheckValidator {

    public ValidationResult validate(@NotNull CommonAuthorisationParameters updateRequest, @NotNull ScaStatus scaStatus, @NotNull AuthorisationServiceType authType) {
        if (scaStatus == RECEIVED && updateRequest.getPsuData().isEmpty()) {
            return ValidationResult.invalid(resolveErrorType(authType), SERVICE_INVALID_400);
        }

        if (scaStatus == PSUIDENTIFIED && updateRequest.getPassword() == null) {
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
        switch (authType) {
            case AIS:
                return ErrorType.AIS_400;
            case PIIS:
                return ErrorType.PIIS_400;
            default:
                return ErrorType.PIS_400;
        }
    }
}
