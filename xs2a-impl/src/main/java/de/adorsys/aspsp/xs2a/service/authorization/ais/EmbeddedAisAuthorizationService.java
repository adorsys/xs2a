/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package de.adorsys.aspsp.xs2a.service.authorization.ais;

import de.adorsys.aspsp.xs2a.config.factory.AisScaStageAuthorisationFactory;
import de.adorsys.aspsp.xs2a.domain.consent.*;
import de.adorsys.aspsp.xs2a.service.authorization.ais.stage.AisScaStage;
import de.adorsys.aspsp.xs2a.service.consent.AisConsentService;
import de.adorsys.aspsp.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import de.adorsys.psd2.model.ScaStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static de.adorsys.aspsp.xs2a.config.factory.AisScaStageAuthorisationFactory.SERVICE_PREFIX;
import static de.adorsys.aspsp.xs2a.domain.consent.ConsentAuthorizationResponseLinkType.START_AUTHORISATION_WITH_PSU_AUTHENTICATION;

@Service
@RequiredArgsConstructor
public class EmbeddedAisAuthorizationService implements AisAuthorizationService {
    private final AisConsentService aisConsentService;
    private final Xs2aAisConsentMapper aisConsentMapper;
    private final AisScaStageAuthorisationFactory scaStageAuthorisationFactory;

    @Override
    public Optional<CreateConsentAuthorizationResponse> createConsentAuthorization(String psuId, String consentId) {
        return aisConsentService.createAisConsentAuthorization(consentId, Xs2aScaStatus.valueOf(ScaStatus.STARTED.name()), psuId)
                   .map(authId -> {
                       CreateConsentAuthorizationResponse resp = new CreateConsentAuthorizationResponse();

                       resp.setConsentId(consentId);
                       resp.setAuthorizationId(authId);
                       resp.setScaStatus(ScaStatus.STARTED);
                       resp.setResponseLinkType(START_AUTHORISATION_WITH_PSU_AUTHENTICATION);

                       return resp;
                   });
    }

    @Override
    public AccountConsentAuthorization getAccountConsentAuthorizationById(String authorizationId, String consentId) {
        return aisConsentService.getAccountConsentAuthorizationById(authorizationId, consentId);
    }

    @Override
    public UpdateConsentPsuDataResponse updateConsentPsuData(UpdateConsentPsuDataReq request, AccountConsentAuthorization consentAuthorization) {
        AisScaStage<UpdateConsentPsuDataReq, UpdateConsentPsuDataResponse> service = scaStageAuthorisationFactory.getService(SERVICE_PREFIX + consentAuthorization.getScaStatus().name());
        UpdateConsentPsuDataResponse response = service.apply(request);

        if (!response.hasError()) {
            aisConsentService.updateConsentAuthorization(aisConsentMapper.mapToSpiUpdateConsentPsuDataReq(response, request));
        }

        return response;
    }
}
