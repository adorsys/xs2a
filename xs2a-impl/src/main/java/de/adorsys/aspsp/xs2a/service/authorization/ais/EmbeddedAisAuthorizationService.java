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

import de.adorsys.aspsp.xs2a.domain.MessageErrorCode;
import de.adorsys.aspsp.xs2a.domain.consent.*;
import de.adorsys.aspsp.xs2a.service.consent.AisConsentDataService;
import de.adorsys.aspsp.xs2a.service.consent.AisConsentService;
import de.adorsys.aspsp.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import de.adorsys.aspsp.xs2a.service.mapper.spi_xs2a_mappers.SpiResponseStatusToXs2aMessageErrorCodeMapper;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorisationStatus;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorizationCodeResult;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaMethod;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiConsentStatus;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse.VoidResponse;
import de.adorsys.psd2.xs2a.spi.service.AisConsentSpi;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static de.adorsys.aspsp.xs2a.domain.consent.ConsentAuthorizationResponseLinkType.*;

@Service
@RequiredArgsConstructor
public class EmbeddedAisAuthorizationService implements AisAuthorizationService {
    private final AisConsentService aisConsentService;
    private final Xs2aAisConsentMapper aisConsentMapper;
    private final AisConsentDataService aisConsentDataService;
    private final AisConsentSpi aisConsentSpi;
    private final SpiResponseStatusToXs2aMessageErrorCodeMapper messageErrorCodeMapper;

    @Override
    public Optional<CreateConsentAuthorizationResponse> createConsentAuthorization(String psuId, String consentId) {
        return createConsentAuthorizationAndGetResponse(ScaStatus.STARTED, START_AUTHORISATION_WITH_PSU_AUTHENTICATION, consentId, psuId);
    }

    @Override
    public AccountConsentAuthorization getAccountConsentAuthorizationById(String authorizationId, String consentId) {
        return aisConsentService.getAccountConsentAuthorizationById(authorizationId, consentId);
    }

    // TODO cover with unit tests https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/334
    @Override
    public UpdateConsentPsuDataResponse updateConsentPsuData(UpdateConsentPsuDataReq request, AccountConsentAuthorization consentAuthorization) {
        UpdateConsentPsuDataResponse response = new UpdateConsentPsuDataResponse();

        // TODO refactor this the PIS way https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/426
        if (isPsuAuthenticationStage(consentAuthorization)) {
            response = proceedPsuAuthenticationStage(request);
        } else if (isScaMethodSelectionStage(consentAuthorization)) {
            response = proceedScaMethodSelectionStage(request);
        } else if (isAuthorizationStage(consentAuthorization)) {
            response = proceedAuthorizationStage(request);
        }

        if (!response.hasError()) {
            aisConsentService.updateConsentAuthorization(aisConsentMapper.mapToSpiUpdateConsentPsuDataReq(response, request));
        }

        return response;
    }

    private boolean isPsuAuthenticationStage(AccountConsentAuthorization consentAuthorization) {
        return consentAuthorization.getScaStatus() == ScaStatus.STARTED;
    }

    private boolean isScaMethodSelectionStage(AccountConsentAuthorization consentAuthorization) {
        return consentAuthorization.getScaStatus() == ScaStatus.PSUAUTHENTICATED;
    }

    private boolean isAuthorizationStage(AccountConsentAuthorization consentAuthorization) {
        return consentAuthorization.getScaStatus() == ScaStatus.SCAMETHODSELECTED;
    }

    private boolean isMultipleScaMethods(List<SpiScaMethod> availableMethods) {
        return availableMethods.size() > 1;
    }

    private UpdateConsentPsuDataResponse proceedPsuAuthenticationStage(UpdateConsentPsuDataReq request) {
        UpdateConsentPsuDataResponse response = new UpdateConsentPsuDataResponse();
        SpiAccountConsent accountConsent = aisConsentService.getAccountConsentById(request.getConsentId());

        SpiPsuData psuData = new SpiPsuData(request.getPsuId(), null, null, null);  // TODO get it from XS2A Interface https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/332

        SpiResponse<SpiAuthorisationStatus> authorisationStatusSpiResponse = aisConsentSpi.authorisePsu(psuData, request.getPassword(), accountConsent, aisConsentDataService.getAspspConsentDataByConsentId(request.getConsentId()));
        aisConsentDataService.updateAspspConsentData(authorisationStatusSpiResponse.getAspspConsentData());

        if (authorisationStatusSpiResponse.getPayload() == SpiAuthorisationStatus.FAILURE) {
            response.setScaStatus(ScaStatus.FAILED);
            return response;
        }

        response.setPsuId(request.getPsuId());
        response.setPassword(request.getPassword());

        SpiResponse<List<SpiScaMethod>> spiResponse = aisConsentSpi.requestAvailableScaMethods(psuData, accountConsent, aisConsentDataService.getAspspConsentDataByConsentId(request.getConsentId()));
        aisConsentDataService.updateAspspConsentData(authorisationStatusSpiResponse.getAspspConsentData());

        List<SpiScaMethod> availableScaMethods = spiResponse.getPayload();

        if (CollectionUtils.isNotEmpty(availableScaMethods)) {
            if (isMultipleScaMethods(availableScaMethods)) {
                proceedResponseForMultipleAvailableMethods(response, availableScaMethods);
            } else {
                proceedResponseForOneAvailableMethod(response, accountConsent, availableScaMethods, request.getConsentId());
            }
        } else {
            proceedResponseForNoneAvailableScaMethod(response, request.getConsentId());
        }

        return response;
    }

    private UpdateConsentPsuDataResponse proceedScaMethodSelectionStage(UpdateConsentPsuDataReq request) {
        UpdateConsentPsuDataResponse response = new UpdateConsentPsuDataResponse();
        SpiAccountConsent accountConsent = aisConsentService.getAccountConsentById(request.getConsentId());
        String authenticationMethodId = request.getAuthenticationMethodId();
        SpiPsuData psuData = new SpiPsuData(request.getPsuId(), null, null, null);  // TODO get it from XS2A Interface https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/332

        SpiResponse<SpiAuthorizationCodeResult> spiResponse = aisConsentSpi.requestAuthorisationCode(psuData, SpiScaMethod.valueOf(authenticationMethodId), accountConsent, aisConsentDataService.getAspspConsentDataByConsentId(request.getConsentId()));
        aisConsentDataService.updateAspspConsentData(spiResponse.getAspspConsentData());

        if (spiResponse.hasError()) {
            response.setErrorCode(messageErrorCodeMapper.mapToMessageErrorCode(spiResponse.getResponseStatus()));
            return response;
        }

        response.setChosenScaMethod(authenticationMethodId);
        response.setScaStatus(ScaStatus.SCAMETHODSELECTED);
        response.setResponseLinkType(START_AUTHORISATION_WITH_TRANSACTION_AUTHORISATION);
        return response;
    }

    private UpdateConsentPsuDataResponse proceedAuthorizationStage(UpdateConsentPsuDataReq request) {
        UpdateConsentPsuDataResponse response = new UpdateConsentPsuDataResponse();
        SpiAccountConsent accountConsent = aisConsentService.getAccountConsentById(request.getConsentId());

        SpiPsuData psuData = new SpiPsuData(null, null, null, null);    // TODO get it from XS2A Interface https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/332

        SpiResponse<VoidResponse> spiResponse = aisConsentSpi.verifyAuthorisationCodeAndExecuteRequest(psuData, aisConsentMapper.mapToSpiScaConfirmation(request), accountConsent, aisConsentDataService.getAspspConsentDataByConsentId(request.getConsentId()));
        aisConsentDataService.updateAspspConsentData(spiResponse.getAspspConsentData());

        if (spiResponse.hasError()) {
            response.setErrorCode(messageErrorCodeMapper.mapToMessageErrorCode(spiResponse.getResponseStatus()));
            return response;
        }

        response.setScaAuthenticationData(request.getScaAuthenticationData());
        response.setScaStatus(ScaStatus.FINALISED);
        response.setResponseLinkType(START_AUTHORISATION_WITH_AUTHENTICATION_METHOD_SELECTION);
        aisConsentService.updateConsentStatus(request.getConsentId(), SpiConsentStatus.VALID);
        return response;
    }

    private void proceedResponseForMultipleAvailableMethods(UpdateConsentPsuDataResponse response, List<SpiScaMethod> availableScaMethods) {
        response.setAvailableScaMethods(aisConsentMapper.mapToCmsScaMethods(availableScaMethods));
        response.setScaStatus(ScaStatus.PSUAUTHENTICATED);
        response.setResponseLinkType(START_AUTHORISATION_WITH_AUTHENTICATION_METHOD_SELECTION);
    }

    private void proceedResponseForOneAvailableMethod(UpdateConsentPsuDataResponse response, SpiAccountConsent accountConsent, List<SpiScaMethod> availableScaMethods, String consentId) {
        SpiPsuData psuData = new SpiPsuData(response.getPsuId(), null, null, null); // TODO get it from XS2A Interface https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/332

        SpiResponse<SpiAuthorizationCodeResult> spiResponse = aisConsentSpi.requestAuthorisationCode(psuData, availableScaMethods.get(0), accountConsent, aisConsentDataService.getAspspConsentDataByConsentId(consentId));
        aisConsentDataService.updateAspspConsentData(spiResponse.getAspspConsentData());

        if (spiResponse.hasError()) {
            response.setErrorCode(messageErrorCodeMapper.mapToMessageErrorCode(spiResponse.getResponseStatus()));
            return;
        }

        response.setChosenScaMethod(availableScaMethods.get(0).name());
        response.setScaStatus(ScaStatus.SCAMETHODSELECTED);
        response.setResponseLinkType(START_AUTHORISATION_WITH_TRANSACTION_AUTHORISATION);
    }

    private void proceedResponseForNoneAvailableScaMethod(UpdateConsentPsuDataResponse response, String consentId) {
        response.setScaStatus(ScaStatus.FAILED);
        response.setErrorCode(MessageErrorCode.SCA_METHOD_UNKNOWN);
        aisConsentService.updateConsentStatus(consentId, SpiConsentStatus.REJECTED);
    }

    private Optional<CreateConsentAuthorizationResponse> createConsentAuthorizationAndGetResponse(ScaStatus scaStatus, ConsentAuthorizationResponseLinkType linkType, String consentId, String psuId) {
        return aisConsentService.createAisConsentAuthorization(consentId, scaStatus, psuId)
                   .map(authId -> {
                       CreateConsentAuthorizationResponse resp = new CreateConsentAuthorizationResponse();

                       resp.setConsentId(consentId);
                       resp.setAuthorizationId(authId);
                       resp.setScaStatus(scaStatus);
                       resp.setResponseLinkType(linkType);

                       return resp;
                   });
    }
}
