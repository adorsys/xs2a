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

package de.adorsys.psd2.xs2a.service.authorization.ais;

import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationRequest;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.authorisation.CommonAuthorisationParameters;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.authorization.Xs2aAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorResponse;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aConsentService;
import de.adorsys.psd2.xs2a.service.mapper.ConsentPsuDataMapper;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aConsentAuthorisationMapper;
import org.springframework.stereotype.Service;

/**
 * AisAuthorizationService implementation to be used in case of redirect approach
 */
@Service
public class RedirectAisAuthorizationService extends AbstractAisAuthorizationService {
    private final RequestProviderService requestProviderService;

    public RedirectAisAuthorizationService(Xs2aConsentService consentService,
                                           Xs2aAuthorisationService authorisationService,
                                           ConsentPsuDataMapper consentPsuDataMapper,
                                           Xs2aAisConsentService aisConsentService,
                                           Xs2aConsentAuthorisationMapper xs2aConsentAuthorisationMapper, RequestProviderService requestProviderService) {
        super(consentService, authorisationService, consentPsuDataMapper, aisConsentService, xs2aConsentAuthorisationMapper);
        this.requestProviderService = requestProviderService;
    }

    @Override
    public AuthorisationProcessorResponse updateConsentPsuData(CommonAuthorisationParameters request, AuthorisationProcessorResponse response) {
        return null;
    }

    @Override
    public ScaApproach getScaApproachServiceType() {
        return ScaApproach.REDIRECT;
    }

    @Override
    protected CreateAuthorisationRequest createAuthorisationRequest(String authorisationId, ScaStatus scaStatus, PsuIdData psuData, ScaApproach scaApproach) {
        String tppRedirectURI = requestProviderService.getTppRedirectURI();
        String tppNOKRedirectURI = requestProviderService.getTppNokRedirectURI();
        return xs2aConsentAuthorisationMapper.mapToAuthorisationRequest(authorisationId, scaStatus, psuData, scaApproach,
                                                                        tppRedirectURI, tppNOKRedirectURI);
    }
}
