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

package de.adorsys.psd2.xs2a.service.validator.tpp;

import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
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
