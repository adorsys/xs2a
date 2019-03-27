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

package de.adorsys.psd2.xs2a.service.validator.tpp;

import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.UNAUTHORIZED;

@Slf4j
@Component
@RequiredArgsConstructor
public class AisTppInfoValidator {
    private final TppInfoCheckerService tppInfoCheckerService;
    private final RequestProviderService requestProviderService;

    /**
     * Validates the TPP object contained in the consent by checking whether it matches the current TPP in the request
     *
     * @param tppInfoInConsent TPP Info object contained in the consent
     * @return valid result if the TPP is valid, invalid result with appropriate error otherwise
     */
    public ValidationResult validateTpp(@Nullable TppInfo tppInfoInConsent) {
        if (tppInfoCheckerService.differsFromTppInRequest(tppInfoInConsent)) {
            log.info("X-Request-ID: [{}]. TPP validation has failed: TPP in consent doesn't match the TPP in request",
                     requestProviderService.getRequestId());
            return ValidationResult.invalid(ErrorType.AIS_401, TppMessageInformation.of(UNAUTHORIZED, "TPP certificate doesn’t match the initial request"));
        }

        return ValidationResult.valid();
    }
}
