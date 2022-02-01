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
