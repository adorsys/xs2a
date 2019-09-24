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

package de.adorsys.psd2.xs2a.service.authorization.ais;

import de.adorsys.psd2.xs2a.domain.consent.AccountConsent;
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
     * @param accountConsent consent which is being authorised
     * @return whether single-factor authentication is enough for this consent
     */
    public boolean isOneFactorAuthorisation(AccountConsent accountConsent) {
        if (!accountConsent.isOneAccessType()) {
            return false;
        }

        if (accountConsent.isConsentForAllAvailableAccounts()) {
            return !aspspProfileServiceWrapper.isScaByOneTimeAvailableAccountsConsentRequired();
        }

        if (accountConsent.isGlobalConsent()) {
            return !aspspProfileServiceWrapper.isScaByOneTimeGlobalConsentRequired();
        }

        return false;
    }
}
