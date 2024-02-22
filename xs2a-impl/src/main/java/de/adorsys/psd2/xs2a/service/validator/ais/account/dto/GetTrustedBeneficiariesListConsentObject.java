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

package de.adorsys.psd2.xs2a.service.validator.ais.account.dto;

import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.service.validator.TppInfoProvider;
import de.adorsys.psd2.xs2a.service.validator.ais.account.GetTrustedBeneficiariesListValidator;
import lombok.Value;

/**
 * Consent object that contains necessary information for validating consent in {@link GetTrustedBeneficiariesListValidator}
 */
@Value
public class GetTrustedBeneficiariesListConsentObject implements TppInfoProvider {
    private AisConsent aisConsent;
    private String accountId;
    private String requestUri;

    @Override
    public TppInfo getTppInfo() {
        return aisConsent.getTppInfo();
    }
}
