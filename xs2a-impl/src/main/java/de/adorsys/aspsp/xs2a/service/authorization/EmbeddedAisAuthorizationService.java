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

package de.adorsys.aspsp.xs2a.service.authorization;

import de.adorsys.aspsp.xs2a.domain.consent.ConsentAuthorizationResponseLinkType;
import de.adorsys.aspsp.xs2a.domain.consent.CreateConsentAuthorizationResponse;
import de.adorsys.aspsp.xs2a.domain.consent.UpdateConsentPsuDataReq;
import de.adorsys.aspsp.xs2a.domain.consent.UpdateConsentPsuDataResponse;
import de.adorsys.aspsp.xs2a.spi.domain.SpiResponse;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountConsentAuthorization;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiScaMethod;
import de.adorsys.aspsp.xs2a.spi.service.AccountSpi;
import de.adorsys.psd2.model.ScaStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmbeddedAisAuthorizationService implements AisAuthorizationService {

    private final AccountSpi accountSpi;

    @Override
    public CreateConsentAuthorizationResponse createConsentAuthorization(String psuId, String consentId) {
        CreateConsentAuthorizationResponse response = new CreateConsentAuthorizationResponse();

        return Optional.ofNullable(psuId)
            .map(s -> {
                response.setScaStatus(ScaStatus.PSUIDENTIFIED);
                response.setResponseLinkType(ConsentAuthorizationResponseLinkType.startAuthorisationWithPsuAuthentication);
                return response;
            })
            .orElseGet(() -> {
                response.setScaStatus(ScaStatus.RECEIVED);
                response.setResponseLinkType(ConsentAuthorizationResponseLinkType.startAuthorisationWithPsuIdentfication);
                return response;
            });
    }

    @Override
    public UpdateConsentPsuDataResponse updateConsentPsuData(UpdateConsentPsuDataReq updatePsuData, SpiAccountConsentAuthorization consentAuthorization) {
        UpdateConsentPsuDataResponse response = new UpdateConsentPsuDataResponse();

        if (checkPsuIdentification(updatePsuData, response)) return response;

        if (checkPsuAuthentication(updatePsuData, response, consentAuthorization)) return response;

        if (checkScaMethod(updatePsuData, response, consentAuthorization)) return response;

        if (checkScaAuthenticationData(updatePsuData, response, consentAuthorization)) return response;

        return response;

    }

    private boolean checkPsuIdentification(UpdateConsentPsuDataReq updatePsuData, UpdateConsentPsuDataResponse response) {
        if (updatePsuData.isUpdatePsuIdentification()) {
            response.setPsuId(updatePsuData.getPsuId());
            response.setScaStatus(ScaStatus.PSUIDENTIFIED);
            response.setResponseLinkType(ConsentAuthorizationResponseLinkType.startAuthorisationWithPsuAuthentication);
            return true;
        }
        return false;
    }

    private boolean checkPsuAuthentication(UpdateConsentPsuDataReq updatePsuData, UpdateConsentPsuDataResponse response, SpiAccountConsentAuthorization spiAuthorization) {
        if (spiAuthorization.getPassword() == null && updatePsuData.getPassword() != null) {
            response.setPassword(updatePsuData.getPassword());

            SpiResponse<List<SpiScaMethod>> spiResponse = accountSpi.readAvailableScaMethods(spiAuthorization.getPsuId(), spiAuthorization.getPassword());
            if (spiResponse.getPayload().size() > 1) {
                response.setScaStatus(ScaStatus.PSUAUTHENTICATED);
                response.setResponseLinkType(ConsentAuthorizationResponseLinkType.startAuthorisationWithAuthentciationMethodSelection);
                return true;
            } else {
                response.setAuthenticationMethodId(spiResponse.getPayload().get(0).getId());
                response.setScaStatus(ScaStatus.SCAMETHODSELECTED);
                response.setResponseLinkType(ConsentAuthorizationResponseLinkType.startAuthorisationWithTransactionAuthorisation);
                return true;
            }
        }
        return false;
    }

    private boolean checkScaMethod(UpdateConsentPsuDataReq updatePsuData, UpdateConsentPsuDataResponse response, SpiAccountConsentAuthorization spiAuthorization) {
        if (spiAuthorization.getAuthenticationMethodId() == null && updatePsuData.getAuthenticationMethodId() != null) {
            response.setAuthenticationMethodId(updatePsuData.getAuthenticationMethodId());
            response.setScaStatus(ScaStatus.SCAMETHODSELECTED);
            response.setResponseLinkType(ConsentAuthorizationResponseLinkType.startAuthorisationWithTransactionAuthorisation);
            return true;
        }
        return false;
    }

    private boolean checkScaAuthenticationData(UpdateConsentPsuDataReq updatePsuData, UpdateConsentPsuDataResponse response, SpiAccountConsentAuthorization spiAuthorization) {
        if (updatePsuData.getScaAuthenticationData() != null) {
            response.setScaAuthenticationData(updatePsuData.getScaAuthenticationData());
            response.setScaStatus(ScaStatus.STARTED);
            response.setResponseLinkType(ConsentAuthorizationResponseLinkType.startAuthorisationWithAuthentciationMethodSelection);
            return true;
        }
        return false;
    }


}
