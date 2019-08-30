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

import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AuthorisationStatusValidator {
    private final RequestProviderService requestProviderService;

    @NotNull
    public ValidationResult validate(@NotNull ScaStatus scaStatus) {
        if (scaStatus == ScaStatus.FAILED) {
            log.info("InR-ID: [{}], X-Request-ID: [{}]. Authorisation has failed status",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId());
            return ValidationResult.invalid(getErrorType(), MessageErrorCode.STATUS_INVALID);
        }

        return ValidationResult.valid();
    }

    @NotNull
    protected abstract ErrorType getErrorType();
}
