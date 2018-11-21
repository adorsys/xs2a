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

package de.adorsys.psd2.xs2a.service.mapper.consent;

import de.adorsys.psd2.consent.api.ais.AisConsentAuthorizationRequest;
import de.adorsys.psd2.consent.api.ais.AisConsentAuthorizationResponse;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsentAuthorization;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataReq;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class Xs2aAisConsentAuthorisationMapper {

    public AccountConsentAuthorization mapToAccountConsentAuthorization(
        AisConsentAuthorizationResponse spiConsentAuthorization) {
        return Optional.ofNullable(spiConsentAuthorization)
                   .map(conAuth -> {
                       AccountConsentAuthorization consentAuthorization = new AccountConsentAuthorization();

                       consentAuthorization.setId(conAuth.getAuthorizationId());
                       consentAuthorization.setConsentId(conAuth.getConsentId());
                       consentAuthorization.setPsuId(conAuth.getPsuId());
                       consentAuthorization.setScaStatus(conAuth.getScaStatus());
                       consentAuthorization.setAuthenticationMethodId(conAuth.getAuthenticationMethodId());
                       consentAuthorization.setScaAuthenticationData(conAuth.getScaAuthenticationData());
                       consentAuthorization.setPassword(conAuth.getPassword());
                       return consentAuthorization;
                   })
                   .orElse(null);
    }

    public AisConsentAuthorizationRequest mapToAisConsentAuthorization(ScaStatus scaStatus, PsuIdData psuData) {
        return Optional.ofNullable(scaStatus)
                   .map(st -> {
                       AisConsentAuthorizationRequest consentAuthorization = new AisConsentAuthorizationRequest();
                       consentAuthorization.setPsuData(psuData);
                       consentAuthorization.setScaStatus(scaStatus);
                       return consentAuthorization;
                   })
                   .orElse(null);
    }

    public AisConsentAuthorizationRequest mapToAisConsentAuthorizationRequest(UpdateConsentPsuDataReq updatePsuData) {
        return Optional.ofNullable(updatePsuData)
                   .map(data -> {
                       AisConsentAuthorizationRequest consentAuthorization = new AisConsentAuthorizationRequest();
                       consentAuthorization.setPsuData(data.getPsuData());
                       consentAuthorization.setScaStatus(data.getScaStatus());
                       consentAuthorization.setAuthenticationMethodId(data.getAuthenticationMethodId());
                       consentAuthorization.setPassword(data.getPassword());
                       consentAuthorization.setScaAuthenticationData(data.getScaAuthenticationData());

                       return consentAuthorization;
                   })
                   .orElse(null);
    }

}
