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
