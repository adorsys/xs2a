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

package de.adorsys.psd2.xs2a.service.consent;

import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationRequest;
import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationResponse;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.service.authorization.Xs2aAuthorisationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class Xs2aConsentService {
    private final Xs2aAuthorisationService authorisationService;

    /**
     * Sends a POST request to CMS to store created consent authorisation
     *
     * @param consentId       String representation of identifier of stored consent
     * @param request         Object representation of all data needed for authorisation creation
     * @return CreateAuthorisationResponse object with authorisation ID and scaStatus
     */
    public Optional<CreateAuthorisationResponse> createConsentAuthorisation(String consentId, CreateAuthorisationRequest request) {
        return authorisationService.createAuthorisation(request, consentId, AuthorisationType.CONSENT);
    }

    /**
     * Requests CMS to retrieve SCA status of consent authorisation
     *
     * @param consentId       String representation of consent identifier
     * @param authorisationId String representation of authorisation identifier
     * @return SCA status of the authorisation
     */
    public Optional<ScaStatus> getAuthorisationScaStatus(String consentId, String authorisationId) {
        return authorisationService.getAuthorisationScaStatus(authorisationId, consentId, AuthorisationType.CONSENT);
    }
}
