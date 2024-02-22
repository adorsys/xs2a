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

package de.adorsys.psd2.xs2a.service.authorization.ais;

import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AisScaAuthorisationService {
    private final AspspProfileServiceWrapper aspspProfileServiceWrapper;

    /**
     * Checks whether authorisation resource of this consent can be authenticated using only single factor
     *
     * @param aisConsent consent which is being authorised
     * @return whether single-factor authentication is enough for this consent
     */
    public boolean isOneFactorAuthorisation(AisConsent aisConsent) {
        if (!aisConsent.isOneAccessType()) {
            return false;
        }

        if (aisConsent.isConsentForAllAvailableAccounts()) {
            return !aspspProfileServiceWrapper.isScaByOneTimeAvailableAccountsConsentRequired();
        }

        if (aisConsent.isGlobalConsent()) {
            return !aspspProfileServiceWrapper.isScaByOneTimeGlobalConsentRequired();
        }

        return false;
    }
}
