/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at psd2@adorsys.com.
 */

package de.adorsys.psd2.consent.service.mapper;

import de.adorsys.psd2.consent.api.ais.AisAccountAccess;
import de.adorsys.psd2.consent.api.ais.AisAccountConsentAuthorisation;
import de.adorsys.psd2.consent.api.ais.CmsAisAccountConsent;
import de.adorsys.psd2.consent.domain.AuthorisationEntity;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.domain.consent.ConsentTppInformationEntity;
import de.adorsys.psd2.consent.service.AisConsentUsageService;
import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.core.data.ais.AisConsentData;
import de.adorsys.psd2.core.mapper.ConsentDataMapper;
import de.adorsys.psd2.xs2a.core.ais.AccountAccessType;
import de.adorsys.psd2.xs2a.core.authorisation.ConsentAuthorization;
import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AisConsentMapper {
    private final PsuDataMapper psuDataMapper;
    private final TppInfoMapper tppInfoMapper;
    private final AisConsentUsageService aisConsentUsageService;
    private final AuthorisationTemplateMapper authorisationTemplateMapper;
    private final ConsentDataMapper consentDataMapper;
    private final ConsentTppInformationMapper consentTppInformationMapper;
    private final AccessMapper accessMapper;

    private AisAccountAccess getAvailableAccess(AisConsent aisConsent) {
        AisAccountAccess tppAccountAccess = mapToAisAccountAccess(aisConsent);
        AisAccountAccess aspspAccountAccess = mapToAspspAisAccountAccess(aisConsent);

        if (tppAccountAccess.getAllPsd2() != null
                || !aspspAccountAccess.isNotEmpty()) {
            return tppAccountAccess;
        }

        return aspspAccountAccess;
    }

    public CmsAisAccountConsent mapToCmsAisAccountConsent(ConsentEntity consent, List<AuthorisationEntity> authorisations) {
        AisConsent aisConsent = mapToAisConsent(consent, authorisations);
        AisAccountAccess chosenAccess = getAvailableAccess(aisConsent);
        ConsentTppInformationEntity tppInformation = consent.getTppInformation();

        return new CmsAisAccountConsent(
            consent.getExternalId(),
            chosenAccess,
            consent.isRecurringIndicator(),
            consent.getValidUntil(),
            consent.getExpireDate(),
            consent.getFrequencyPerDay(),
            consent.getLastActionDate(),
            consent.getConsentStatus(),
            aisConsent.isWithBalance(),
            tppInformation.isTppRedirectPreferred(),
            aisConsent.getConsentRequestType(),
            aisConsent.getPsuIdDataList(),
            tppInfoMapper.mapToTppInfo(tppInformation.getTppInfo()),
            aisConsent.getAuthorisationTemplate(),
            consent.isMultilevelScaRequired(),
            mapToAisAccountConsentAuthorisation(authorisations),
            aisConsent.getUsageCounterMap(),
            consent.getCreationTimestamp(),
            consent.getStatusChangeTimestamp(),
            tppInformation.getTppBrandLoggingInformation(),
            tppInformation.getAdditionalInfo());
    }

    public AisConsent mapToAisConsent(ConsentEntity entity, List<AuthorisationEntity> authorisations) {
        AisConsentData aisConsentData = consentDataMapper.mapToAisConsentData(entity.getData());

        Map<String, Integer> usageCounterMap = aisConsentUsageService.getUsageCounterMap(entity);

        return AisConsent.builder()
                   .consentData(aisConsentData)
                   .id(entity.getExternalId())
                   .internalRequestId(entity.getInternalRequestId())
                   .consentStatus(entity.getConsentStatus())
                   .frequencyPerDay(entity.getFrequencyPerDay())
                   .recurringIndicator(entity.isRecurringIndicator())
                   .multilevelScaRequired(entity.isMultilevelScaRequired())
                   .validUntil(entity.getValidUntil())
                   .expireDate(entity.getExpireDate())
                   .lastActionDate(entity.getLastActionDate())
                   .creationTimestamp(entity.getCreationTimestamp())
                   .statusChangeTimestamp(entity.getStatusChangeTimestamp())
                   .consentTppInformation(consentTppInformationMapper.mapToConsentTppInformation(entity.getTppInformation()))
                   .authorisationTemplate(authorisationTemplateMapper.mapToAuthorisationTemplate(entity.getAuthorisationTemplate()))
                   .psuIdDataList(psuDataMapper.mapToPsuIdDataList(entity.getPsuDataList()))
                   .authorisations(mapToAccountConsentAuthorisations(authorisations))
                   .usages(usageCounterMap)
                   .tppAccountAccesses(accessMapper.mapTppAccessesToAccountAccess(entity.getTppAccountAccesses(),
                                                                                  entity.getOwnerNameType(),
                                                                                  entity.getTrustedBeneficiariesType()))
                   .aspspAccountAccesses(accessMapper.mapAspspAccessesToAccountAccess(entity.getAspspAccountAccesses(),
                                                                                      entity.getOwnerNameType(),
                                                                                      entity.getTrustedBeneficiariesType()))
                   .instanceId(entity.getInstanceId())
                   .consentType(ConsentType.AIS)
                   .signingBasketBlocked(entity.isSigningBasketBlocked())
                   .signingBasketAuthorised(entity.isSigningBasketAuthorised())
                   .build();
    }

    public AccountAccess mapToAccountAccess(AisAccountAccess accountAccess) {
        return new AccountAccess(ListUtils.emptyIfNull(accountAccess.getAccounts()),
                                 ListUtils.emptyIfNull(accountAccess.getBalances()),
                                 ListUtils.emptyIfNull(accountAccess.getTransactions()),
                                 accountAccess.getAccountAdditionalInformationAccess());
    }

    private AisAccountAccess mapToAisAccountAccess(AisConsent aisConsent) {
        AccountAccess tppAccesses = aisConsent.getTppAccountAccesses();
        AisConsentData consentData = aisConsent.getConsentData();
        return new AisAccountAccess(tppAccesses.getAccounts(),
                                    tppAccesses.getBalances(),
                                    tppAccesses.getTransactions(),
                                    getAccessType(consentData.getAvailableAccounts()),
                                    getAccessType(consentData.getAllPsd2()),
                                    getAccessType(consentData.getAvailableAccountsWithBalance()),
                                    tppAccesses.getAdditionalInformationAccess());
    }

    private AisAccountAccess mapToAspspAisAccountAccess(AisConsent aisConsent) {
        AccountAccess aspspAccesses = aisConsent.getAspspAccountAccesses();
        AisConsentData consentData = aisConsent.getConsentData();
        return new AisAccountAccess(aspspAccesses.getAccounts(),
                                    aspspAccesses.getBalances(),
                                    aspspAccesses.getTransactions(),
                                    getAccessType(consentData.getAvailableAccounts()),
                                    getAccessType(consentData.getAllPsd2()),
                                    getAccessType(consentData.getAvailableAccountsWithBalance()),
                                    aspspAccesses.getAdditionalInformationAccess());
    }

    private String getAccessType(AccountAccessType type) {
        return Optional.ofNullable(type)
                   .map(Enum::name)
                   .orElse(null);
    }


    private List<AisAccountConsentAuthorisation> mapToAisAccountConsentAuthorisation(List<AuthorisationEntity> aisConsentAuthorisations) {
        if (CollectionUtils.isEmpty(aisConsentAuthorisations)) {
            return Collections.emptyList();
        }

        return aisConsentAuthorisations.stream()
                   .map(this::mapToAisAccountConsentAuthorisation)
                   .collect(Collectors.toList());
    }

    private List<ConsentAuthorization> mapToAccountConsentAuthorisations(List<AuthorisationEntity> aisConsentAuthorisations) {
        if (CollectionUtils.isEmpty(aisConsentAuthorisations)) {
            return Collections.emptyList();
        }

        return aisConsentAuthorisations.stream()
                   .map(this::mapToAccountConsentAuthorisation)
                   .collect(Collectors.toList());
    }

    private AisAccountConsentAuthorisation mapToAisAccountConsentAuthorisation(AuthorisationEntity aisConsentAuthorisation) {
        return Optional.ofNullable(aisConsentAuthorisation)
                   .map(auth -> new AisAccountConsentAuthorisation(auth.getExternalId(),
                                                                   psuDataMapper.mapToPsuIdData(auth.getPsuData()),
                                                                   auth.getScaStatus()))
                   .orElse(null);
    }

    private ConsentAuthorization mapToAccountConsentAuthorisation(AuthorisationEntity aisConsentAuthorisation) {
        return Optional.ofNullable(aisConsentAuthorisation)
                   .map(auth -> {
                       ConsentAuthorization authorisation = new ConsentAuthorization();

                       authorisation.setId(auth.getExternalId());
                       authorisation.setConsentId(auth.getParentExternalId());
                       authorisation.setPsuIdData(psuDataMapper.mapToPsuIdData(auth.getPsuData()));
                       authorisation.setScaStatus(auth.getScaStatus());
                       authorisation.setPassword(null);
                       authorisation.setChosenScaApproach(auth.getScaApproach());
                       authorisation.setAuthenticationMethodId(auth.getAuthenticationMethodId());
                       authorisation.setScaAuthenticationData(auth.getScaAuthenticationData());

                       return authorisation;
                   })
                   .orElse(null);
    }

}
