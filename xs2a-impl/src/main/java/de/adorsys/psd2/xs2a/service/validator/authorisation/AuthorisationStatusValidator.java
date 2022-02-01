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
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AuthorisationStatusValidator {
    private final AspspProfileServiceWrapper aspspProfileService;

    @NotNull
    public ValidationResult validate(@NotNull ScaStatus scaStatus, @NotNull boolean confirmationCodeReceived) {
        if (scaStatus != ScaStatus.FAILED) {
            return ValidationResult.valid();
        }

        if (aspspProfileService.isAuthorisationConfirmationRequestMandated() && confirmationCodeReceived) {
            return ValidationResult.invalid(getErrorTypeForSCAInvalid(), MessageErrorCode.SCA_INVALID);
        }

        log.info("Authorisation has failed status");
        return ValidationResult.invalid(getErrorTypeForStatusInvalid(), MessageErrorCode.STATUS_INVALID);
    }

    @NotNull
    protected abstract ErrorType getErrorTypeForStatusInvalid();

    @NotNull
    protected abstract ErrorType getErrorTypeForSCAInvalid();
}
