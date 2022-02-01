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

package de.adorsys.psd2.xs2a.service.validator.piis;

import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.domain.fund.CreatePiisConsentRequest;
import de.adorsys.psd2.xs2a.service.validator.BusinessValidator;
import de.adorsys.psd2.xs2a.service.validator.PsuDataInInitialRequestValidator;
import de.adorsys.psd2.xs2a.service.validator.SupportedAccountReferenceValidator;
import de.adorsys.psd2.xs2a.service.validator.piis.dto.CreatePiisConsentRequestObject;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * Validator to be used for validating create piis consent request according to some business rules
 */
@Component
@AllArgsConstructor
public class CreatePiisConsentValidator implements BusinessValidator<CreatePiisConsentRequestObject> {
    private final PsuDataInInitialRequestValidator psuDataInInitialRequestValidator;
    private final SupportedAccountReferenceValidator supportedAccountReferenceValidator;

    @Override
    public @NotNull ValidationResult validate(@NotNull CreatePiisConsentRequestObject requestObject) {
        ValidationResult psuDataValidationResult = psuDataInInitialRequestValidator.validate(requestObject.getPsuIdData());
        if (psuDataValidationResult.isNotValid()) {
            return psuDataValidationResult;
        }

        CreatePiisConsentRequest request = requestObject.getCreatePiisConsentRequest();
        ValidationResult supportedAccountReferenceValidationResult = supportedAccountReferenceValidator.validate(Collections.singletonList(request.getAccount()));
        if (supportedAccountReferenceValidationResult.isNotValid()) {
            return supportedAccountReferenceValidationResult;
        }

        return ValidationResult.valid();
    }
}
