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

package de.adorsys.psd2.xs2a.service.validator.tpp;

import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public abstract class TppInfoValidator {
    private final TppInfoCheckerService tppInfoCheckerService;

    /**
     * Validates the TPP object contained in the consent/payment by checking whether it matches the current TPP in the request
     *
     * @param tppInfo TPP Info object contained in the consent/payment
     * @return valid result if the TPP is valid, invalid result with appropriate error otherwise
     */
    public ValidationResult validateTpp(@Nullable TppInfo tppInfo) {
        if (tppInfoCheckerService.differsFromTppInRequest(tppInfo)) {
            log.info("TPP validation has failed: TPP in consent/payment doesn't match the TPP in request");
            return ValidationResult.invalid(getErrorType(), getTppMessageInformation());
        }

        return ValidationResult.valid();
    }

    abstract ErrorType getErrorType();

    abstract TppMessageInformation getTppMessageInformation();
}
