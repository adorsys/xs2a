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

package de.adorsys.psd2.xs2a.web.validator.header;

import de.adorsys.psd2.xs2a.core.profile.PiisConsentSupported;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static de.adorsys.psd2.xs2a.web.validator.constants.Xs2aHeaderConstant.CONSENT_ID;

@Component
public class ConsentIdHeaderCofValidatorImpl extends AbstractHeaderValidatorImpl implements FundsConfirmationHeaderValidator {
    private final AspspProfileServiceWrapper profileService;

    @Autowired
    ConsentIdHeaderCofValidatorImpl(ErrorBuildingService errorBuildingService, AspspProfileServiceWrapper profileService) {
        super(errorBuildingService);
        this.profileService = profileService;
    }

    @Override
    protected String getHeaderName() {
        return CONSENT_ID;
    }

    @Override
    protected ValidationResult validate(Map<String, String> headers) {
        PiisConsentSupported piisConsentSupported = profileService.getPiisConsentSupported();
        if (piisConsentSupported == PiisConsentSupported.TPP_CONSENT_SUPPORTED) {
            return checkIfHeaderIsPresented(headers);
        }
        return ValidationResult.valid();
    }
}
