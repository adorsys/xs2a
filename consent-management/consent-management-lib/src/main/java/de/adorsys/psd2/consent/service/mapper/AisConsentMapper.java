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
import de.adorsys.psd2.consent.api.ais.AisAccountConsentAuthorisation;
import de.adorsys.psd2.consent.api.ais.AisConsentAuthorizationResponse;
import de.adorsys.psd2.consent.domain.account.AisConsent;
import de.adorsys.psd2.consent.domain.account.AisConsentAuthorization;
import de.adorsys.psd2.consent.domain.account.AspspAccountAccess;
import de.adorsys.psd2.consent.domain.account.TppAccountAccess;
import de.adorsys.psd2.consent.service.AisConsentUsageService;
import de.adorsys.psd2.xs2a.core.ais.AccountAccessType;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceSelector;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AisConsentMapper {
    private final PsuDataMapper psuDataMapper;
    private final TppInfoMapper tppInfoMapper;
    private final AisConsentUsageService aisConsentUsageService;

    /**
     * Maps AisConsent to AisAccountConsent with accesses populated with account references, provided by ASPSP.
     * <p>
     * If no account references were provided by the ASPSP, TPP accesses will be used instead.
     *
     * @param consent AIS consent entity
     * @return mapped AIS consent
     */
    public AisAccountConsent mapToAisAccountConsent(AisConsent consent) {
        AisAccountAccess aisAccountAccess = consent.getAspspAccountAccesses().isEmpty()
                                                ? mapToAisAccountAccess(consent)
                                                : mapToAspspAisAccountAccess(consent);

        return new AisAccountConsent(
            consent.getExternalId(),
            aisAccountAccess,
            consent.isRecurringIndicator(),
            consent.getExpireDate(),
            consent.getTppFrequencyPerDay(),
            consent.getLastActionDate(),
            consent.getConsentStatus(),
            consent.getAccesses().stream().anyMatch(a -> a.getTypeAccess() == TypeAccess.BALANCE),
            consent.isTppRedirectPreferred(),
            consent.getAisConsentRequestType(),
            psuDataMapper.mapToPsuIdDataList(consent.getPsuDataList()),
            tppInfoMapper.mapToTppInfo(consent.getTppInfo()),
            consent.isMultilevelScaRequired(),
            mapToAisAccountConsentAuthorisation(consent.getAuthorizations()),
            aisConsentUsageService.getUsageCounter(consent),
            consent.getCreationTimestamp(),
            consent.getStatusChangeTimestamp());
    }

    /**
     * Maps AisConsent to AisAccountConsent with accesses populated with account references, provided by TPP.
     *
     * @param consent AIS consent entity
     * @return mapped AIS consent
     */
    public AisAccountConsent mapToInitialAisAccountConsent(AisConsent consent) {
        return new AisAccountConsent(
            consent.getExternalId(),
            mapToAisAccountAccess(consent),
            consent.isRecurringIndicator(),
            consent.getExpireDate(),
            consent.getTppFrequencyPerDay(),
            consent.getLastActionDate(),
            consent.getConsentStatus(),
            consent.getAccesses().stream().anyMatch(a -> a.getTypeAccess() == TypeAccess.BALANCE),
            consent.isTppRedirectPreferred(),
            consent.getAisConsentRequestType(),
            psuDataMapper.mapToPsuIdDataList(consent.getPsuDataList()),
            tppInfoMapper.mapToTppInfo(consent.getTppInfo()),
            consent.isMultilevelScaRequired(),
            mapToAisAccountConsentAuthorisation(consent.getAuthorizations()),
            aisConsentUsageService.getUsageCounter(consent),
            consent.getCreationTimestamp(),
            consent.getStatusChangeTimestamp());
    }

    public AisConsentAuthorizationResponse mapToAisConsentAuthorizationResponse(AisConsentAuthorization aisConsentAuthorization) {
        return Optional.ofNullable(aisConsentAuthorization)
                   .map(conAuth -> {
                       AisConsentAuthorizationResponse resp = new AisConsentAuthorizationResponse();
                       resp.setAuthorizationId(conAuth.getExternalId());
                       resp.setPsuIdData(psuDataMapper.mapToPsuIdData(conAuth.getPsuData()));
                       resp.setConsentId(conAuth.getConsent().getExternalId());
                       resp.setScaStatus(conAuth.getScaStatus());
                       resp.setAuthenticationMethodId(conAuth.getAuthenticationMethodId());
                       resp.setScaAuthenticationData(conAuth.getScaAuthenticationData());
                       resp.setChosenScaApproach(conAuth.getScaApproach());

                       return resp;
                   })
                   .orElse(null);
    }

    public Set<AspspAccountAccess> mapAspspAccountAccesses(AisAccountAccess aisAccountAccess) {
        Set<AspspAccountAccess> accesses = new HashSet<>();
        accesses.addAll(getAspspAccountAccesses(TypeAccess.ACCOUNT, aisAccountAccess.getAccounts()));
        accesses.addAll(getAspspAccountAccesses(TypeAccess.BALANCE, aisAccountAccess.getBalances()));
        accesses.addAll(getAspspAccountAccesses(TypeAccess.TRANSACTION, aisAccountAccess.getTransactions()));
        return accesses;
    }

    private AisAccountAccess mapToAisAccountAccess(AisConsent consent) {
        List<TppAccountAccess> accesses = consent.getAccesses();
        return new AisAccountAccess(mapToInitialAccountReferences(accesses, TypeAccess.ACCOUNT),
            mapToInitialAccountReferences(accesses, TypeAccess.BALANCE),
            mapToInitialAccountReferences(accesses, TypeAccess.TRANSACTION),
            getAccessType(consent.getAvailableAccounts()),
            getAccessType(consent.getAllPsd2()));
    }

    private List<AccountReference> mapToInitialAccountReferences(List<TppAccountAccess> aisAccounts, TypeAccess typeAccess) {
        return aisAccounts.stream()
                   .filter(a -> a.getTypeAccess() == typeAccess)
                   .map(access -> new AccountReference(access.getAccountReferenceType(), access.getAccountIdentifier(), access.getCurrency()))
                   .collect(Collectors.toList());
    }

    private AisAccountAccess mapToAspspAisAccountAccess(AisConsent consent) {
        List<AspspAccountAccess> accesses = consent.getAspspAccountAccesses();
        return new AisAccountAccess(mapToAccountReferences(accesses, TypeAccess.ACCOUNT),
            mapToAccountReferences(accesses, TypeAccess.BALANCE),
            mapToAccountReferences(accesses, TypeAccess.TRANSACTION),
            getAccessType(consent.getAvailableAccounts()),
            getAccessType(consent.getAllPsd2()));
    }

    private List<AccountReference> mapToAccountReferences(List<AspspAccountAccess> aisAccounts, TypeAccess typeAccess) {
        return aisAccounts.stream()
                   .filter(a -> a.getTypeAccess() == typeAccess)
                   .map(access -> new AccountReference(access.getAccountReferenceType(), access.getAccountIdentifier(), access.getCurrency(), access.getResourceId(), access.getAspspAccountId()))
                   .collect(Collectors.toList());
    }

    private Set<AspspAccountAccess> getAspspAccountAccesses(TypeAccess typeAccess, List<AccountReference> accountReferences) {
        return Optional.ofNullable(accountReferences)
                   .map(lst -> lst.stream()
                                   .map(acc -> mapToAspspAccountAccess(typeAccess, acc))
                                   .collect(Collectors.toSet()))
                   .orElse(Collections.emptySet());
    }

    private AspspAccountAccess mapToAspspAccountAccess(TypeAccess typeAccess, AccountReference accountReference) {
        AccountReferenceSelector selector = accountReference.getUsedAccountReferenceSelector();

        return new AspspAccountAccess(selector.getAccountValue(),
            typeAccess,
            selector.getAccountReferenceType(),
            accountReference.getCurrency(),
            accountReference.getResourceId(),
            accountReference.getAspspAccountId());
    }

    private String getAccessType(AccountAccessType type) {
        return Optional.ofNullable(type)
                   .map(Enum::name)
                   .orElse(null);
    }


    private List<AisAccountConsentAuthorisation> mapToAisAccountConsentAuthorisation(List<AisConsentAuthorization> aisConsentAuthorisations) {
        if (CollectionUtils.isEmpty(aisConsentAuthorisations)) {
            return Collections.emptyList();
        }

        return aisConsentAuthorisations.stream()
                   .map(this::mapToAisAccountConsentAuthorisation)
                   .collect(Collectors.toList());
    }

    private AisAccountConsentAuthorisation mapToAisAccountConsentAuthorisation(AisConsentAuthorization aisConsentAuthorisation) {
        return Optional.ofNullable(aisConsentAuthorisation)
                   .map(auth -> new AisAccountConsentAuthorisation(psuDataMapper.mapToPsuIdData(auth.getPsuData()), auth.getScaStatus()))
                   .orElse(null);
    }
}
