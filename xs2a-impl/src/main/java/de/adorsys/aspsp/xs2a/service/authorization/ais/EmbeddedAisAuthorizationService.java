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

import de.adorsys.aspsp.xs2a.domain.consent.*;
import de.adorsys.aspsp.xs2a.service.consent.AisConsentService;
import de.adorsys.aspsp.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import de.adorsys.aspsp.xs2a.spi.domain.SpiResponse;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountConsentAuthorization;
import de.adorsys.aspsp.xs2a.spi.domain.consent.AspspConsentData;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiScaStatus;
import de.adorsys.aspsp.xs2a.spi.domain.psu.SpiScaMethod;
import de.adorsys.aspsp.xs2a.spi.service.AccountSpi;
import de.adorsys.psd2.model.ScaStatus;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static de.adorsys.aspsp.xs2a.domain.consent.ConsentAuthorizationResponseLinkType.*;

@Service
@RequiredArgsConstructor
public class EmbeddedAisAuthorizationService implements AisAuthorizationService {
    private final AccountSpi accountSpi;
    private final AisConsentService aisConsentService;
    private final Xs2aAisConsentMapper aisConsentMapper;

    @Override
    public Optional<CreateConsentAuthorizationResponse> createConsentAuthorization(String psuId, String consentId) {
        return StringUtils.isBlank(psuId)
                   ? createConsentAuthorizationAndGetResponse(ScaStatus.RECEIVED, START_AUTHORISATION_WITH_PSU_IDENTIFICATION, consentId)
                   : createConsentAuthorizationAndGetResponse(ScaStatus.PSUAUTHENTICATED, START_AUTHORISATION_WITH_PSU_AUTHENTICATION, consentId);
    }

    @Override
    public AccountConsentAuthorization getAccountConsentAuthorizationById(String authorizationId, String consentId) {
        SpiAccountConsentAuthorization spiConsentAuthorization = aisConsentService.getAccountConsentAuthorizationById(authorizationId, consentId);
        return aisConsentMapper.mapToAccountConsentAuthorization(spiConsentAuthorization);
    }

    @Override
    public UpdateConsentPsuDataResponse updateConsentPsuData(UpdateConsentPsuDataReq updatePsuData, AccountConsentAuthorization consentAuthorization) {
        UpdateConsentPsuDataResponse response = createResponseFromUpdatePsuData(updatePsuData, consentAuthorization);
        aisConsentService.updateConsentAuthorization(aisConsentMapper.mapToSpiUpdateConsentPsuDataReq(response, updatePsuData));
        return response;
    }

    private UpdateConsentPsuDataResponse createResponseFromUpdatePsuData(UpdateConsentPsuDataReq request, AccountConsentAuthorization consentAuthorization) {
        UpdateConsentPsuDataResponse response = new UpdateConsentPsuDataResponse();

        if (checkPsuIdentification(request)) {
            response.setPsuId(request.getPsuId());
            response.setScaStatus(ScaStatus.PSUIDENTIFIED);
            response.setResponseLinkType(START_AUTHORISATION_WITH_PSU_AUTHENTICATION);
            return response;
        }

        if (checkPsuAuthentication(request, consentAuthorization)) {
            response.setPsuId(request.getPsuId());
            response.setPassword(request.getPassword());

            SpiResponse<List<SpiScaMethod>> spiResponse = "PSU_002".equals(request.getPsuId()) // TODO use `accountSpi.readAvailableScaMethods(updatePsuData.getPsuId(), updatePsuData.getPassword());` after #310 is merged
                                                              ? new SpiResponse<>(Collections.singletonList(SpiScaMethod.SMS_OTP), new AspspConsentData())
                                                              : new SpiResponse<>(Arrays.asList(SpiScaMethod.SMS_OTP, SpiScaMethod.PUSH_OTP), new AspspConsentData());

            List<SpiScaMethod> availableMethods = spiResponse.getPayload();

            if (CollectionUtils.isNotEmpty(availableMethods)) {
                if (isMultipleScaMethods(availableMethods)) {
                    response.setAvailableScaMethods(aisConsentMapper.mapToCmsScaMethods(availableMethods));
                    response.setScaStatus(ScaStatus.PSUAUTHENTICATED);
                    response.setResponseLinkType(START_AUTHORISATION_WITH_AUTHENTICATION_METHOD_SELECTION);
                } else {
                    response.setChosenScaMethod(availableMethods.get(0).name());
                    response.setScaStatus(ScaStatus.SCAMETHODSELECTED);
                    response.setResponseLinkType(START_AUTHORISATION_WITH_TRANSACTION_AUTHORISATION);
                }
            }

            return response;
        }

        if (checkScaMethod(request, consentAuthorization)) {
            response.setChosenScaMethod(request.getAuthenticationMethodId());
            response.setScaStatus(ScaStatus.SCAMETHODSELECTED);
            response.setResponseLinkType(START_AUTHORISATION_WITH_TRANSACTION_AUTHORISATION);
            return response;
        }

        if (checkScaAuthenticationData(request)) {
            response.setScaAuthenticationData(request.getScaAuthenticationData());
            response.setScaStatus(ScaStatus.FINALISED);
            response.setResponseLinkType(START_AUTHORISATION_WITH_AUTHENTICATION_METHOD_SELECTION);
            return response;
        }

        return response;
    }

    private Optional<CreateConsentAuthorizationResponse> createConsentAuthorizationAndGetResponse(ScaStatus scaStatus, ConsentAuthorizationResponseLinkType linkType, String consentId) {
        return aisConsentService.createAisConsentAuthorization(consentId, SpiScaStatus.valueOf(scaStatus.name()))
                   .map(authId -> {
                       CreateConsentAuthorizationResponse resp = new CreateConsentAuthorizationResponse();
                       resp.setConsentId(consentId);
                       resp.setAuthorizationId(authId);
                       resp.setScaStatus(scaStatus);
                       resp.setResponseLinkType(linkType);

                       return resp;
                   });
    }

    private boolean checkPsuIdentification(UpdateConsentPsuDataReq updatePsuData) {
        return updatePsuData.isUpdatePsuIdentification();
    }

    private boolean checkPsuAuthentication(UpdateConsentPsuDataReq updatePsuData, AccountConsentAuthorization spiAuthorization) {
        return spiAuthorization.getPassword() == null && updatePsuData.getPassword() != null;
    }

    private boolean checkScaMethod(UpdateConsentPsuDataReq updatePsuData, AccountConsentAuthorization spiAuthorization) {
        return spiAuthorization.getAuthenticationMethodId() == null && updatePsuData.getAuthenticationMethodId() != null;
    }

    private boolean checkScaAuthenticationData(UpdateConsentPsuDataReq updatePsuData) {
        return updatePsuData.getScaAuthenticationData() != null;
    }

    private boolean isMultipleScaMethods(List<SpiScaMethod> availableMethods) {
        return availableMethods.size() > 1;
    }
}
