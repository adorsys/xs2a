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
