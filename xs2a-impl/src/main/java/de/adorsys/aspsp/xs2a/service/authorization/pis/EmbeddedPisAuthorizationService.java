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

package de.adorsys.aspsp.xs2a.service.authorization.pis;

import de.adorsys.aspsp.xs2a.domain.consent.*;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiScaStatus;
import de.adorsys.aspsp.xs2a.spi.service.ConsentSpi;
import de.adorsys.psd2.model.ScaStatus;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

import static de.adorsys.aspsp.xs2a.domain.consent.ConsentAuthorizationResponseLinkType.START_AUTHORISATION_WITH_PSU_AUTHENTICATION;
import static de.adorsys.aspsp.xs2a.domain.consent.ConsentAuthorizationResponseLinkType.START_AUTHORISATION_WITH_PSU_IDENTIFICATION;

@RequiredArgsConstructor
public class EmbeddedPisAuthorizationService implements PisAuthorizationService {
    private final ConsentSpi consentSpi;

    @Override
    public Optional<CreateConsentAuthorizationResponse> createConsentAuthorization(String psuId, String paymentId) {
        return StringUtils.isBlank(psuId)
                   ? createConsentAuthorizationAndGetResponse(ScaStatus.RECEIVED, START_AUTHORISATION_WITH_PSU_IDENTIFICATION, paymentId)
                   : createConsentAuthorizationAndGetResponse(ScaStatus.PSUAUTHENTICATED, START_AUTHORISATION_WITH_PSU_AUTHENTICATION, paymentId);
    }

    private Optional<CreateConsentAuthorizationResponse> createConsentAuthorizationAndGetResponse(ScaStatus scaStatus, ConsentAuthorizationResponseLinkType linkType, String paymentId) {
        return consentSpi.createPisConsentAuthorization(paymentId, SpiScaStatus.valueOf(scaStatus.name()))
                   .map(authId -> {
                       CreateConsentAuthorizationResponse resp = new CreateConsentAuthorizationResponse();
                       resp.setAuthorizationId(authId);
                       resp.setScaStatus(scaStatus);
                       resp.setResponseLinkType(linkType);

                       return resp;
                   });
    }

    @Override
    public UpdateConsentPsuDataResponse updateConsentPsuData(UpdateConsentPsuDataReq updatePsuData, AccountConsentAuthorization consentAuthorization) {
        return null;
    }

    @Override
    public AccountConsentAuthorization getAccountConsentAuthorizationById(String authorizationId, String consentId) {
        return null;
    }
}
