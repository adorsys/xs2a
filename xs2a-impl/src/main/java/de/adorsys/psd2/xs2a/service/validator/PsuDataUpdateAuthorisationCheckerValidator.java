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

package de.adorsys.psd2.xs2a.service.validator;

import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Slf4j
@RequiredArgsConstructor
public abstract class PsuDataUpdateAuthorisationCheckerValidator {
    private final RequestProviderService requestProviderService;
    private final PsuDataUpdateAuthorisationChecker psuDataUpdateAuthorisationChecker;

    @NotNull
    public ValidationResult validate(@NotNull PsuIdData psuIdDataRequest, @Nullable PsuIdData psuIdDataAuthorisation) {
        if (psuDataUpdateAuthorisationChecker.areBothPsusAbsent(psuIdDataRequest, psuIdDataAuthorisation)) {
            log.info("PsuID-Request: [{}], PsuID-Authorisation: [{}]. Updating PSU Data has failed: PSU from request and PSU from authorisation are absent",
                     psuIdDataRequest, psuIdDataAuthorisation);
            return ValidationResult.invalid(getMessageErrorAreBothPsusAbsent());
        }

        if (!psuDataUpdateAuthorisationChecker.canPsuUpdateAuthorisation(psuIdDataRequest, psuIdDataAuthorisation)) {
            log.info("PsuID-Request: [{}], PsuID-Authorisation: [{}]. Updating PSU Data has failed: PSU from authorisation and PSU from request are different",
                     psuIdDataRequest, psuIdDataAuthorisation);
            return ValidationResult.invalid(getMessageErrorCanPsuUpdateAuthorisation());
        }

        return ValidationResult.valid();
    }

    public abstract MessageError getMessageErrorAreBothPsusAbsent();

    public abstract MessageError getMessageErrorCanPsuUpdateAuthorisation();
}
