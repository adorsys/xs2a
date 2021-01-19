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
