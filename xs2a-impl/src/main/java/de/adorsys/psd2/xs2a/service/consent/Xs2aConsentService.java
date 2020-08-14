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

package de.adorsys.psd2.xs2a.service.consent;

import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationRequest;
import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationResponse;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.authorization.Xs2aAuthorisationService;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aConsentAuthorisationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class Xs2aConsentService {
    private final RequestProviderService requestProviderService;
    private final Xs2aConsentAuthorisationMapper xs2aConsentAuthorisationMapper;
    private final Xs2aAuthorisationService authorisationService;
    private final ScaApproachResolver scaApproachResolver;

    /**
     * Sends a POST request to CMS to store created consent authorisation
     *
     * @param consentId String representation of identifier of stored consent
     * @param scaStatus Enum for status of the SCA method applied
     * @param psuData   authorisation data about PSU
     * @return CreateAuthorisationResponse object with authorisation ID and scaStatus
     */
    public Optional<CreateAuthorisationResponse> createConsentAuthorisation(String consentId, ScaStatus scaStatus, PsuIdData psuData) {
        String tppRedirectURI = requestProviderService.getTppRedirectURI();
        String tppNOKRedirectURI = requestProviderService.getTppNokRedirectURI();
        CreateAuthorisationRequest request = xs2aConsentAuthorisationMapper.mapToAuthorisationRequest(scaStatus, psuData, scaApproachResolver.resolveScaApproach(), tppRedirectURI, tppNOKRedirectURI);
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
