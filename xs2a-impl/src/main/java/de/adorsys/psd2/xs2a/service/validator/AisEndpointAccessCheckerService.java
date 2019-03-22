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

package de.adorsys.psd2.xs2a.service.validator;

import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AisEndpointAccessCheckerService extends EndpointAccessChecker {
    private final Xs2aAisConsentService aisConsentService;

    /**
     * Checks whether endpoint is accessible for current authorisation
     *
     * @param authorisationId ID of authorisation process
     * @param consentId       ID of consent
     * @return <code>true</code> if accessible. <code>false</code> otherwise.
     */
    public boolean isEndpointAccessible(String authorisationId, String consentId) {
        return aisConsentService.getAccountConsentAuthorizationById(authorisationId, consentId)
                   .map(a -> isAccessible(a.getChosenScaApproach(), a.getScaStatus()))
                   .orElse(true);
    }
}
