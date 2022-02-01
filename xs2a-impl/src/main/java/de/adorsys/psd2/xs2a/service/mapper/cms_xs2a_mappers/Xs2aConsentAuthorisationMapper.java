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

package de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers;

import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationRequest;
import de.adorsys.psd2.consent.api.authorisation.UpdateAuthorisationRequest;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.ConsentAuthorisationsParameters;
import de.adorsys.psd2.xs2a.web.mapper.TppRedirectUriMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class Xs2aConsentAuthorisationMapper {
    private final TppRedirectUriMapper tppRedirectUriMapper;

    public CreateAuthorisationRequest mapToAuthorisationRequest(String authorisationId, ScaStatus scaStatus,
                                                                PsuIdData psuData, ScaApproach scaApproach, String tppRedirectURI,
                                                                String tppNOKRedirectURI) {
        return Optional.ofNullable(scaStatus)
                   .map(st -> {
                       CreateAuthorisationRequest consentAuthorization = new CreateAuthorisationRequest();
                       consentAuthorization.setScaStatus(scaStatus);
                       consentAuthorization.setAuthorisationId(authorisationId);
                       consentAuthorization.setPsuData(psuData);
                       consentAuthorization.setScaApproach(scaApproach);
                       consentAuthorization.setTppRedirectURIs(tppRedirectUriMapper.mapToTppRedirectUri(tppRedirectURI,tppNOKRedirectURI));
                       return consentAuthorization;
                   })
                   .orElse(null);
    }

    public CreateAuthorisationRequest mapToAuthorisationRequest(String authorisationId, ScaStatus scaStatus,
                                                                PsuIdData psuData, ScaApproach scaApproach) {
        return mapToAuthorisationRequest(authorisationId, scaStatus, psuData, scaApproach, null, null);
    }

    public UpdateAuthorisationRequest mapToAuthorisationRequest(ConsentAuthorisationsParameters updatePsuData) {
        return Optional.ofNullable(updatePsuData)
                   .map(data -> {
                       UpdateAuthorisationRequest consentAuthorization = new UpdateAuthorisationRequest();
                       consentAuthorization.setPsuData(data.getPsuData());
                       consentAuthorization.setScaStatus(data.getScaStatus());
                       consentAuthorization.setAuthenticationMethodId(data.getAuthenticationMethodId());
                       consentAuthorization.setPassword(data.getPassword());
                       consentAuthorization.setScaAuthenticationData(data.getScaAuthenticationData());
                       consentAuthorization.setAuthorisationType(AuthorisationType.CONSENT);

                       return consentAuthorization;
                   })
                   .orElse(null);
    }
}
