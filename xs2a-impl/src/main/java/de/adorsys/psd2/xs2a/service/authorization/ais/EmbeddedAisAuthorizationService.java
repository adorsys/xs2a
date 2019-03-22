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
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.*;
import de.adorsys.psd2.xs2a.service.authorization.ais.stage.AisScaStage;
import de.adorsys.psd2.xs2a.service.authorization.ais.stage.embedded.AisScaMethodSelectedStage;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static de.adorsys.psd2.xs2a.config.factory.AisScaStageAuthorisationFactory.SEPARATOR;
import static de.adorsys.psd2.xs2a.config.factory.AisScaStageAuthorisationFactory.SERVICE_PREFIX;
import static de.adorsys.psd2.xs2a.domain.consent.ConsentAuthorizationResponseLinkType.START_AUTHORISATION_WITH_PSU_AUTHENTICATION;
import static de.adorsys.psd2.xs2a.domain.consent.ConsentAuthorizationResponseLinkType.START_AUTHORISATION_WITH_PSU_IDENTIFICATION;

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
        Optional<AccountConsent> accountConsentOptional = aisConsentService.getAccountConsentById(consentId);
        if (!accountConsentOptional.isPresent()) {
            return Optional.empty();
        }

        return aisConsentService.createAisConsentAuthorization(consentId, ScaStatus.STARTED, psuData)
                   .map(authId -> {
                       CreateConsentAuthorizationResponse resp = new CreateConsentAuthorizationResponse();

                       resp.setConsentId(consentId);
                       resp.setAuthorizationId(authId);
                       resp.setScaStatus(ScaStatus.STARTED);
                       resp.setResponseLinkType(getResponseLinkType(psuData, psuData));

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
    public Optional<AccountConsentAuthorization> getAccountConsentAuthorizationById(String authorizationId, String consentId) {
        return aisConsentService.getAccountConsentAuthorizationById(authorizationId, consentId);
    }

    /**
     * Updates consent PSU data.
     * {@link AisScaStageAuthorisationFactory} is used there to provide the actual service for current stage.
     * Service returns UpdateConsentPsuDataResponse on invoking its apply() method
     * (e.g. see {@link AisScaMethodSelectedStage#apply}).
     * If response has no errors, consent authorisation is updated by invoking CMS through AisConsentService
     * See {@link Xs2aAisConsentService#updateConsentAuthorization(UpdateConsentPsuDataReq)} for details.
     *
     * @param request              UpdateConsentPsuDataReq request to update PSU data
     * @param consentAuthorization AccountConsentAuthorization instance with authorisation data
     * @return UpdateConsentPsuDataResponse update consent PSU data response
     */
    @Override
    public UpdateConsentPsuDataResponse updateConsentPsuData(UpdateConsentPsuDataReq request, AccountConsentAuthorization consentAuthorization) {
        AisScaStage<UpdateConsentPsuDataReq, UpdateConsentPsuDataResponse> service = scaStageAuthorisationFactory.getService(SERVICE_PREFIX + SEPARATOR + consentAuthorization.getScaStatus().name());
        UpdateConsentPsuDataResponse response = service.apply(request);

        if (!response.hasError()) {
            aisConsentService.updateConsentAuthorization(aisConsentMapper.mapToSpiUpdateConsentPsuDataReq(response, request));
        }

        return response;
    }

    /**
     * Gets list of consent authorisation IDs by invoking CMS through AisConsentService
     * See {@link Xs2aAisConsentService#getAuthorisationSubResources(String)} for details
     *
     * @param consentId String identification of consent
     * @return Optional of Xs2aAuthorisationSubResources with list of authorisation IDs
     */
    @Override
    public Optional<Xs2aAuthorisationSubResources> getAuthorisationSubResources(String consentId) {
        return aisConsentService.getAuthorisationSubResources(consentId)
                   .map(Xs2aAuthorisationSubResources::new);
    }

    /**
     * Gets SCA status of the authorisation from CMS
     *
     * @param consentId       String representation of consent identifier
     * @param authorisationId String representation of authorisation identifier
     * @return SCA status of the authorisation
     */
    @Override
    public Optional<ScaStatus> getAuthorisationScaStatus(String consentId, String authorisationId) {
        return aisConsentService.getAuthorisationScaStatus(consentId, authorisationId);
    }

    @Override
    public ScaApproach getScaApproachServiceType() {
        return ScaApproach.EMBEDDED;
    }

    private ConsentAuthorizationResponseLinkType getResponseLinkType(PsuIdData psuIdDataConsent, PsuIdData psuIdDataAuthorisation) {
        return isPsuExist(psuIdDataConsent) || isPsuExist(psuIdDataAuthorisation)
                   ? START_AUTHORISATION_WITH_PSU_AUTHENTICATION
                   : START_AUTHORISATION_WITH_PSU_IDENTIFICATION;
    }

    private boolean isPsuExist(PsuIdData psuIdData) {
        return Optional.ofNullable(psuIdData)
                   .map(PsuIdData::isNotEmpty)
                   .orElse(false);
    }
}
