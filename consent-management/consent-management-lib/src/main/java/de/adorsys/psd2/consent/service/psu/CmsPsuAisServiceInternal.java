/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.consent.service.psu;


import de.adorsys.psd2.consent.api.ais.AisAccountConsent;
import de.adorsys.psd2.consent.api.service.AisConsentService;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.account.AisConsent;
import de.adorsys.psd2.consent.domain.account.AisConsentAuthorization;
import de.adorsys.psd2.consent.psu.api.CmsPsuAisService;
import de.adorsys.psd2.consent.psu.api.ais.CmsAisConsentAccessRequest;
import de.adorsys.psd2.consent.psu.api.ais.CmsAisConsentResponse;
import de.adorsys.psd2.consent.repository.AisConsentAuthorisationRepository;
import de.adorsys.psd2.consent.repository.AisConsentRepository;
import de.adorsys.psd2.consent.repository.PsuDataRepository;
import de.adorsys.psd2.consent.repository.specification.AisConsentAuthorizationSpecification;
import de.adorsys.psd2.consent.repository.specification.AisConsentSpecification;
import de.adorsys.psd2.consent.service.mapper.AisConsentMapper;
import de.adorsys.psd2.consent.service.mapper.PsuDataMapper;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.adorsys.psd2.xs2a.core.consent.ConsentStatus.*;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
//TODO Discuss instanceId security workflow https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/577
public class CmsPsuAisServiceInternal implements CmsPsuAisService {
    private final AisConsentRepository aisConsentRepository;
    private final AisConsentMapper consentMapper;
    private final PsuDataRepository psuDataRepository;
    private final PsuDataMapper psuDataMapper;
    private final AisConsentAuthorisationRepository aisConsentAuthorisationRepository;
    private final AisConsentAuthorizationSpecification aisConsentAuthorizationSpecification;
    private final AisConsentSpecification aisConsentSpecification;
    private final AisConsentService aisConsentService;

    @Override
    @Transactional
    public boolean updatePsuDataInConsent(@NotNull PsuIdData psuIdData, @NotNull String authorisationId, @NotNull String instanceId) {
        AisConsentAuthorization authorisation = aisConsentAuthorisationRepository.findOne(
                aisConsentAuthorizationSpecification.byExternalIdAndInstanceId(authorisationId, instanceId));
        return Optional.ofNullable(authorisation)
                   .map(AisConsentAuthorization::getConsent)
                   .map(con -> updatePsuData(con, psuIdData))
                   .orElse(false);
    }

    @Override
    @Transactional
    public @NotNull Optional<AisAccountConsent> getConsent(@NotNull PsuIdData psuIdData, @NotNull String consentId, @NotNull String instanceId) {
        return Optional.ofNullable(aisConsentRepository.findOne(aisConsentSpecification.byConsentIdAndInstanceId(consentId, instanceId)))
                   .map(this::checkAndUpdateOnExpiration)
                   .map(consentMapper::mapToAisAccountConsent);
    }

    @Override
    @Transactional
    public boolean updateAuthorisationStatus(@NotNull PsuIdData psuIdData, @NotNull String consentId, @NotNull String authorisationId, @NotNull ScaStatus status, @NotNull String instanceId) {
        Optional<AisConsent> actualAisConsent = getActualAisConsent(consentId, instanceId);

        if (!actualAisConsent.isPresent()) {
            return false;
        }

        return Optional.ofNullable(aisConsentAuthorisationRepository.findOne(aisConsentAuthorizationSpecification.byExternalIdAndInstanceId(authorisationId, instanceId)))
                   .map(auth -> updateScaStatus(status, auth))
                   .orElse(false);
    }

    @Override
    @Transactional
    public boolean confirmConsent(@NotNull PsuIdData psuIdData, @NotNull String consentId, @NotNull String instanceId) {
        if (changeConsentStatus(consentId, VALID, instanceId)) {
            aisConsentService.findAndTerminateOldConsentsByNewConsentId(consentId);
            return true;
        }

        return false;
    }

    @Override
    @Transactional
    public boolean rejectConsent(@NotNull PsuIdData psuIdData, @NotNull String consentId, @NotNull String instanceId) {
        return changeConsentStatus(consentId, REJECTED, instanceId);
    }

    @Override
    public @NotNull List<AisAccountConsent> getConsentsForPsu(@NotNull PsuIdData psuIdData, @NotNull String instanceId) {
        return aisConsentRepository.findAll(aisConsentSpecification.byPsuIdIdAndInstanceId(psuIdData.getPsuId(), instanceId)).stream()
                   .map(consentMapper::mapToAisAccountConsent)
                   .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean revokeConsent(@NotNull PsuIdData psuIdData, @NotNull String consentId, @NotNull String instanceId) {
        return changeConsentStatus(consentId, REVOKED_BY_PSU, instanceId);
    }

    @Override
    @Transactional
    public @NotNull Optional<CmsAisConsentResponse> checkRedirectAndGetConsent(@NotNull String redirectId, @NotNull String instanceId) {
        Optional<AisConsentAuthorization> optionalAuthorisation = Optional.ofNullable(aisConsentAuthorisationRepository.findOne(aisConsentAuthorizationSpecification.byExternalIdAndInstanceId(redirectId, instanceId)))
                                                                      .filter(a -> a.getScaStatus().isNotFinalisedStatus());

        if (optionalAuthorisation.isPresent()) {
            AisConsentAuthorization authorisation = optionalAuthorisation.get();

            if (authorisation.isNotExpired()) {
                return createCmsAisConsentResponseFromAisConsent(authorisation.getConsent(), redirectId);
            }

            updateAuthorisationOnExpiration(authorisation);
            String tppNokRedirectUri = authorisation.getConsent().getTppInfo().getNokRedirectUri();

            return Optional.of(new CmsAisConsentResponse(tppNokRedirectUri));
        }

        return Optional.empty();
    }

    @Override
    public boolean saveAccountAccessInConsent(@NotNull String consentId,
                                              @NotNull CmsAisConsentAccessRequest accountAccessRequest
                                             ) {
        // TODO implement method https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/596
        return false;
    }

    private boolean changeConsentStatus(String consentId, ConsentStatus status, String instanceId) {
        return Optional.ofNullable(aisConsentRepository.findOne(aisConsentSpecification.byConsentIdAndInstanceId(consentId, instanceId)))
                   .map(con -> updateConsentStatus(con, status))
                   .orElse(false);
    }

    private AisConsent checkAndUpdateOnExpiration(AisConsent consent) {
        if (consent != null && consent.isExpiredByDate() && consent.isStatusNotExpired()) {
            consent.setConsentStatus(EXPIRED);
            consent.setExpireDate(LocalDate.now());
            consent.setLastActionDate(LocalDate.now());
            aisConsentRepository.save(consent);
        }
        return consent;
    }

    private Optional<AisConsent> getActualAisConsent(String consentId, String instanceId) {
        return Optional.ofNullable(aisConsentRepository.findOne(aisConsentSpecification.byConsentIdAndInstanceId(consentId, instanceId)))
                   .filter(c -> !c.getConsentStatus().isFinalisedStatus());
    }

    private boolean updateConsentStatus(AisConsent consent, ConsentStatus status) {
        if (consent.getConsentStatus().isFinalisedStatus()) {
            return false;
        }
        consent.setLastActionDate(LocalDate.now());
        consent.setConsentStatus(status);
        return aisConsentRepository.save(consent) != null;
    }

    private boolean updatePsuData(AisConsent consent, PsuIdData psuIdData) {
        PsuData psuData = consent.getPsuData();
        psuData.setPsuId(psuIdData.getPsuId());
        psuData.setPsuIdType(psuIdData.getPsuIdType());
        psuData.setPsuCorporateId(psuIdData.getPsuCorporateId());
        psuData.setPsuCorporateIdType(psuIdData.getPsuCorporateIdType());

        return psuDataRepository.save(psuData) != null;
    }

    private boolean updateScaStatus(@NotNull ScaStatus status, AisConsentAuthorization authorization) {
        if (authorization.getScaStatus().isFinalisedStatus()) {
            return false;
        }
        authorization.setScaStatus(status);
        return aisConsentAuthorisationRepository.save(authorization) != null;
    }

    private void updateAuthorisationOnExpiration(AisConsentAuthorization authorisation) {
        authorisation.setScaStatus(ScaStatus.FAILED);
        aisConsentAuthorisationRepository.save(authorisation);
    }

    private Optional<CmsAisConsentResponse> createCmsAisConsentResponseFromAisConsent(AisConsent aisConsent, String
                                                                                                                 redirectId) {
        if (aisConsent == null) {
            return Optional.empty();
        }

        AisAccountConsent aisAccountConsent = consentMapper.mapToAisAccountConsent(aisConsent);

        Optional<TppInfo> tppInfoOptional = Optional.ofNullable(aisAccountConsent)
                                                .map(AisAccountConsent::getTppInfo);

        if (!tppInfoOptional.isPresent()) {
            return Optional.empty();
        }

        Optional<TppRedirectUri> tppRedirectUriOptional = tppInfoOptional.map(TppInfo::getTppRedirectUri);

        String tppOkRedirectUri = null;
        String tppNokRedirectUri = null;

        if (tppRedirectUriOptional.isPresent()) {
            TppRedirectUri tppRedirectUri = tppRedirectUriOptional.get();
            tppOkRedirectUri = tppRedirectUri.getUri();
            tppNokRedirectUri = tppRedirectUri.getNokUri();
        }

        return Optional.of(new CmsAisConsentResponse(aisAccountConsent, redirectId, tppOkRedirectUri, tppNokRedirectUri));
    }
}
