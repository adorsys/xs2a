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

package de.adorsys.psd2.xs2a.service.validator.piis;

import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.validator.authorisation.AuthorisationStatusValidator;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class PiisAuthorisationStatusValidator extends AuthorisationStatusValidator {
    public PiisAuthorisationStatusValidator(AspspProfileServiceWrapper aspspProfileService) {
        super(aspspProfileService);
    }

    @Override
    protected @NotNull ErrorType getErrorTypeForStatusInvalid() {
        return ErrorType.PIIS_409;
    }

    @Override
    protected @NotNull ErrorType getErrorTypeForSCAInvalid() {
        return ErrorType.PIIS_400;
    }
}
