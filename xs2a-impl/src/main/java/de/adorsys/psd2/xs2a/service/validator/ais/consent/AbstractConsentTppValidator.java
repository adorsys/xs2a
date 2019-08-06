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

package de.adorsys.psd2.xs2a.service.validator.ais.consent;

import de.adorsys.psd2.xs2a.service.validator.TppInfoProvider;
import de.adorsys.psd2.xs2a.service.validator.ais.AbstractAisTppValidator;
import de.adorsys.psd2.xs2a.service.validator.tpp.AisConsentTppInfoValidator;
import de.adorsys.psd2.xs2a.service.validator.tpp.TppInfoValidator;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Common validator for validating TPP in consents for consents endpoints and executing request-specific business validation afterwards.
 * Should be used for all AIS requests related to consents, excluding consent creation request.
 *
 * @param <T> type of object to be checked
 */
public abstract class AbstractConsentTppValidator<T extends TppInfoProvider> extends AbstractAisTppValidator<T> {
    private AisConsentTppInfoValidator aisConsentTppInfoValidator;

    @Override
    protected @NotNull TppInfoValidator getTppInfoValidator() {
        return aisConsentTppInfoValidator;
    }

    @Autowired
    public void setAisConsentTppInfoValidator(AisConsentTppInfoValidator aisConsentTppInfoValidator) {
        this.aisConsentTppInfoValidator = aisConsentTppInfoValidator;
    }
}
