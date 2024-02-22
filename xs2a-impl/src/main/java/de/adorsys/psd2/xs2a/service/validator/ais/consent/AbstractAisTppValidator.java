/*
 * Copyright 2018-2024 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.xs2a.service.validator.ais.consent;

import de.adorsys.psd2.xs2a.service.validator.TppInfoProvider;
import de.adorsys.psd2.xs2a.service.validator.ais.AbstractConsentTppValidator;
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
public abstract class AbstractAisTppValidator<T extends TppInfoProvider> extends AbstractConsentTppValidator<T> {
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
