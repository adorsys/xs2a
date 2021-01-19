/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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
