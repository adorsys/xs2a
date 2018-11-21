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

package de.adorsys.psd2.xs2a.service.authorization.ais;

import de.adorsys.psd2.xs2a.config.factory.AisScaStageAuthorisationFactory;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsentAuthorization;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentAuthorizationResponse;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataReq;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.authorization.ais.stage.AisScaStage;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static de.adorsys.psd2.xs2a.config.factory.AisScaStageAuthorisationFactory.SERVICE_PREFIX;
import static de.adorsys.psd2.xs2a.domain.consent.ConsentAuthorizationResponseLinkType.START_AUTHORISATION_WITH_PSU_AUTHENTICATION;

/**
 * AisAuthorizationService implementation to be used in case of embedded approach
 */
@Service
@RequiredArgsConstructor
public class EmbeddedAisAuthorizationService implements AisAuthorizationService {
    private final Xs2aAisConsentService aisConsentService;
    private final Xs2aAisConsentMapper aisConsentMapper;
    private final AisScaStageAuthorisationFactory scaStageAuthorisationFactory;

    /**
     * Creates consent authorisation using provided psu id and consent id by invoking CMS through AisConsentService
     * See {@link Xs2aAisConsentService#createAisConsentAuthorization(String, ScaStatus, PsuIdData)} for details
     *
     * @param psuData   PsuIdData container of authorisation data about PSU
     * @param consentId String identification of consent
     * @return Optional of CreateConsentAuthorizationResponse with consent creating data
     */
    @Override
    public Optional<CreateConsentAuthorizationResponse> createConsentAuthorization(PsuIdData psuData, String consentId) {
        return aisConsentService.createAisConsentAuthorization(consentId, ScaStatus.valueOf(ScaStatus.STARTED.name()), psuData)
                   .map(authId -> {
                       CreateConsentAuthorizationResponse resp = new CreateConsentAuthorizationResponse();

                       resp.setConsentId(consentId);
                       resp.setAuthorizationId(authId);
                       resp.setScaStatus(ScaStatus.STARTED);
                       resp.setResponseLinkType(START_AUTHORISATION_WITH_PSU_AUTHENTICATION);

                       return resp;
                   });
    }

    /**
     * Gets AccountConsentAuthorization using provided authorization id and consent id by invoking CMS through AisConsentService.
     * See {@link Xs2aAisConsentService#getAccountConsentAuthorizationById(String, String)} for details
     *
     * @param authorizationId String identification of AccountConsentAuthorization
     * @param consentId       String identification of consent
     * @return AccountConsentAuthorization instance
     */
    @Override
    public AccountConsentAuthorization getAccountConsentAuthorizationById(String authorizationId, String consentId) {
        return aisConsentService.getAccountConsentAuthorizationById(authorizationId, consentId);
    }

    /**
     * Updates consent PSU data.
     * {@link AisScaStageAuthorisationFactory} is used there to provide the actual service for current stage.
     * Service returns UpdateConsentPsuDataResponse on invoking its apply() method
     * (e.g. see {@link de.adorsys.psd2.xs2a.service.authorization.ais.stage.AisScaMethodSelectedStage#apply}).
     * If response has no errors, consent authorisation is updated by invoking CMS through AisConsentService
     * See {@link Xs2aAisConsentService#updateConsentAuthorization(UpdateConsentPsuDataReq)} for details.
     *
     * @param request UpdateConsentPsuDataReq request to update PSU data
     * @param consentAuthorization AccountConsentAuthorization instance with authorisation data
     * @return UpdateConsentPsuDataResponse update consent PSU data response
     */
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
