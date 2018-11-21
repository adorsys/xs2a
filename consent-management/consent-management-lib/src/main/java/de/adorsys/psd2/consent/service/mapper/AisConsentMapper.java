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

package de.adorsys.psd2.consent.service.mapper;

import de.adorsys.psd2.consent.api.TypeAccess;
import de.adorsys.psd2.consent.api.ais.AisAccountAccess;
import de.adorsys.psd2.consent.api.ais.AisAccountConsent;
import de.adorsys.psd2.consent.api.ais.AisConsentAuthorizationResponse;
import de.adorsys.psd2.consent.api.ais.CmsAccountReference;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.account.AccountAccess;
import de.adorsys.psd2.consent.domain.account.AisConsent;
import de.adorsys.psd2.consent.domain.account.AisConsentAuthorization;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AisConsentMapper {
    private final PsuDataMapper psuDataMapper;

    public AisAccountConsent mapToAisAccountConsent(AisConsent consent) {
        return new AisAccountConsent(
            consent.getId().toString(),
            mapToAisAccountAccess(consent.getAccesses()),
            consent.isRecurringIndicator(),
            consent.getExpireDate(),
            consent.getUsageCounter(),
            consent.getLastActionDate(),
            consent.getConsentStatus(),
            consent.getAccesses().stream().anyMatch(a -> a.getTypeAccess() == TypeAccess.BALANCE),
            consent.isTppRedirectPreferred(),
            consent.getAisConsentRequestType(),
            psuDataMapper.mapToPsuIdData(consent.getPsuData()),
            consent.getTppId());
    }

    public AisConsentAuthorizationResponse mapToAisConsentAuthorizationResponse(AisConsentAuthorization aisConsentAuthorization) {
        return Optional.ofNullable(aisConsentAuthorization)
                   .map(conAuth -> {
                       AisConsentAuthorizationResponse resp = new AisConsentAuthorizationResponse();
                       resp.setAuthorizationId(conAuth.getExternalId());
                       resp.setPsuId(Optional.ofNullable(conAuth.getPsuData())
                                         .map(PsuData::getPsuId)
                                         .orElse(null));
                       resp.setConsentId(conAuth.getConsent().getExternalId());
                       resp.setScaStatus(conAuth.getScaStatus());
                       resp.setAuthenticationMethodId(conAuth.getAuthenticationMethodId());
                       resp.setScaAuthenticationData(conAuth.getScaAuthenticationData());

                       return resp;
                   })
                   .orElse(null);
    }

    private AisAccountAccess mapToAisAccountAccess(List<AccountAccess> accountAccesses) {
        return new AisAccountAccess(mapToCmsAccountReference(accountAccesses, TypeAccess.ACCOUNT),
            mapToCmsAccountReference(accountAccesses, TypeAccess.BALANCE),
            mapToCmsAccountReference(accountAccesses, TypeAccess.TRANSACTION));
    }

    private List<CmsAccountReference> mapToCmsAccountReference(List<AccountAccess> aisAccounts, TypeAccess typeAccess) {
        return aisAccounts.stream()
                   .filter(ass -> ass.getTypeAccess() == typeAccess)
                   .map(access -> new CmsAccountReference(access.getResourceId(), access.getIban(), access.getCurrency()))
                   .collect(Collectors.toList());
    }
}
